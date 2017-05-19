package cn.cantonese.segmenter.evaluation;

import cn.cantonese.segmenter.Utils;
import cn.cantonese.segmenter.data.Data;
import cn.cantonese.segmenter.data.DataIterator;
import cn.cantonese.segmenter.data.SafeDataIterator;

import java.util.*;

public class Score {
    private List<Segmenter> segmenters;
    private List<Data<String>> dataList;
    private List<Evaluation> evaluations;

    public Score(ScoreBuilder scoreBuilder) {
        this.segmenters = scoreBuilder.segmenters;
        this.dataList = scoreBuilder.dataList;
        this.evaluations = scoreBuilder.evaluations;
    }

    public static ScoreBuilder builder() {
        return new ScoreBuilder();
    }

    // TODO: 2017/5/19 calculate data size?
    public Map<String, Double> performanceRun(int time) {
        Map<String, Double> timeMap = new HashMap<>();
        for (Segmenter segmenter : segmenters) {
            double total = 0;
            for (int i = 0; i < time; i++) {
                for (Data<String> data : dataList) {
                    SafeDataIterator<String> iterator = data.safeDataIterator();
                    String test;
                    long start = System.currentTimeMillis();
                    while (iterator.hasNext()) {
                        test = iterator.next();
                        segmenter.processSentence(test);
                    }
                    total += System.currentTimeMillis() - start;
                }
            }
            timeMap.put(segmenter.getId(), total / time);
        }
        return timeMap;
    }

    public Map<String, List<Statistics>> calStatistics() {
        List<Statistics> statisticsList;
        Map<String, List<Statistics>> map = new HashMap<>();
        for (Segmenter segmenter : segmenters) {
            statisticsList = new ArrayList<>(evaluations.size());
            for (int i = 0; i < evaluations.size(); i++) statisticsList.add(null);
            map.put(segmenter.getId(), statisticsList);
        }
        String gold, test;
        Statistics tempStatistics;
        for (Data<String> data : dataList) {
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
        }
        return map;
    }

    public static class ScoreBuilder {
        private List<Segmenter> segmenters;
        private List<Data<String>> dataList;
        private List<Evaluation> evaluations;

        public ScoreBuilder() {
            segmenters = new ArrayList<>();
            dataList = new ArrayList<>();
            evaluations = new ArrayList<>();
        }

        public ScoreBuilder withSegmenter(Segmenter segmenter) {
            segmenters.add(segmenter);
            return this;
        }

        public ScoreBuilder withData(Data<String> data) {
            dataList.add(data);
            return this;
        }

        public ScoreBuilder withEvaluation(Evaluation evaluation) {
            evaluations.add(evaluation);
            return this;
        }

        public Score build() {
            return new Score(this);
        }
    }
}
