package cn.cantonese.segmenter.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FileData implements Data<String> {
    private List<String> filePaths;

    public FileData(String filePath) {
        this(new String[]{filePath});
    }

    public FileData(String[] filePaths) {
        this.filePaths = new ArrayList<>();
        Collections.addAll(this.filePaths, filePaths);
    }

    public FileData(List<String> filePaths) {
        this.filePaths = new ArrayList<>(filePaths);
    }

    @Override
    public DataIterator<String> dataIterator() {
        return new FileDataIterator();
    }

    @Override
    public SafeDataIterator<String> safeDataIterator() {
        return new FileSafeDataIterator();
    }

    private class FileDataIterator implements DataIterator<String>{
        BufferedReader bufferedReader;
        int index = 0;
        FileDataIterator(){}

        public boolean hasNext() throws IOException {
            if (bufferedReader == null) {
                if (index >= filePaths.size()) {
                    return false;
                }
            }
            else {
                if (bufferedReader.ready()) {
                    return true;
                }
                else {
                    bufferedReader.close();
                    bufferedReader = null;
                }
            }
            if (index < filePaths.size()) {
                bufferedReader = new BufferedReader(new FileReader(filePaths.get(index++)));
                return bufferedReader.ready();
            }
            return false;
        }

        public String next() throws IOException{
            hasNext();
            // TODO: 2017/5/17 terrible
            return bufferedReader.readLine();
        }

        public void close() throws IOException{
            if (bufferedReader != null) bufferedReader.close();
        }
    }

    private class FileSafeDataIterator implements SafeDataIterator<String> {
        Iterator<String> iterator;
        FileSafeDataIterator() {
            List<String> list = new LinkedList<>();
            for (String filePath : filePaths) {
                try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
                    while (bufferedReader.ready()) {
                        list.add(bufferedReader.readLine());
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            iterator = list.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public String next() {
            return iterator.next();
        }
    }

}
