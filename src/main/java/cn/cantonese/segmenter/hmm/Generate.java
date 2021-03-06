package cn.cantonese.segmenter.hmm;

import cn.cantonese.segmenter.Utils;
import cn.cantonese.segmenter.data.Data;
import cn.cantonese.segmenter.data.DataIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Generate {
    public Generate() {}

    public static void train(Data<String> data, String trans, String start, String emit) {
        int[][] transCount = new int[4][4];
        int BCount = 0, SCount = 0;
        int[] count = new int[4];
        List<Map<Character, Double>> maps = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            maps.add(new HashMap<>());
        }
        try (DataIterator<String> iterator = data.dataIterator()) {
            while (iterator.hasNext()) {
                String sentence = iterator.next();
                List<Integer> states = new ArrayList<>(sentence.length());
                String[] words = sentence.split(Utils.SEG_DELIMITER);
                for (String word : words) {
                    if (word.length() == 1) {
                        put(word.charAt(0), 3, maps, count);
                        states.add(3);
                    }
                    else {
                        put(word.charAt(0), 0, maps, count);
                        states.add(0);
                        for (int i = 1; i < word.length()-1; i++) {
                            put(word.charAt(i), 1, maps, count);
                            states.add(1);
                        }
                        put(word.charAt(word.length()-1), 2, maps, count);
                        states.add(2);
                    }
                }

                if (states.get(0) == 0) {
                    BCount++;
                }
                else {
                    assert states.get(0) == 3;
                    SCount++;
                }

                for (int i = 1; i < states.size(); i++) {
                    transCount[states.get(i-1)][states.get(i)]++;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        transMatrix(transCount, trans);
        startMatrix(BCount, SCount, start);
        emitMatrix(maps, count, emit);
    }

    public static void transMatrix(int[][] count, String path) {
        double[][] trans = new double[4][4];
        for (int i = 0; i < trans.length; i++) {
            int sum = 0;
            for (int j = 0; j < trans[i].length; j++) sum += count[i][j];
            for (int j = 0; j < trans[i].length; j++) {
                trans[i][j] = count[i][j] * 1. / sum;
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (int i = 0; i < trans.length; i++) {
                for (int j = 0; j < trans[i].length; j++) {
                    writer.write(trans[i][j] + "");
                    if (j != trans[i].length-1) {
                        writer.write(Utils.PART_DELIMITER);
                    }
                }
                writer.write(Utils.LINE_DELIMITER);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startMatrix(int BCount, int SCount, String path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            int sum = BCount + SCount;
            double b = BCount * 1. / sum;
            double m = 0., e = m;
            double s = SCount * 1. / sum;
            writer.write(b + "\n");
            writer.write(m + "\n");
            writer.write(e + "\n");
            writer.write(s + "");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: 2017/5/18 how to generate a nice emit matrix
    public static void emitMatrix(List<Map<Character, Double>> maps, int[] count, String path) {
        for (int i = 0; i < maps.size(); i++) {
            Map<Character, Double> map = maps.get(i);
            for (Map.Entry<Character, Double> entry : map.entrySet()) {
                map.put(entry.getKey(), entry.getValue() / count[i]);
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            int i = 0;
            for (String state : new String[] {"B\n", "M\n", "E\n", "S\n"}) {
                writer.write(state);
                for (Map.Entry<Character, Double> entry : maps.get(i).entrySet()) {
                    writer.write(entry.getKey());;
                    writer.write(Utils.PART_DELIMITER);
                    writer.write(""+entry.getValue());
                    writer.write("\n");
                }
                i++;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void put(char c, int state, List<Map<Character, Double>> maps, int[] count) {
        count[state] += 1;
        Double i = maps.get(state).get(c);
        if (i == null) i = 0.;
        i += 1;
        maps.get(state).put(c, i);
    }
}
