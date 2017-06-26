package cn.cantonese.segmenter.evaluation;

import cn.cantonese.segmenter.Utils;
import cn.cantonese.segmenter.data.Data;
import cn.cantonese.segmenter.data.DataIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by nyq on 2017/6/25.
 */
public class Util {

    public static Map<String,List<Statistics>> score(Data<String> testData, Segmenter...segmenters) {
        Score.ScoreBuilder sb = Score.builder();
        for (Segmenter segmenter : segmenters) {
            sb.withSegmenter(segmenter);
        }
        Score score = sb.withData(testData)
                .withEvaluation(new DefaultEvaluation())
                .withEvaluation(new BMES())
                .withEvaluation(new SentencePerfect())
                .build();
        return score.calStatistics();
    }

    public static Map<String,Object> size(Data<String> data) {
        int charCount = 0, wordCount = 0;
        try (DataIterator<String> dataIterator = data.dataIterator()){
            while (dataIterator.hasNext()) {
                String sentence = dataIterator.next();
                for (String word :sentence.split(Utils.SEG_DELIMITER)) {
                    charCount += word.length();
                    wordCount++;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> res = new HashMap<>();
        res.put("charCount", charCount);
        res.put("wordCount", wordCount);
        return res;
    }

    public static void save(String dirPath, Map<String,List<Statistics>> res) {
        new File(dirPath).mkdirs();
        for (Map.Entry<String,List<Statistics>> entry : res.entrySet()) {
            String filePath = dirPath + File.separatorChar + entry.getKey();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))){
                for (Statistics statistics : entry.getValue()) {
                    if (statistics instanceof DefaultEvaluation.DefaultStatistics) {
                        writer.write("PRF\n");
                        DefaultEvaluation.DefaultStatistics s = (DefaultEvaluation.DefaultStatistics) statistics;
                        writer.write(s.calP() + "," + s.calR() + "," + s.calF() + "\n");
                    }
                    else if (statistics instanceof BMES.BMESStatistics) {
                        writer.write("BMES\n");
                        BMES.BMESStatistics s = (BMES.BMESStatistics) statistics;
                        for(int i = 0; i < BMES.BMESStatistics.STATE_NUM; i++){
                            double P = s.getSegCorrect()[i] * 1./ (s.getSegCorrect()[i] + s.getSegWrong()[i]);
                            double R = s.getSegCorrect()[i] * 1./ s.getGoldTotal()[i];
                            double F = P * R * 2 / (P + R);
                            writer.write(P + "," + R + "," + F + "\n");
                        }
                    }
                    else {
                        writer.write("SentencePerfect\n");
                        SentencePerfect s = (SentencePerfect) statistics;
                        writer.write((s.getPerfect()*1. / s.getTotal()) + "\n");
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
