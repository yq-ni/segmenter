package cn.cantonese.segmenter.evaluation;

import cn.cantonese.segmenter.Utils;
import java.util.ListIterator;

public class BMES implements Evaluation {
    public static final int STATE_NUM = 4;
    private int[] segCorrect = new int[STATE_NUM];
    private int[] segWrong = new int[STATE_NUM];
    private int[] goldTotal = new int[STATE_NUM];
    public BMES() {}

    public Statistics calStatistics(String gold, String test) {
        for (int i = 0; i < STATE_NUM; i++) {
            this.segCorrect[i] = 0;
            this.segWrong[i] = 0;
            this.goldTotal[i] = 0;
        }
        ListIterator<Integer> goldListIterator = Utils.toStates(gold).listIterator();
        ListIterator<Integer> testListIterator = Utils.toStates(test).listIterator();
        while (goldListIterator.hasNext() && testListIterator.hasNext()) {
            int gs = goldListIterator.next();
            int ts = testListIterator.next();
            goldTotal[gs]++;
            if (gs == ts) {
                segCorrect[ts]++;
            }
            else {
                segWrong[ts]++;
            }
        }
        return new BMESStatistics(segCorrect, segWrong, goldTotal);
    }

    public static class BMESStatistics implements Statistics {
        public static final int STATE_NUM = 4;
        private int[] segCorrect = new int[STATE_NUM];
        private int[] segWrong = new int[STATE_NUM];
        private int[] goldTotal = new int[STATE_NUM];
        BMESStatistics() {}

        BMESStatistics(int[] segCorrect, int[] segWrong, int[] goldTotal) {
            // check length
            for (int i = 0; i < STATE_NUM; i++) {
                this.segCorrect[i] += segCorrect[i];
                this.segWrong[i] += segWrong[i];
                this.goldTotal[i] += goldTotal[i];
            }
        }

        @Override
        public void merge(Statistics statistics) {
            if (!(statistics instanceof BMESStatistics)) throw new RuntimeException("not support");
            for (int i = 0; i < STATE_NUM; i++) {
                this.segCorrect[i] += ((BMESStatistics) statistics).segCorrect[i];
                this.segWrong[i] += ((BMESStatistics) statistics).segWrong[i];
                this.goldTotal[i] += ((BMESStatistics) statistics).goldTotal[i];
            }
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\nBMES:\n");

            for(int i=0;i<4;i++){
                if(i == 0){
                    sb.append("Begin:\t");
                }
                else if(i == 1){
                    sb.append("Medium:\t");
                }
                else if(i == 2){
                    sb.append("End:\t");
                }
                else if(i == 3){
                    sb.append("Single:\t");
                }
                double P = segCorrect[i] * 1./ (segCorrect[i] + segWrong[i]);
                double R = segCorrect[i] * 1./ goldTotal[i];
                double F = P * R * 2 / (P + R);
                sb.append(String.format("P=%f, R=%f, F=%f\n", P, R, F));
            }
            return sb.toString();
        }

        public int[] getSegCorrect() {
            return segCorrect;
        }

        public int[] getSegWrong() {
            return segWrong;
        }

        public int[] getGoldTotal() {
            return goldTotal;
        }
    }
}
