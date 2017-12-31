/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx;

import javafx.application.Platform;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import diarsid.beam.core.application.environment.Configuration;

/**
 *
 * @author Diarsid
 */
public class GuiJavaFXResources {
    
    private final String pathToCssFile = "file:./../config/beam.css";
    
    private Image taskImage;
    private Image errorImage;
    private Image messageImage;
    
    private DropShadow buttonShadow;
    private DropShadow opacityBlackShadow;
    
    public GuiJavaFXResources(Configuration configuration) {
        String imagesLocation = configuration.asString("ui.images.resources");
        Platform.runLater(() -> {
            this.buttonShadow = new DropShadow();
            this.buttonShadow.setColor(Color.YELLOW);
            
            Color opacityBlack = new Color(0, 0, 0, 0.4);
            
            this.opacityBlackShadow = new DropShadow();
            this.opacityBlackShadow.setHeight(this.opacityBlackShadow.getHeight() * 1.35);
            this.opacityBlackShadow.setWidth(this.opacityBlackShadow.getWidth() * 1.35);
            this.opacityBlackShadow.setSpread(this.opacityBlackShadow.getSpread() * 1.35);            
            this.opacityBlackShadow.setColor(opacityBlack);

            this.taskImage = new Image("file:"+ imagesLocation + "task.png");                
            this.messageImage = new Image("file:"+ imagesLocation + "message.png");
            this.errorImage = new Image("file:"+ imagesLocation + "exception.png");           
        });
    }
    
    public DropShadow opacityBlackShadow() {
        return this.opacityBlackShadow;
    }
    
    public DropShadow buttonShadow() {
        return this.buttonShadow;
    }
    
    public Image taskImage() {
        return this.taskImage;
    }
    
    public Image errorImage() {
        return this.errorImage;
    }
    
    public Image infoImage() {
        return this.messageImage;
    }
    
    public String cssFilePath() {
        return this.pathToCssFile;
    }
}
