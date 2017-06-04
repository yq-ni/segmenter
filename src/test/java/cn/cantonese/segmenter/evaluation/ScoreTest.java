package cn.cantonese.segmenter.evaluation;


import cn.cantonese.segmenter.Utils;
import cn.cantonese.segmenter.data.Data;
import cn.cantonese.segmenter.data.DataIterator;
import cn.cantonese.segmenter.data.FileData;
import cn.cantonese.segmenter.hmm.Generate;
import cn.cantonese.segmenter.hmm.HMMSeg;
import cn.cantonese.segmenter.hmm.HMMSegmenter;
import cn.cantonese.segmenter.hmm.HMMTnT;
import cn.cantonese.segmenter.word.GenerateWord;
import cn.cantonese.segmenter.word.WordSeg;
import cn.cantonese.segmenter.word.WordSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter;

import java.io.*;
import java.util.*;

public class ScoreTest {

    public static void main(String[] args) {
        String emit = "temp/emit.txt";
        String start = "temp/start.txt";
        String trans = "temp/trans.txt";
        String words = "temp/words.txt";
        Data<String> trainData = getTrainData();
        Data<String> testData = getTestData();
        GenerateWord.generateWords(trainData, words);
        Generate.train(trainData, trans, start, emit);
        System.out.printf("trainData size: %d\n", size(trainData));
        System.out.printf("testData size: %d\n", size(testData));

        HMMSeg hmmSeg = new HMMSeg(trans, start, emit);
        WordSeg wordSeg = new WordSeg(words);
        HMMTnT hmmTnT = new HMMTnT();
        hmmTnT.train(trainData);

        score(testData, hmmSeg, wordSeg, hmmTnT);
    }

    public static Data<String> getTrainData() {
        LinkedList<String> files = new LinkedList<>();
        for (int i = 1; i <= 14; i++) {
            files.add("data/movies/movie_"+i+".txt");
        }
        files.add("data/chat/chat.txt");
        return new FileData(files);
    }

    public static Data<String> getTestData() {
        LinkedList<String> files = new LinkedList<>();
        for (int i = 1; i <= 14; i++) {
            files.add("data/movies/movie_"+i+".txt");
        }
        files.add("data/chat/chat.txt");
        return new FileData(files);
    }

    public static void score(Data<String> testData, Segmenter...segmenters) {
        Score.ScoreBuilder sb = Score.builder();
        for (Segmenter segmenter : segmenters) {
            sb.withSegmenter(segmenter);
        }
        Score score = sb.withData(testData)
                .withEvaluation(new DefaultEvaluation())
                .withEvaluation(new BMES())
                .withEvaluation(new SentencePerfect())
                .build();
        System.out.println(score.calStatistics());
    }

    public static int size(Data<String> data) {
        int count = 0;
        try (DataIterator<String> dataIterator = data.dataIterator()){
            while (dataIterator.hasNext()) {
                String sentence = dataIterator.next();
                for (String word :sentence.split(Utils.SEG_DELIMITER)) {
                    count += word.length();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
}
