/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.innerio.javafxgui;

import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/**
 *
 * @author Diarsid
 */
public class WindowResourcesProvider {
    
    private final String taskImageURL;
    private final String taskIconURL;
    private final String messageImageURL;
    private final String messageIconURL;
    private final String errorImageURL;
    private final String errorIconURL;
    
    
    WindowResourcesProvider(String imagesLocation) {
        this.taskIconURL = imagesLocation + "task_ico.png";
        this.messageIconURL = imagesLocation + "message_ico.png";
        this.errorIconURL = imagesLocation + "exception_ico.png";
        this.taskImageURL = imagesLocation + "task.png";
        this.messageImageURL = imagesLocation + "message.png";
        this.errorImageURL = imagesLocation + "exception.png";
    }
    
    public String getPathToCssFile() {
        return "file:./../config/BeamWindow.css";
    }
    
    public String getTaskImageURL() {
        return this.taskImageURL;
    }
    
    public String getMessageImageURL() {
        return this.messageImageURL;
    }
    
    public String getErrorImageURL() {
        return this.errorImageURL;
    }
    
    public String getTaskIconURL() {
        return this.taskIconURL;
    }
    
    public String getMessageIconURL() {
        return this.messageIconURL;
    }
    
    public String getErrorIconURL() {
        return this.errorIconURL;
    }
    
    public DropShadow getButtonShadow() {
        DropShadow sh = new DropShadow();
        sh.setColor(Color.YELLOW);
        return sh;
    }
}
