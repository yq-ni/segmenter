package cn.cantonese.segmenter.evaluation;

import cn.cantonese.segmenter.data.Data;
import cn.cantonese.segmenter.data.DataIterator;
import cn.cantonese.segmenter.data.FileData;
import cn.cantonese.segmenter.data.SafeDataIterator;
import cn.cantonese.segmenter.hmm.Generate;
import cn.cantonese.segmenter.hmm.HMMSeg;
import cn.cantonese.segmenter.hmm.HMMTnT;
import cn.cantonese.segmenter.word.WordSeg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by nyq on 2017/6/25.
 */
public class InnerTest {
    private static String emit = "temp/emit.txt";
    private static String start = "temp/start.txt";
    private static String trans = "temp/trans.txt";
    private static String words = "temp/words.txt";
    private static String movie = "temp/movie.txt";
    private static String hkcanor = "temp/hkcanor.txt";
    private static String children = "temp/children.txt";

    public static void main(String[] args) {
        int time = 5;
        String[] dataPaths = {movie, hkcanor, children};
        double[] trainRates = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        for (String dataPath : dataPaths) {
            for (double trainRate : trainRates) {
                score(dataPath, trainRate, time);
            }
        }
    }


    public static void score(String dataPath, double trainRate, int time) {
        Map<String,List<Statistics>> res = getRes(dataPath, trainRate);
        for (int i = 1; i < time; i++) {
            Map<String,List<Statistics>> temp = getRes(dataPath, trainRate);
            for (Map.Entry<String, List<Statistics>> entry : res.entrySet()) {
                List<Statistics> list = entry.getValue();
                for (int j = 0; j < list.size(); j++) {
                    list.get(j).merge(temp.get(entry.getKey()).get(j));
                }
            }
        }
        Util.save("temp/inner/"+new File(dataPath).getName().split("\\.")[0] + "/" + trainRate, res);
    }

    public static Map<String, List<Statistics>> getRes(String dataPath, double trainRate) {
        Data<String>[] trainTest = randomSplit(dataPath, trainRate);
        Data<String> trainData = trainTest[0];
        Data<String> testData = trainTest[1];
        Generate.train(trainData, trans, start, emit);
        HMMSeg hmmSeg = new HMMSeg(trans, start, emit);
        WordSeg wordSeg = new WordSeg(words);
        HMMTnT hmmTnT = new HMMTnT();
        hmmTnT.train(trainData);

        return Util.score(testData, hmmSeg, wordSeg, hmmTnT);
    }

    public static Data<String>[] randomSplit(String dataPath, double trainRate) {
        Data[] trainTest = new Data[2];
        List<String> test = readInList(dataPath);
        List<String> train = new LinkedList<>();
        int n = (int)(trainRate * test.size());
        Random random = new Random();
        while (n > 0) {
            train.add(test.remove(random.nextInt(test.size())));
            n--;
        }
        System.out.println("train: " + train.size());
        System.out.println("test: " + test.size());
        trainTest[0] = new ListFile(train);
        trainTest[1] = new ListFile(test);
        return trainTest;
    }

    private static List<String> readInList(String dataPath) {
        List<String> list = new LinkedList<>();
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(dataPath))) {
            while (bufferedReader.ready()) {
                list.add(bufferedReader.readLine());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }



    private static class ListFile implements Data<String> {
        private List<String> list;
        ListFile(List<String> list) {
            this.list = list;
        }

        @Override
        public DataIterator<String> dataIterator() {
            return new DataIterator<String>() {
                Iterator<String> iterator = list.iterator();
                @Override
                public boolean hasNext() throws Exception {
                    return iterator.hasNext();
                }

                @Override
                public String next() throws Exception {
                    return iterator.next();
                }

                @Override
                public void close() throws Exception {

                }
            };
        }

        @Override
        public SafeDataIterator<String> safeDataIterator() {
            return new SafeDataIterator<String>() {
                Iterator<String> iterator = list.iterator();
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public String next() {
                    return iterator.next();
                }
            };
        }
    }
}
