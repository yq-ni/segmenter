package cn.cantonese.segmenter.evaluation;

import cn.cantonese.segmenter.Utils;
import cn.cantonese.segmenter.data.Data;
import cn.cantonese.segmenter.data.DataIterator;
import cn.cantonese.segmenter.data.FileData;
import cn.cantonese.segmenter.data.SafeDataIterator;
import cn.cantonese.segmenter.hmm.Generate;
import cn.cantonese.segmenter.hmm.HMMSeg;
import cn.cantonese.segmenter.hmm.HMMTnT;
import cn.cantonese.segmenter.word.WordSeg;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by nyq on 2017/6/25.
 */
public class FittingTest {

    private static String emit = "temp/emit.txt";
    private static String start = "temp/start.txt";
    private static String trans = "temp/trans.txt";
    private static String words = "temp/words.txt";

    public static void main(String[] args) {
        score("temp/movie.txt");
        score("temp/hkcanor.txt");
        score("temp/children.txt");
    }


    public static void score(String dataPath) {
        Data<String> data = new FileData(dataPath);
        Generate.train(data, trans, start, emit);
        HMMSeg hmmSeg = new HMMSeg(trans, start, emit);
        WordSeg wordSeg = new WordSeg(words);
        HMMTnT hmmTnT = new HMMTnT();
        hmmTnT.train(data);

        Map<String,List<Statistics>> res = Util.score(data, hmmSeg, wordSeg, hmmTnT);
        Util.save("temp/fitting/"+new File(dataPath).getName().split("\\.")[0], res);
    }
}
