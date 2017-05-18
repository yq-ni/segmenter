package cn.cantonese.segmenter.evaluation;

import cn.cantonese.segmenter.Utils;

public class DefaultEvaluation implements Evaluation{
    public DefaultEvaluation() {}

    public Statistics calStatistics(String gold, String test) {
        int[][] goldL = segSentToIndexTupleList(gold);
        int[][] testL = segSentToIndexTupleList(test);
        int segCorrect = 0, segWrong = 0, goldTotal = goldL.length;
        for (int m = 0; m < testL.length; m++) {
            int[] tupleT = testL[m];
            for (int n = 0; n < goldL.length; n++) {
                int[] tupleG = goldL[n];
                if (tupleT[0] == tupleG[0] && tupleT[1] == tupleG[1]) {
                    segCorrect++;
                    break;
                }
            }
        }
        segWrong = testL.length - segCorrect;
        return new DefaultStatistics(segCorrect, segWrong, goldTotal);
    }

    private int[][] segSentToIndexTupleList(String sent) {
        String[] tokens = sent.split(Utils.SEG_DELIMITER);
        int[][] tupleA = new int[tokens.length][];
        int i = 0, j =0, n = 0;
        for (String token : tokens) {
            j += token.length();
            int[] tuple = {i, j};
            tupleA[n++] = tuple;
            i = j;
        }
        return tupleA;
    }

    class DefaultStatistics implements Statistics {
        private int segCorrect = 0;
        private int segWrong = 0;
        private int goldTotal = 0;
        public DefaultStatistics() {}
        public DefaultStatistics(int segCorrect, int segWrong, int goldTotal) {
            this.segCorrect = segCorrect;
            this.segWrong = segWrong;
            this.goldTotal = goldTotal;
        }

        @Override
        public void merge(Statistics statistics) {
            if (!(statistics instanceof DefaultStatistics)) return;
            segCorrect += ((DefaultStatistics) statistics).segCorrect;
            segWrong += ((DefaultStatistics) statistics).segWrong;
            goldTotal += ((DefaultStatistics) statistics).goldTotal;
        }

        @Override
        public String toString() {
            double R = segCorrect * 1. / goldTotal;
            double P = segCorrect * 1. / (segCorrect + segWrong);
            double F = 2 * P * R / (P + R);
            return String.format("\nDefaultEvaluation:\n {P: %f, R: %f, F: %f}\n", P, R, F);
        }
    }
}
