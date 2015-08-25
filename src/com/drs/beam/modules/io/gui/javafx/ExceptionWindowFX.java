/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.modules.io.gui.javafx;

import com.drs.beam.modules.io.gui.Gui;

import java.util.StringJoiner;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/*
 * Window for showing exceptions.
 */
public class ExceptionWindowFX implements Runnable{
    // Fields =============================================================================
    private final Exception exc;
    private final boolean isCritical;
    
    // Constructors =======================================================================
    public ExceptionWindowFX(Exception exc, boolean isCritical){    
        this.exc = exc;  
        this.isCritical = isCritical;
    }
    
    private String getTextFromException(Exception e){
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(e.getMessage());
        for(StackTraceElement elem : e.getStackTrace()){
            joiner.add(elem.toString());
        }
        return joiner.toString();
    }

    // Methods ============================================================================
    @Override
    public void run() {
        Stage stage = new Stage();
        VBox mainVBox = new VBox(15); 
        mainVBox.setPadding(new Insets(15, 15, 15, 15));
        mainVBox.setAlignment(Pos.TOP_CENTER);
        
        HBox hBox = new HBox(15);
        hBox.setMinWidth(300);
        hBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox messageTextBox = new VBox();
        messageTextBox.setAlignment(Pos.TOP_LEFT);        
        
        ImageView messagePic = new ImageView(new Image("file:"+Gui.IMAGES_LOCATION+"exception.jpeg"));
        
        Label picture = new Label("", messagePic); 
        
        Label messageLabel = new Label(); 
        messageLabel.setFont(new Font(12.0));
        messageLabel.setWrapText(true);
        messageLabel.setPadding(new Insets(0, 0, 0, 0));
        
        messageLabel.setText(getTextFromException(exc));
        
        messageTextBox.getChildren().addAll(messageLabel);
        hBox.getChildren().addAll(picture, messageTextBox);
        
        Button button = new Button("Ok");
        button.setFont(new Font(14.0));
        button.setMinWidth(100);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(isCritical){
                    System.exit(1);
                } else {
                    stage.close();
                }                
            }
        });
        
        mainVBox.getChildren().addAll(hBox, button);
        
        Scene scene = new Scene(mainVBox);
        
        stage.setTitle("Message");
        stage.getIcons().add(new Image("file:"+Gui.IMAGES_LOCATION+"exception_ico.jpeg"));
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();
    }    
}
