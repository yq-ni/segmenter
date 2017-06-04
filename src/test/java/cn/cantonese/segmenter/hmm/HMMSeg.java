package cn.cantonese.segmenter.hmm;

import cn.cantonese.segmenter.evaluation.Segmenter;

import java.util.List;

public class HMMSeg implements Segmenter {

    private HMMSegmenter hmmSegmenter;

    public HMMSeg() {
        hmmSegmenter = new HMMSegmenter();
    }

    public HMMSeg(String trans, String start, String emit) {
        hmmSegmenter = new HMMSegmenter(trans, start, emit);
    }

    @Override
    public List<String> processSentence(String sentence) {
        return hmmSegmenter.processEnglish(sentence);
    }

    @Override
    public String getId() {
        return "HMMSeg";
    }
}
