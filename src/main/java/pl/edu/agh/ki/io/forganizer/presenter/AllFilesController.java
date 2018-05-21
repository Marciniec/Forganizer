package pl.edu.agh.ki.io.forganizer.presenter;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeTableColumn;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import pl.edu.agh.ki.io.forganizer.model.File;
import pl.edu.agh.ki.io.forganizer.model.FileManager;
import pl.edu.agh.ki.io.forganizer.search.Language;
import pl.edu.agh.ki.io.forganizer.utils.Const;
import javafx.beans.value.ChangeListener;
import pl.edu.agh.ki.io.forganizer.utils.SizeConverter;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;

//TODO: Add this controller to controller map at app start
//TODO: should be optimized somehow
public class AllFilesController implements Initializable {

    private static final Logger log = Logger.getLogger(AllFilesController.class);
    private FileChooser fileChooser;
    private FileManager fileManager = new FileManager(Const.pathIndex, Language.ENGLISH);
    private ObservableList<File> filesList;

    @FXML
    private JFXTreeTableView<File> allFileTableView;
    @FXML
    private JFXTreeTableColumn<File, String> fileNameColumn;
    @FXML
    private JFXTreeTableColumn<File, String> filePathColumn;
    @FXML
    private JFXTextField searchField;
    @FXML
    private Label tagLabel;
    @FXML
    private Label sizeLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private Label commentLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.fileChooser = setupFileChooser();
        setupTableView();
        log.info("AllFile Controller initialized");
    }

    //TODO: fix bug, that doesnt show first file when index dir is not created
    private void setupTableView() {
        setupCellValueFactory(fileNameColumn, File::getNameProperty);
        setupCellValueFactory(filePathColumn, File::getPathProperty);
        try (Directory dir = FSDirectory.open(Paths.get(Const.pathIndex))) {
            filesList = fileManager.getAllFiles(dir);
        } catch (IOException e) {
            filesList = FXCollections.observableArrayList();
            log.error(e.getMessage());
        } finally {
            allFileTableView.setRoot(new RecursiveTreeItem<>(
                    filesList,
                    RecursiveTreeObject::getChildren
            ));
        }
        allFileTableView.setShowRoot(false);
        addSelectedItemListener();
        searchField.textProperty().addListener(setupSearchField(allFileTableView));
    }

    //TODO: just for prototyping

    private void addSelectedItemListener() {
        allFileTableView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSelection, newSelection) -> {
                    File selectedFile = newSelection.getValue();
                    sizeLabel.setText(SizeConverter.getReadableFileSize(selectedFile.getSize()));
                    typeLabel.setText(selectedFile.getFileType());
                    tagLabel.setText(selectedFile.getTag());
                    commentLabel.setText(selectedFile.getComment());
                    log.info(selectedFile.getComment());
                });
    }

    private <T> void setupCellValueFactory(JFXTreeTableColumn<File, T> column, Function<File, ObservableValue<T>> mapper) {
        column.prefWidthProperty().bind(allFileTableView.widthProperty().divide(2));
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<File, T> param) -> {
            if (column.validateValue(param) && param.getValue().getValue() != null) {
                return mapper.apply(param.getValue().getValue());
            } else {
                return column.getComputedValue(param);
            }
        });
    }

    private ChangeListener<String> setupSearchField(JFXTreeTableView<File> tableView) {
        return (o, oldVal, newVal) ->
                tableView.setPredicate(fileProp -> {
                    final File file = fileProp.getValue();
                    return file.getName().contains(newVal)
                            || file.getPath().contains(newVal)
                            || file.getComment().contains(newVal);

                });
    }

    //TODO: handle unrecognized files type
    public void addFileButtonOnAction() {
        java.io.File selectedFile = fileChooser.showOpenDialog(null);
        try {
            if (selectedFile != null) {
                String path = selectedFile.getAbsolutePath();
                String fileName = selectedFile.getName();
                File newFile = new File(fileName, path, selectedFile.length(),
                        Files.probeContentType(selectedFile.toPath()));
                fileManager.addFile(
                        newFile,
                        FSDirectory.open(Paths.get(Const.pathIndex))
                );
                filesList.add(newFile);
                log.info(String.format("File path: %s, file name: %s", path, fileName));
            } else {
                log.error("File not specified");
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void addCommentButtonOnAction() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Comment section");
        Optional<String> result = dialog.showAndWait();
        File file = allFileTableView.getSelectionModel()
                .selectedItemProperty()
                .get()
                .getValue();
        try (Directory dir = FSDirectory.open(Paths.get(Const.pathIndex))){
            fileManager.updateFile(file.withComment(result.orElse(null)), dir);
        }catch (IOException e){
            log.error(e);
        }
        commentLabel.setText(result.orElse(null));
    }

    public void addTagButtonOnAction() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Tag section");
        Optional<String> result = dialog.showAndWait();
        File file = allFileTableView.getSelectionModel()
                .selectedItemProperty()
                .get()
                .getValue();
        try (Directory dir = FSDirectory.open(Paths.get(Const.pathIndex))){
            fileManager.updateFile(file.withTag(result.orElse(null)), dir);
        }catch (IOException e){
            log.error(e);
        }
        tagLabel.setText(result.orElse(null));
    }

    //TODO: consider moving FileChose to separate class
    //TODO: enable adding few files in the same time
    private FileChooser setupFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file");
        fileChooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
        return fileChooser;
    }

}


