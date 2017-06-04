package cn.cantonese.segmenter.word;

import cn.cantonese.segmenter.dictionary.WordDictionary;
import cn.cantonese.segmenter.evaluation.Segmenter;

import java.util.List;

public class WordSeg implements Segmenter {

    private WordSegmenter wordSegmenter;

    public WordSeg() {
        wordSegmenter = new WordSegmenter();
    }

    public WordSeg(WordDictionary wordDictionary) {
        wordSegmenter = new WordSegmenter(wordDictionary);
    }

    public WordSeg(String wordDictionaryPath) {
        wordSegmenter = new WordSegmenter(wordDictionaryPath);
    }

    @Override
    public List<String> processSentence(String sentence) {
        return wordSegmenter.bmmProcessEnglish(sentence);
    }

    @Override
    public String getId() {
        return "WordSeg";
    }
}
