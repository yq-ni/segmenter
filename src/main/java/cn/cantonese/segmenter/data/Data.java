package cn.cantonese.segmenter.data;

public interface Data<T> {
    DataIterator<T> dataIterator();
    SafeDataIterator<T> safeDataIterator();
}
