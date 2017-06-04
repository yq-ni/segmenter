package cn.cantonese.segmenter.word;

import cn.cantonese.segmenter.Utils;
import cn.cantonese.segmenter.data.Data;
import cn.cantonese.segmenter.data.DataIterator;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GenerateWord {

    public static void generateWords(Data<String> trainData, String wordsPath) {
        String chinese = "data/Chinese.txt";
        Set<Character> chineseSet = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(chinese))) {
            while (reader.ready()) {
                chineseSet.add(reader.readLine().charAt(0));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Integer> wordMap = new HashMap<>();
        try (DataIterator<String> iterator = trainData.dataIterator()) {
            while (iterator.hasNext()) {
                String sentence = iterator.next();
                String[] ws = sentence.split(Utils.SEG_DELIMITER);
                for (String word : ws) {
                    if (word.length() > 1) {
                        for (int i = 0; i < word.length(); i++) {
                            char c = word.charAt(i);
                            if (chineseSet.contains(c) || (c >= 0x4E00 && c <= 0x9FA5)) {
                                if (wordMap.containsKey(word)) {
                                    wordMap.put(word, wordMap.get(word)+1);
                                }
                                else {
                                    wordMap.put(word, 1);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(wordsPath))) {
            for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
                writer.write(entry.getKey());
                writer.write(Utils.PART_DELIMITER);
                writer.write(entry.getValue()+"");
                writer.write('\n');
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
