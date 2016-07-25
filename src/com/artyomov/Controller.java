package com.artyomov;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class Controller {

    @FXML
    private MenuItem menuExit;
    @FXML
    private MenuItem menuAbout;
    @FXML
    private MenuItem menuClear;
    @FXML
    private Button btnOpenSourceFile;
    @FXML
    private Button btnOpenCodedFile;
    @FXML
    private TextArea taCodedText;
    @FXML
    private TextArea taSourceText;
    @FXML
    private TextField tfCompressedValue;

    private LZ77 lz;

    @FXML
    public void initialize() {
        lz = new LZ77();

        menuExit.setOnAction(actionEvent -> Platform.exit());

        menuAbout.setOnAction(actionEvent -> {
            showDialog(Alert.AlertType.INFORMATION, "Author", "Vlad Artyomov\nDonNTU");
        });

        menuClear.setOnAction(actionEvent -> {
            taCodedText.clear();
            taSourceText.clear();
            tfCompressedValue.clear();
        });

        btnOpenSourceFile.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Source text file");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(null);
            DecimalFormat df = new DecimalFormat("0.00");

            if (file != null) {
                try {
                    lz.compress(file.getPath());
                    taSourceText.setText(readFile(file.getPath(), Charset.forName("UTF-8")));
                    String filepath = file.getPath().substring(0, file.getPath().lastIndexOf('.')) + ".lz77";
                    File coded = new File(filepath);
                    taCodedText.setText(readFile(filepath, Charset.forName("UTF-8")));
                    tfCompressedValue.setText(String.valueOf(df.format(((double) file.length() - (double) coded.length()) / (double) file.length() * 100.0)) + "%");

                } catch (IOException e) {
                    System.err.println(e.toString());
                    showDialog(Alert.AlertType.ERROR, "Error", "Wrong source file");
                }
            }
        });

        btnOpenCodedFile.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Output file");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text files (*.lz77)", "*.lz77");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(null);
            DecimalFormat df = new DecimalFormat("0.00");

            if (file != null) {
                try {
                    lz.unCompress(file.getPath());
                    taCodedText.setText(readFile(file.getPath(), Charset.forName("UTF-8")));
                    String filepath = file.getPath().substring(0, file.getPath().lastIndexOf('.')) + ".txt";
                    File decoded = new File(filepath);
                    taSourceText.setText(readFile(filepath, Charset.forName("UTF-8")));
                    tfCompressedValue.setText(String.valueOf(df.format(((double) decoded.length() - (double) file.length()) / (double) decoded.length() * 100.0)) + "%");
                } catch (IOException e) {
                    System.err.println(e.toString());
                    showDialog(Alert.AlertType.ERROR, "Error", "Wrong output file");
                }
            }
        });
    }

    private String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private void showDialog(Alert.AlertType type, String title, String text) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);

        alert.showAndWait();
    }
}
