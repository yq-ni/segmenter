package cn.cantonese.segmenter.evaluation;

import cn.cantonese.segmenter.Utils;
import cn.cantonese.segmenter.data.Data;
import cn.cantonese.segmenter.data.DataIterator;
import cn.cantonese.segmenter.data.SafeDataIterator;

import java.util.*;

public class Score {

    public Score() {

    }

    public void performanceRun(List<Segmenter> segmenters, Data<String> data, int time) {
        for (Segmenter segmenter : segmenters) {
            long total = performanceRun(segmenter, data, time);
            System.out.printf("segmenter: %s, cost: %f", segmenter, total);
        }
    }

    public long performanceRun(Segmenter segmenter, Data<String> data, int time) {
        long total = 0;
        for (int i = 0; i < time; i++) {

            SafeDataIterator<String> iterator = data.safeDataIterator();
            String test;
            long start = System.currentTimeMillis();
            while (iterator.hasNext()) {
                test = iterator.next();
                segmenter.processSentence(test);
            }
            total += System.currentTimeMillis() - start;
        }
        return total / time;
    }

    public List<Statistics> calStatistics(Segmenter segmenter, Data<String> data) {
        return calStatistics(segmenter, data, Arrays.asList(new DefaultEvaluation(), new BMES()));
    }

    public List<Statistics> calStatistics(Segmenter segmenter, Data<String> data, List<Evaluation> evaluations) {
        return calStatistics(Arrays.asList(segmenter), data, evaluations).get(segmenter.getId());
    }

    public Statistics calStatistics(Segmenter segmenter, Data<String> data, Evaluation evaluation) {
        return calStatistics(Arrays.asList(segmenter), data, Arrays.asList(evaluation)).get(segmenter.getId()).get(0);
    }

    public Map<String, List<Statistics>> calStatistics(List<Segmenter> segmenters, Data<String> data, List<Evaluation> evaluations) {
        List<Statistics> statisticsList;
        Map<String, List<Statistics>> map = new HashMap<>();
        for (Segmenter segmenter : segmenters) {
            statisticsList = new ArrayList<>(evaluations.size());
            for (int i = 0; i < evaluations.size(); i++) statisticsList.add(null);
            map.put(segmenter.getId(), statisticsList);
        }
        String gold, test;
        Statistics tempStatistics;
        try (DataIterator<String> iterator = data.dataIterator()) {
            while (iterator.hasNext()) {
                gold = iterator.next();
                test = Utils.goldToSource(gold);
                for (Segmenter segmenter : segmenters) {
                    String segTest = Utils.tokensToGold(segmenter.processSentence(test));
                    statisticsList = map.get(segmenter.getId());
                    for (int i = 0; i < statisticsList.size(); i++) {
                        tempStatistics = evaluations.get(i).calStatistics(gold, segTest);
                        if (statisticsList.get(i) == null) {
                            statisticsList.set(i, tempStatistics);
                        } else {
                            statisticsList.get(i).merge(tempStatistics);
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private interface ProcessTwo {
        void process(String gold, String test);
    }

}
