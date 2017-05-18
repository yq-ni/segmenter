package cn.cantonese.segmenter.dictionary;

import cn.cantonese.segmenter.Utils;
import cn.cantonese.segmenter.data.Data;
import cn.cantonese.segmenter.data.DataIterator;
import cn.cantonese.segmenter.data.FileData;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class WordDictionary {
    public static final String WORDS = "data/words.txt";
    private Map<String, Integer> wordFreqMap = new HashMap<>();
    private int maxWordLength = 1;
    public WordDictionary() {
        this(WORDS);
    }

    public WordDictionary(String dictPath) {
        loadData(dictPath);
    }

    private void loadData(String dictPath) {
        loadData(new FileData(dictPath));
    }

    private void loadData(Data<String> data) {
        String line, word;
        int freq;
        try (DataIterator<String> iterator = data.dataIterator()){
            while (iterator.hasNext()) {
                line = iterator.next();
                String[] word_freq = line.split(Utils.PART_DELIMITER_REGEX);
                if (word_freq.length > 0) {
                    word = word_freq[0];
//                    if (word.length() == 1) continue;
                    freq = word_freq.length == 1 ? 1 : Integer.valueOf(word_freq[1]);
                    wordFreqMap.put(word, freq);
                    if (word_freq[0].length() > maxWordLength) {
                        maxWordLength = word_freq[0].length();
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.printf("fail to load data %s: %s\n", data, e);
        }
    }

    public int getWordFreq(String word) {
        return wordFreqMap.get(word);
    }

    public boolean exist(String word) {
        return wordFreqMap.containsKey(word);
    }

    public int getMaxWordLength() {
        return maxWordLength;
    }
}
