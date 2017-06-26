package cn.cantonese.segmenter.evaluation;

import cn.cantonese.segmenter.data.Data;
import cn.cantonese.segmenter.data.FileData;
import cn.cantonese.segmenter.hmm.Generate;
import cn.cantonese.segmenter.hmm.HMMSeg;
import cn.cantonese.segmenter.hmm.HMMTnT;
import cn.cantonese.segmenter.word.WordSeg;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by nyq on 2017/6/25.
 */
public class OuterTest {
    private static String emit = "temp/emit.txt";
    private static String start = "temp/start.txt";
    private static String trans = "temp/trans.txt";
    private static String words = "temp/words.txt";
    private static String movie = "temp/movie.txt";
    private static String hkcanor = "temp/hkcanor.txt";
    private static String children = "temp/children.txt";

    public static void main(String[] args) {
        String[] ds = {movie, hkcanor, children};
        for (int i = 0; i < ds.length; i++) {
            for (int j = 0; j < ds.length; j++) {
                if (i == j) continue;
                score(ds[i], ds[j]);
            }
        }
    }


    public static void score(String trainDataPath, String testDataPath) {
        Data<String> trainData = new FileData(trainDataPath);
        Data<String> testData = new FileData(testDataPath);
        Generate.train(trainData, trans, start, emit);
        HMMSeg hmmSeg = new HMMSeg(trans, start, emit);
        WordSeg wordSeg = new WordSeg(words);
        HMMTnT hmmTnT = new HMMTnT();
        hmmTnT.train(trainData);

        Map<String,List<Statistics>> res = Util.score(testData, hmmSeg, wordSeg, hmmTnT);
        String tr = new File(trainDataPath).getName().split("\\.")[0];
        String te = new File(testDataPath).getName().split("\\.")[0];
        Util.save("temp/outer/"+tr+"-"+te, res);
    }
}
