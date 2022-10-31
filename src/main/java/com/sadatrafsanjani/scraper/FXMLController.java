package com.sadatrafsanjani.scraper;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FXMLController implements Initializable {

    private static final String DIRECTORY = System.getProperty("user.home") + "\\Desktop\\";

    @FXML
    private Button downloadButton, refreshButton, exportButton;
    @FXML
    private TextArea linkArea, imageArea, emailArea;
    @FXML
    private TextField textField;
    @FXML
    private CheckBox hyperlinkCheck, imageCheck, emailCheck;
    private Elements elements;
    private String anchorTags, emails;
    private Service<Void> linkTask, imageTask, emailTask;
    @FXML
    private ListView listView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        anchorTags = "";

        listView.setOnMouseClicked((MouseEvent event) -> {

            String page = String.valueOf(listView.getSelectionModel().getSelectedItem());
            openPage(page);
        });
    }

    private void openPage(String page) {

        try {
            FXMLLoader loader = new FXMLLoader((getClass().getResource("/fxml/Page.fxml")));
            PageController controller = new PageController();
            loader.setController(controller);
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            controller.setPage(page);
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void handleDownload(ActionEvent event) {

        String url = textField.getText().trim();

        if (!isValid(url)) {
            url = "http://" + url;
        }

        if (hyperlinkCheck.isSelected() || imageCheck.isSelected() || emailCheck.isSelected()) {

            if (hyperlinkCheck.isSelected() || imageCheck.isSelected()) {
                scrap(url);
                if (hyperlinkCheck.isSelected()) {
                    downloadLinks();
                }
                if (imageCheck.isSelected()) {
                    downloadImages();
                }
            }

            if (emailCheck.isSelected()) {
                scrapEmail(url);
                downloadEmails();
            }

        }

    }

    private void scrap(String url) {

        System.setProperty("http.proxyhost", "127.0.0.1");
        System.setProperty("http.proxyport", "3128");

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .ignoreContentType(true)
                    .timeout(5000)
                    .get();
            elements = doc.select("a[href]");
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void scrapEmail(String url) {

        System.setProperty("http.proxyhost", "127.0.0.1");
        System.setProperty("http.proxyport", "3128");

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .ignoreContentType(true)
                    .timeout(5000).get();
            emails = doc.text();
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void downloadLinks() {

        linkTask = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {

                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {

                        StringBuilder builder = new StringBuilder("");

                        for (Element a : elements) {

                            String anchor = a.attr("abs:href").trim();

                            if ((anchor != null) || !(anchor.equals(""))) {

                                listView.getItems().add(anchor);
                                builder.append(anchor).append("\n");
                                updateMessage(builder.toString());
                                anchorTags += anchor + ", ";
                            }
                        }

                        return null;
                    }
                };
            }
        };

        linkTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                linkArea.textProperty().unbind();
            }
        });

        linkArea.textProperty().bind(linkTask.messageProperty());
        linkTask.restart();
    }

    private void downloadImages() {

        imageTask = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {

                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {

                        StringBuilder builder = new StringBuilder("");

                        for (Element a : elements) {

                            String anchor = a.attr("abs:href").trim();

                            if (getExt(anchor).contains("jpg") || getExt(anchor).contains("png") || getExt(anchor).contains("gif")) {
                                if ((anchor != null) || !(anchor.equals(""))) {
                                    builder.append(anchor).append("\n");
                                    updateMessage(builder.toString());
                                }
                            }
                        }
                        return null;
                    }
                };
            }
        };

        imageTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                imageArea.textProperty().unbind();
            }
        });

        imageArea.textProperty().bind(imageTask.messageProperty());
        imageTask.restart();
    }

    private void downloadEmails() {

        emailTask = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {

                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {

                        StringBuilder builder = new StringBuilder("");

                        Pattern p = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
                        Matcher matcher = p.matcher(emails);
                        Set<String> mails = new HashSet<>();
                        while (matcher.find()) {
                            mails.add(matcher.group());
                        }

                        for (String m : mails) {
                            builder.append(m).append("\n");
                            updateMessage(builder.toString());
                        }

                        return null;
                    }
                };
            }
        };

        emailTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                emailArea.textProperty().unbind();
            }
        });

        emailArea.textProperty().bind(emailTask.messageProperty());
        emailTask.restart();
    }

    private String getExt(String s) {

        if (s.lastIndexOf(".") != -1 && s.lastIndexOf(".") != 0) {
            return s.substring(s.lastIndexOf(".") + 1).toLowerCase();
        } else {
            return "";
        }
    }

    @FXML
    public void handleExport(ActionEvent event) {

        try {
            String TIMESTAMP = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String URL = DIRECTORY + TIMESTAMP + "_urls.txt";

            try (Writer urls = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(URL), "UTF-8"))) {
                urls.write(anchorTags);
                urls.close();
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        confirmationDialog();
        resetAll();
    }

    @FXML
    public void handleRefresh(ActionEvent event) {
        resetAll();
    }

    private void resetAll() {

        textField.setText("");
        linkArea.setText("");
        imageArea.setText("");
        emailArea.setText("");
        anchorTags = "";
        hyperlinkCheck.setSelected(false);
        imageCheck.setSelected(false);
        emailCheck.setSelected(false);
        listView.getItems().clear();
    }

    private void confirmationDialog() {

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Save");
        alert.setHeaderText("Results:");
        alert.setContentText("Data exported successfully!");
        alert.showAndWait();
    }

    private boolean isValid(String url) {

        return url.contains("http://") || url.contains("https://");
    }

}
