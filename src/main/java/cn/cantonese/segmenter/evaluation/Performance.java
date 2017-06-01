package cn.cantonese.segmenter.evaluation;

public class Performance {
    private int runTimes;
    private long totalCostMs;
    private long totalChar;

    public Performance(int runTimes, long totalCostMs, long totalChar) {
        this.runTimes = runTimes;
        this.totalCostMs = totalCostMs;
        this.totalChar = totalChar;
    }

    public double speed() {
        return 1. * totalChar / totalCostMs;
    }

    public int getRunTimes() {
        return runTimes;
    }

    public void setRunTimes(int runTimes) {
        this.runTimes = runTimes;
    }

    public long getTotalCostMs() {
        return totalCostMs;
    }

    public void setTotalCostMs(long totalCostMs) {
        this.totalCostMs = totalCostMs;
    }

    public long getTotalChar() {
        return totalChar;
    }

    public void setTotalChar(long totalChar) {
        this.totalChar = totalChar;
    }

    @Override
    public String toString() {
        return String.format("Performance{ " +
                "runTimes= %d, " +
                "totalCostMs= %d, " +
                "totalChar=%d, " +
                "speed=%.3f char/ms " +
                "}", runTimes, totalCostMs, totalChar, speed());
    }
}
