package com.sadatrafsanjani.scraper;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class PageController implements Initializable {
    
    @FXML
    private WebView webView;
    private WebEngine engine;
    private String page;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    
        engine = webView.getEngine();
    }
    
    public void setPage(String page){
        this.page = page;
        setData();
    }
    
    private void setData(){
        
        try {
            Document document = Jsoup.connect(page).get();
            engine.loadContent(document.html());
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
