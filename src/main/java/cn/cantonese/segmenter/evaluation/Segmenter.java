package cn.cantonese.segmenter.evaluation;

import java.util.List;

public interface Segmenter {
    List<String> processSentence(String sentence);
    String getId();
}
