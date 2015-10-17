/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.server.modules.io.gui.javafx;

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
import javafx.stage.WindowEvent;

import com.drs.beam.server.modules.io.gui.GuiWindowsController;

/**
 *
 * @author Diarsid
 */
public class PopupWindow implements Runnable{
    // Fields =============================================================================
    private final String title;
    private final String mainImage;
    private final String iconImage;
    private final String[] message;
    private final GuiWindowsController controller;
    
    // Constructors =======================================================================

    public PopupWindow(String title, String mainImage, String iconImage, String[] message, GuiWindowsController controller) {
        this.title = title;
        this.mainImage = mainImage;
        this.iconImage = iconImage;
        this.message = message;
        this.controller = controller;
        this.controller.plusOneActiveWindow();
    }
    
    // Methods ============================================================================
    
    private String getTextFromMessage(String[] strings){
        StringJoiner joiner = new StringJoiner("\n");
        for(String line : strings){
            joiner.add(line);
        }
        return joiner.toString();
    }
    
    @Override
    public void run() {
        Stage stage = new Stage();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                stage.close();
                controller.minusOneActiveWindow();
            }
        });
        stage.setAlwaysOnTop(true);
        
        VBox mainVBox = new VBox(15); 
        mainVBox.setPadding(new Insets(15, 15, 15, 15));
        mainVBox.setAlignment(Pos.TOP_CENTER);
        
        HBox hBox = new HBox(15);
        hBox.setMinWidth(300);
        hBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox messageTextBox = new VBox();
        messageTextBox.setAlignment(Pos.TOP_LEFT);
        
        Label picture = new Label("", new ImageView(new Image("file:"+this.mainImage))); 
        
        Label messageLabel = new Label(); 
        messageLabel.setFont(new Font(14.0));
        messageLabel.setWrapText(true);
        messageLabel.setPadding(new Insets(0, 0, 0, 0));
        
        messageLabel.setText(getTextFromMessage(this.message));
        
        messageTextBox.getChildren().addAll(messageLabel);
        hBox.getChildren().addAll(picture, messageTextBox);
        
        Button button = new Button("Ok");
        button.setFont(new Font(14.0));
        button.setMinWidth(100);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stage.close();
                controller.minusOneActiveWindow();
            }
        });
        
        mainVBox.getChildren().addAll(hBox, button);
        
        Scene scene = new Scene(mainVBox);
        
        stage.setTitle(this.title);
        stage.getIcons().add(new Image("file:"+this.iconImage));
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();
    }    
}
