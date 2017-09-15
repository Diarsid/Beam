/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.gui.javafx.window;

import java.util.List;
import java.util.StringJoiner;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import diarsid.beam.core.application.gui.javafx.WindowController;
import diarsid.beam.core.application.gui.javafx.WindowResources;

/**
 *
 * @author Diarsid
 */
class PopupWindow extends BeamWindow implements Runnable {
    
    private final String title;
    private final List<String> message;
    
    PopupWindow(
            String title,
            List<String> message, 
            WindowResources resources, 
            WindowController controller) {
        
        super(resources, controller);
        this.title = title;
        this.message = message;
    }
        
    private String getTextFromMessage(List<String> strings) {        
        StringJoiner joiner = new StringJoiner("\n");
        for(String line : strings){
            joiner.add(line);
        }
        return joiner.toString();
    }
    
    @Override
    public void run() {
        prepareStage();
        setContent(createMainContent());
        setTitle(this.title); 
        if (title.equals("Error")) {
            setErrorIcon();
        } else {
            setMessageIcon();
        }
        showThis();
    }    
    
    private Pane createMainContent() {
        VBox mainVBox = new VBox(15); 
        mainVBox.setPadding(new Insets(15, 15, 0, 15));
        mainVBox.setAlignment(Pos.TOP_CENTER);
        
        HBox hBox = new HBox(15);
        hBox.setMinWidth(300);
        hBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox messageTextBox = new VBox();
        messageTextBox.setAlignment(Pos.TOP_LEFT);
        
        Label picture;
        if (title.equals("Error")) {
            picture = new Label("", new ImageView(getErrorImage())); 
        } else {
            picture = new Label("", new ImageView(getMessageImage())); 
        }
        
        Label messageLabel = new Label(); 
        messageLabel.setWrapText(true);
        messageLabel.setPadding(new Insets(0, 0, 0, 0));
        
        messageLabel.setText(this.getTextFromMessage(this.message));
        
        messageTextBox.getChildren().addAll(messageLabel);
        hBox.getChildren().addAll(picture, messageTextBox);
        
        mainVBox.getChildren().addAll(hBox, newOkButton("OK"));  
        return mainVBox;
    }
}
