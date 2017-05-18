package cn.cantonese.segmenter.data;

public interface SafeDataIterator<T> {
    boolean hasNext();
    T next();
}
