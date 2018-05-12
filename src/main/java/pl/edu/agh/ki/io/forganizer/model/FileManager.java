package pl.edu.agh.ki.io.forganizer.model;

import org.apache.lucene.store.Directory;
import pl.edu.agh.ki.io.forganizer.search.Indexer;
import pl.edu.agh.ki.io.forganizer.search.Language;
import pl.edu.agh.ki.io.forganizer.search.Searcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileManager {

    private String path;
    private Indexer indexer;
    private Searcher searcher;
    private Converter converter;

    public FileManager(String path, Language language) {
        this.path = path;
        this.indexer = new Indexer(path, language);
        this.searcher = new Searcher(path, language);
        this.converter = new Converter();
    }

    public void addFile(File file, Directory dir) throws IOException {
        indexer.addDoc(converter.convertFileToDoc(file), dir);
    }

    public void updateFile(File file) {

    }

    public void removeFile(File file) {

    }

    List<File> getAllFiles(Directory dir) throws IOException {
        return Arrays.stream(searcher.getAllDocs(dir))
                .map(converter::convertDocToFile).collect(Collectors.toList());
    }


}
