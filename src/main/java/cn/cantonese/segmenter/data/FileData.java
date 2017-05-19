package cn.cantonese.segmenter.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FileData implements Data<String> {
    private List<String> filePaths;

    public FileData(String filePath) {
        this(Arrays.asList(filePath));
    }

    public FileData(String[] filePaths) {
        this(Arrays.asList(filePaths));
    }

    public FileData(List<String> filePaths) {
        this.filePaths = new ArrayList<>(filePaths);
        validate();
    }

    // could do more, check whether file exists and so on...
    private void validate() {
        filePaths.removeIf((filePath)->filePath.trim().length()==0);
        if (filePaths.size() == 0) {
            throw new RuntimeException("invalid filePaths");
        }
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

        private void turnToNextFileIfNeed() throws IOException {
            if (index >= filePaths.size()) {
                return;
            }
            if (bufferedReader == null) {
                bufferedReader = new BufferedReader(new FileReader(filePaths.get(index++)));
            }
            else if (!bufferedReader.ready()) {
                bufferedReader.close();
                bufferedReader = new BufferedReader(new FileReader(filePaths.get(index++)));
            }
        }

        public boolean hasNext() throws IOException {
            turnToNextFileIfNeed();
            return bufferedReader != null && bufferedReader.ready();
        }

        public String next() throws IOException{
            turnToNextFileIfNeed();
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
