package cn.cantonese.segmenter.evaluation;

public class SentencePerfect implements Evaluation, Statistics{
    private int perfect;
    private int total;
    public SentencePerfect() { this(0, 0); }
    public SentencePerfect(int perfect, int total) { this.perfect = perfect; this.total = total; }
    @Override
    public Statistics calStatistics(String gold, String test) {
        return new SentencePerfect(gold.equals(test) ? 1 : 0, 1);
    }

    @Override
    public void merge(Statistics statistics) {
        if (!(statistics instanceof SentencePerfect)) throw new RuntimeException("not support");;
        this.perfect += ((SentencePerfect) statistics).perfect;
        this.total += ((SentencePerfect) statistics).total;
    }

    @Override
    public String toString() {
        return "\nSentencePerfect:\n{perfect sentence: "+perfect*1./total+"}\n";
    }

    public int getPerfect() {
        return perfect;
    }

    public int getTotal() {
        return total;
    }
}
