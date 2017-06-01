package cn.cantonese.segmenter.hmm;

import cn.cantonese.segmenter.Utils;
import cn.cantonese.segmenter.data.Data;
import cn.cantonese.segmenter.data.DataIterator;
import cn.cantonese.segmenter.evaluation.Segmenter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HMMTnT implements Segmenter{
    private char[] bos = {'\b', 'x'};

    @Override
    public String getId() {
        return "HMMTnT";
    }

    private int total = 0;
    private Map<String, Integer> map = new HashMap<>();

    private double l1 = 0., l2 = 0. ,l3 = 0.;

    private static final double inf = -1e10;
    private static final char[] id2tag = new char[]{'b', 'm', 'e', 's'};

    public List<String> processSentence(String sentence) {
        int[] tags = tag(sentence.toCharArray());
        List<String> tokens = new LinkedList<>();
        for (int j = 0; j < tags.length; j++) {
            int s = tags[j];
            if (s == 3) {
                tokens.add(sentence.substring(j, j+1));
            }
            else {
                int start = j;
                while (j < tags.length-1 && tags[++j] != 2);
                tokens.add(sentence.substring(start, j+1));
            }
        }
        return tokens;
    }

    public int[] tag(char[] charArray)
    {
        if (charArray.length == 0) return new int[0];
        if (charArray.length == 1) return new int[]{3};
        int[] tag = new int[charArray.length];
        double[][] now = new double[4][4];
        double[] first = new double[4];

        int[][][] link = new int[charArray.length][4][4];

        for (int s = 0; s < 4; ++s)
        {
            double p = (s == 1 || s == 2) ? inf : log_prob(bos, bos, new char[]{charArray[0], id2tag[s]});
            first[s] = p;
        }

        for (int f = 0; f < 4; ++f)
        {
            for (int s = 0; s < 4; ++s)
            {
                double p = first[f] + log_prob(bos, new char[]{charArray[0], id2tag[f]}, new char[]{charArray[1], id2tag[s]});
                now[f][s] = p;
                link[1][f][s] = f;
            }
        }

        double[][] pre = new double[4][4];
        for (int i = 2; i < charArray.length; i++)
        {
            // swap(now, pre)
            double[][] temp = pre;
            pre = now;
            now = temp;
            // end of swap
            for (int s = 0; s < 4; ++s)
            {
                for (int t = 0; t < 4; ++t)
                {
                    now[s][t] = -1e20;
                    for (int f = 0; f < 4; ++f)
                    {
                        double p = pre[f][s] + log_prob(new char[]{charArray[i - 2], id2tag[f]},
                                new char[]{charArray[i - 1], id2tag[s]},
                                new char[]{charArray[i],     id2tag[t]});
                        if (p > now[s][t])
                        {
                            now[s][t] = p;
                            link[i][s][t] = f;
                        }
                    }
                }
            }
        }
        double score = inf;
        int s = 0;
        int t = 0;
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                if (now[i][j] > score)
                {
                    score = now[i][j];
                    s = i;
                    t = j;
                }
            }
        }
        for (int i = link.length - 1; i >= 0; --i)
        {
            tag[i] = t;
            int f = link[i][s][t];
            t = s;
            s = f;
        }
        return tag;
    }

    public void train(Data<String> data) {
        try (DataIterator<String> dataIterator = data.dataIterator()){
            while (dataIterator.hasNext()) {
                learn(dataIterator.next());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        double tl1 = 0.0;
        double tl2 = 0.0;
        double tl3 = 0.0;
        for (String key : map.keySet())
        {
            if (key.length() != 6) continue;    // tri samples
            char[][] now = new char[][]
                    {
                            {key.charAt(0), key.charAt(1)},
                            {key.charAt(2), key.charAt(3)},
                            {key.charAt(4), key.charAt(5)},
                    };
            double c3 = div(get(now) - 1, get(now[0], now[1]) - 1);
            double c2 = div(get(now[1], now[2]) - 1, get(now[1]) - 1);
            double c1 = div(get(now[2]) - 1, total - 1);
            if (c3 >= c1 && c3 >= c2)
                tl3 += get(now);
            else if (c2 >= c1 && c2 >= c3)
                tl2 += get(now);
            else if (c1 >= c2 && c1 >= c3)
                tl1 += get(now);
        }

        l1 = div(tl1, tl1 + tl2 + tl3);
        l2 = div(tl2, tl1 + tl2 + tl3);
        l3 = div(tl3, tl1 + tl2 + tl3);

    }

    private void learn(String sentence) {
        String[] words = sentence.split(Utils.SEG_DELIMITER);
        LinkedList<char[]> states = new LinkedList<>();
        for (String word : words) {
            if (word.length() == 1) {
                states.add(new char[]{word.charAt(0), 's'});
            }
            else {
                states.add(new char[]{word.charAt(0), 'b'});
                for (int i = 1; i < word.length() - 1; i++) {
                    states.add(new char[]{word.charAt(i), 'm'});
                }
                states.add(new char[]{word.charAt(word.length()-1), 'e'});
            }
        }

        char[][] now = new char[3][];
        now[1] = bos;
        now[2] = bos;
        add(1, bos, bos);
        add(2, bos);
        for (char[] state : states) {
            System.arraycopy(now, 1, now, 0, 2);
            now[2] = state;
            add(1, state);
            add(1, now[1], now[2]);
            add(1, now);
        }
    }

    private int get(String key) {
        Integer count = map.get(key);
        return count == null ? 0 : count;
    }

    private int get(char[]...keyArray) {
        return get(convert(keyArray));
    }

    private void add(int count, char[]...keyArray) {
        String key = convert(keyArray);
        add(count, key);
    }

    private void add(int count, String key) {
        Integer i = map.get(key);
        if (i == null) {
            i = 0;
        }
        i += count;
        map.put(key, i);
        total += count;
    }

    private String convert(char[]...keyArray) {
        StringBuilder sb = new StringBuilder(keyArray.length * 2);
        for (char[] k : keyArray) {
            sb.append(k);
        }
        return sb.toString();
    }

    private static double div(int v1, int v2)
    {
        if (v2 == 0) return 0.0;
        return v1 / (double) v2;
    }

    private static double div(double v1, double v2)
    {
        if (v2 == 0) return 0.0;
        return v1 / v2;
    }

    private double freq(String key) {
        return get(key) / (double) total;
    }

    private double freq(char[]...keyArray) {
        return freq(convert(keyArray));
    }

    private double log_prob(char[] s1, char[] s2, char[] s3)
    {
        double uni = l1 * freq(s3);
        double bi = div(l2 * get(s2, s3), get(s2));
        double tri = div(l3 * get(s1, s2, s3), get(s1, s2));
        if (uni + bi + tri == 0)
            return inf;
        return Math.log(uni + bi + tri);
    }
}
