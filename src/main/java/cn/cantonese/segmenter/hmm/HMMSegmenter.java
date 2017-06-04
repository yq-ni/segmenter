package cn.cantonese.segmenter.hmm;

import cn.cantonese.segmenter.Utils;
import cn.cantonese.segmenter.data.DataIterator;
import cn.cantonese.segmenter.data.FileData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HMMSegmenter {

    private static final String EMIT = "data/emit.txt";
    private static final String TRANS = "data/trans.txt";
    private static final String START = "data/start.txt";
    private static final int STATE_NUM = 4;
    private List<Map<Character, Double>> emitMaps = new ArrayList<>(STATE_NUM);
    private double minEmitProb = Double.MAX_EXPONENT;
    private double[] start = new double[STATE_NUM];
    private double[][] trans =new double[STATE_NUM][STATE_NUM];
    private static final int[][] PREV_STATUS = { {2, 3}, {0, 1}, {0, 1}, {2, 3}};
    private Pattern reSkip = Pattern.compile("((\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+)*)|\\d+\\.\\d+|[a-zA-Z0-9]+)");

    public HMMSegmenter() {
        load(EMIT, TRANS, START);
    }

    public HMMSegmenter(String trans, String start, String emit) {
        load(trans, start, emit);
    }

    private void load(String trans, String start, String emit) {
        try {
            loadSTART(start);
            loadTRANS(trans);
            loadEMIT(emit);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.printf("fail to init HMM: %s\n", e);
        }

    }

    private void loadTRANS(String transPath) throws Exception {
        try (DataIterator<String> iterator = new FileData(transPath).dataIterator()) {
            String[] probStrings;
            for (int i = 0; i < STATE_NUM; i++) {
                probStrings = iterator.next().split(Utils.PART_DELIMITER_REGEX);
                for (int j = 0; j < STATE_NUM; j++) {
                    trans[i][j] = Double.valueOf(probStrings[j]);
                }
            }
        }
    }

    private void loadSTART(String startPath) throws Exception {
        try (DataIterator<String> iterator = new FileData(startPath).dataIterator()) {
            for (int i = 0; i < STATE_NUM; i++) {
                start[i] = Double.valueOf(iterator.next());
            }
        }
    }

    private void loadEMIT(String emitPath) throws Exception {
        try (DataIterator<String> iterator = new FileData(emitPath).dataIterator()) {
            String[] tokens;
            int i = -1;
            Character c;
            Double prob;
            while (iterator.hasNext()) {
                tokens = iterator.next().split(Utils.PART_DELIMITER_REGEX);
                if (tokens.length == 1) {
                    emitMaps.add(new HashMap<>());
                    i++;
                }
                else {
                    c = tokens[0].charAt(0);
                    prob = Double.valueOf(tokens[1]);
                    if (prob < minEmitProb) {
                        minEmitProb = prob;
                    }
                    emitMaps.get(i).put(c, prob);
                }
            }
        }
    }

    public List<String> processSentence(String sentence) {
        int len = sentence.length();
        double[][] V = new double[len][STATE_NUM];
        int[][] path = new int[STATE_NUM][len];

        for (int state = 0; state < STATE_NUM; state++) {
            Double emitProb = emitMaps.get(state).get(sentence.charAt(0));
            if (emitProb == null) emitProb = minEmitProb;
            V[0][state] = start[state] * emitProb;
            path[state][0] = state;
        }

        for (int charIndex = 1; charIndex < len; charIndex++) {
            int[][] newPath = new int[STATE_NUM][len];

            for (int currState = 0; currState < STATE_NUM; currState++) {
                double prob = -1.;
                int state;
                for (int prevState : PREV_STATUS[currState]) {
                    Double emitProb = emitMaps.get(currState).get(sentence.charAt(charIndex));
                    if (emitProb == null) {
                        emitProb = minEmitProb;
                    }
                    double nprob = V[charIndex-1][prevState] * trans[prevState][currState] * emitProb;
                    if (nprob > prob) {
                        prob = nprob;
                        state = prevState;
                        V[charIndex][currState] = prob;
                        System.arraycopy(path[state], 0, newPath[currState], 0, charIndex);
                        newPath[currState][charIndex] = currState;
                    }
                }
            }
            path = newPath;
        }


        int state = V[len-1][2] > V[len-1][3] ? 2 : 3;

        List<String> tokens = new LinkedList<>();
        for (int j = 0; j < len; j++) {
            int s = path[state][j];
            if (s == 3) {
                tokens.add(sentence.substring(j, j+1));
            }
            else {
                int start = j;
                while (j < path[state].length-1 && path[state][++j] != 2);
                tokens.add(sentence.substring(start, j+1));
            }
        }
        return tokens;
    }

    // TODO: 2017/5/18 same problem that process english word firstly
    public List<String> processEnglish(String sentence) {
        LinkedList<String> result = new LinkedList<>();
        Matcher mat = reSkip.matcher(sentence);
        int offset = 0;
        while (mat.find()) {
            if (mat.start() > offset) {
                result.addAll(processSentence(sentence.substring(offset, mat.start())));
            }
            result.add(mat.group());
            offset = mat.end();
        }
        if (offset < sentence.length())
            result.addAll(processSentence(sentence.substring(offset)));
        return result;
    }

}
