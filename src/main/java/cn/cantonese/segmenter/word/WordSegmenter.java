package cn.cantonese.segmenter.word;

import cn.cantonese.segmenter.dictionary.WordDictionary;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordSegmenter {
    private WordDictionary wordDictionary;
    private Pattern reSkip = Pattern.compile("((\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+)*)|\\d+\\.\\d+|[a-zA-Z0-9]+)");

    public WordSegmenter() {
        wordDictionary = new WordDictionary();
    }

    public WordSegmenter(WordDictionary wordDictionary) {
        this.wordDictionary = wordDictionary;
    }

    public WordSegmenter(String wordDictionaryPath) {
        this.wordDictionary = new WordDictionary(wordDictionaryPath);
    }

    public List<String> bmm(String sentence) {
        LinkedList<String> result = new LinkedList<>();
        int maxWordLength = wordDictionary.getMaxWordLength();

        int sentenceLen = sentence.length(), wordLen;
        String word;
        while (sentenceLen > 0) {
            wordLen = maxWordLength;
            if (wordLen > sentenceLen) {
                wordLen = sentenceLen;
            }
            while (wordLen > 0) {
                word = sentence.substring(sentenceLen - wordLen, sentenceLen);
                if ((wordLen == 1) || wordDictionary.exist(word)) {
                    sentenceLen -= wordLen;
                    result.add(word);
                    break;
                }
                else {
                    wordLen--;
                }
            }
        }
        Collections.reverse(result);
        return result;
    }

    // TODO: 2017/5/18 it may not right to use re to match all English word firstly, because some words may contain both Chinese and English
    public List<String> bmmProcessEnglish(String sentence) {
        LinkedList<String> result = new LinkedList<>();
        Matcher mat = reSkip.matcher(sentence);
        int offset = 0;
        while (mat.find()) {
            if (mat.start() > offset) {
                result.addAll(bmm(sentence.substring(offset, mat.start())));
            }
            result.add(mat.group());
            offset = mat.end();
        }
        if (offset < sentence.length())
            result.addAll(bmm(sentence.substring(offset)));
        return result;
    }
}
