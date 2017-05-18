package cn.cantonese.segmenter.evaluation;

public interface Evaluation {
    Statistics calStatistics(String gold, String test);
}
