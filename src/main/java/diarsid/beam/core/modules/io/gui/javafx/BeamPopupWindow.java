/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io.gui.javafx;

import java.util.StringJoiner;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import diarsid.beam.core.base.control.io.base.interaction.Message;

/**
 *
 * @author Diarsid
 */
class BeamPopupWindow extends BeamMessageWindow {
    
    private final GuiJavaFXResources resources;
    private Message message;
    private Label messageLabel;
    private ImageView messageImage;
    
    BeamPopupWindow(
            WindowManager controller,
            GuiJavaFXResources resources) {        
        super(resources, controller);
        this.resources = resources;
    }
    
    public BeamPopupWindow accept(Message message) {
        this.message = message;
        return this;
    }

    @Override
    protected void onBeamMessageWindowCLosed() {
        this.message = null;
    }

    @Override
    protected Pane createBeamMessageWindowContentPane() {        
        HBox hBox = new HBox(15);
        hBox.setMinWidth(300);
        hBox.setAlignment(Pos.CENTER_LEFT);
        
        this.messageImage = new ImageView();
        
        this.messageLabel = new Label(); 
        this.messageLabel.setWrapText(true);
        
        hBox.getChildren().addAll(this.messageImage, this.messageLabel);
        
        return hBox;
    }

    @Override
    protected void refreshBeamWindowState() {
        this.messageLabel.setText(this.getTextFromMessage());
        switch ( this.message.type() ) {
            case INFO : {
                super.setBarTitle("Message");
                this.messageImage.setImage(this.resources.infoImage());
                break;
            }    
            case ERROR : {
                super.setBarTitle("Error");
                this.messageImage.setImage(this.resources.errorImage());
                break;
            }    
            case TASK : {
                super.setBarTitle("Task");
                this.messageImage.setImage(this.resources.taskImage());
                break;
            }    
            default : {
                // nothing
            }   
        }
    }
        
    private String getTextFromMessage() {        
        StringJoiner joiner = new StringJoiner("\n");
        if ( this.message.hasHeader() ) {
            joiner.add(this.message.header());
            joiner.add("");
        }
        for (String line : this.message.lines()) {
            joiner.add(line);
        }
        return joiner.toString();
    }
}
