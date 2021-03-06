package pl.edu.agh.ki.io.forganizer.model;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import pl.edu.agh.ki.io.forganizer.utils.Const;

public class Converter {

    public File convertDocToFile(Document doc) {
        String name = doc.get(Const.fileNameProperty);
        String path = doc.get(Const.filePathProperty);
        Long size = Long.parseLong(doc.get(Const.fileSizeProperty));
        String fileType = doc.get(Const.fileTypeProperty);
        File file = new File(name, path, size, fileType);

        if (doc.get(Const.commentFileProperty) != null) {
            file.setComment(doc.get(Const.commentFileProperty));
        }
        if (doc.get(Const.tagFileProperty) != null) {
            file.setTag(doc.get(Const.tagFileProperty));
        }
        return file;
    }

    public Document convertFileToDoc(File file) {
        Document document = new Document();
        document.add(new StringField(Const.fileNameProperty, file.getName(), Field.Store.YES));
        document.add(new StringField(Const.filePathProperty, file.getPath(), Field.Store.YES));
        document.add(new StringField(Const.fileSizeProperty, String.valueOf(file.getSize()), Field.Store.YES));
        document.add(new StringField(Const.fileTypeProperty, file.getFileType(), Field.Store.YES));

        if (file.getTag() != null) {
            document.add(new StringField(Const.tagFileProperty, file.getTag(), Field.Store.YES));
        }
        if (file.getComment() != null) {
            document.add(new StringField(Const.commentFileProperty, file.getComment(), Field.Store.YES));
        }

        return document;
    }
}
