package cn.cantonese.segmenter.evaluation;


import cn.cantonese.segmenter.Utils;
import cn.cantonese.segmenter.data.Data;
import cn.cantonese.segmenter.data.FileData;
import cn.cantonese.segmenter.hmm.HMMSegmenter;
import cn.cantonese.segmenter.hmm.HMMTnT;
import cn.cantonese.segmenter.word.WordSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScoreTest {
    public static void main(String[] args) {
        score();
    }

    public static void score() {
        List<String> files = new ArrayList<>();
        for (int i = 1; i <= 14; i++) {
            files.add("data/movies/movie_"+i+".txt");
        }
        HMMTnT hmmTnT = new HMMTnT();
        hmmTnT.train(new FileData(files));
        files.add("data/chat/chat.txt");
        Data<String> data = new FileData(files);
        Score score = Score.builder()
                .withSegmenter(hmmTnT)
                .withSegmenter(new HMMSeg())
                .withSegmenter(new WordSeg())
                .withData(data)
                .withEvaluation(new DefaultEvaluation())
                .withEvaluation(new BMES())
                .withEvaluation(new SentencePerfect())
                .build();
        System.out.println(score.calStatistics());
//        System.out.println(score.performanceRun(5));
    }

}


class JiebaSeg implements Segmenter {
    private JiebaSegmenter jiebaSegmenter = new JiebaSegmenter();
    @Override
    public List<String> processSentence(String sentence) {
        return jiebaSegmenter.sentenceProcess(sentence);
    }

    @Override
    public String getId() {
        return "JiebaSeg";
    }
}

class HMMSeg implements Segmenter {
    private HMMSegmenter hmmSegmenter = new HMMSegmenter();
    @Override
    public List<String> processSentence(String sentence) {
        return hmmSegmenter.processEnglish(sentence);
    }

    @Override
    public String getId() {
        return "HMMSeg";
    }
}

class WordSeg implements Segmenter {
    private WordSegmenter wordSegmenter = new WordSegmenter();
    @Override
    public List<String> processSentence(String sentence) {
        return wordSegmenter.bmmProcessEnglish(sentence);
    }

    @Override
    public String getId() {
        return "WordSeg";
    }
}
