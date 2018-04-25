package pl.edu.agh.ki.io.forganizer.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.morfologik.MorfologikAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;

public class Searcher {

    private String indexPath;
    private Analyzer analyzer;

    public Searcher(String indexPath, String analyzerLanguage) {
        this.indexPath = indexPath;
        if (analyzerLanguage.equals("english")) {
            this.analyzer = new StandardAnalyzer();
        } else if (analyzerLanguage.equals("polish")) {
            this.analyzer = new MorfologikAnalyzer();
        }
    }

    public Document[] searchField(String fieldName,
                                  String queryString,
                                  int resultsNum) throws Exception {

        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        QueryParser queryParser = new QueryParser(fieldName, analyzer);
        Query query = queryParser.parse(queryString);
        ScoreDoc[] hits = indexSearcher.search(query, resultsNum).scoreDocs;
        Document[] results = new Document[hits.length];
        for (int i = 0; i < hits.length; i++) {
            results[i] = indexSearcher.doc(hits[i].doc);
        }
        return results;
    }

}