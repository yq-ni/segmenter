package cn.cantonese.segmenter.data;

public interface DataIterator<T> extends AutoCloseable{
    boolean hasNext() throws Exception;
    T next() throws Exception;
}
