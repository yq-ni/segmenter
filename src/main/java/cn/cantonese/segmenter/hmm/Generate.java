package cn.cantonese.segmenter.hmm;

import cn.cantonese.segmenter.Utils;
import cn.cantonese.segmenter.data.Data;
import cn.cantonese.segmenter.data.DataIterator;

import java.io.*;
import java.util.List;

public class Generate {
    public Generate() {}

    public void trans(Data<String> data) {
        int[][] count = new int[4][4];
        double[][] trans = new double[4][4];
        int BCount = 0, SCount = 0;
        try (DataIterator<String> iterator = data.dataIterator()) {
            while (iterator.hasNext()) {
                List<Integer> states = Utils.toStates(iterator.next());
                if (states.get(0) == 0) {
                    BCount++;
                }
                else {
                    assert states.get(0) == 3;
                    SCount++;
                }
                for (int i = 1; i < states.size(); i++) {
                    count[states.get(i-1)][states.get(i)]++;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        for (int i = 0; i < trans.length; i++) {
            int sum = 0;
            for (int j = 0; j < trans[i].length; j++) sum += count[i][j];
            for (int j = 0; j < trans[i].length; j++) {
                trans[i][j] = count[i][j] * 1. / sum;
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("trans.txt"))) {
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

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("start.txt"))) {
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
}
