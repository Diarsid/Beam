/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import static java.util.Objects.nonNull;

import static javafx.animation.Animation.Status.RUNNING;

/**
 *
 * @author Diarsid
 */
public abstract class BeamMessageWindow extends BeamWindow {
    
    private final GuiJavaFXResources resources;    
    
    private PauseTransition onTopRestoring;
    private Button onTopToggleButton;
    private Button okButton;
    
    public BeamMessageWindow(GuiJavaFXResources resources, WindowManager windowManager) {
        super(resources, windowManager);
        this.resources = resources;
    }
    
    protected abstract void onBeamMessageWindowCLosed();
    
    @Override
    protected final void onBeamWindowClosed() {
        super.parentStage().setAlwaysOnTop(true);         
        this.stopWindowOnTopThrowing(); 
        this.onTopToggleButton.setId("on-top-toggle-button-on"); 
        if ( nonNull(this.onTopRestoring) ) {
            this.onTopRestoring.stop();
        }
        this.onBeamMessageWindowCLosed();
    }
    
    @Override
    protected final void onBeamWindowStageCreated() {        
        this.createOnTopToggleButton();
        this.createOkButton("OK");
    }
    
    protected abstract Pane createBeamMessageWindowContentPane();
    
    @Override
    protected final Pane createBeamWindowContentPane() {
        VBox contentBox = new VBox();
        
        Pane beamMessageWindowContentPane = this.createBeamMessageWindowContentPane();
        beamMessageWindowContentPane.getStyleClass().add("message-inner-area");
        
        contentBox.getChildren().addAll(
                beamMessageWindowContentPane,
                this.createFooter());
        
        return contentBox;
    }
    
    private Pane createFooter() {
        VBox footerVBox = new VBox();
        
        VBox okButtonBox = new VBox();
        okButtonBox.setAlignment(Pos.CENTER);
        okButtonBox.getChildren().add(this.okButton);
        
        VBox onTopToggleButtonBox = new VBox();
        onTopToggleButtonBox.setAlignment(Pos.BOTTOM_RIGHT);
        onTopToggleButtonBox.getChildren().add(this.onTopToggleButton);
        
        footerVBox.getChildren().addAll(okButtonBox, onTopToggleButtonBox);
        
        return footerVBox;
    }
    
    private void createOkButton(String text) {
        Button button = new Button(text);    
        button.setId("ok-button");
        button.getStyleClass().add("button");
        button.setMinWidth(100);
        button.setMinHeight(30);
        button.setOnMouseClicked((mouseEvent) -> {
            super.close();
        });     
        
        button.setOnMouseEntered((mouseEvent) -> {
            button.setEffect(resources.buttonShadow());
        });
        
        button.setOnMouseExited((mouseEvent) -> {
            button.setEffect(null);
        });
        
        this.okButton = button;
    }
    
    private void createOnTopToggleButton() {
        Button button = new Button();
        button.setId("on-top-toggle-button-on");
        button.setMinSize(14, 14);
        button.setMaxSize(14, 14);
        button.setAlignment(Pos.CENTER_RIGHT);
        
        button.setOnMouseClicked((mouseEvent) -> {
            Stage parentStage = this.parentStage();
            if ( parentStage.isAlwaysOnTop() ) {
                parentStage.setAlwaysOnTop(false);
                parentStage.toBack();
                this.onTopToggleButton.setId("on-top-toggle-button-off");
                throwWindowOnTopAndBackPeriodicallyWithSecondsLatency(300);
            } else {
                parentStage.setAlwaysOnTop(true);
                this.onTopToggleButton.setId("on-top-toggle-button-on");
                stopWindowOnTopThrowing();                               
            }
        }); 
        
        button.setOnMouseEntered((mouseEvent) -> {
            button.setEffect(this.resources.buttonShadow());
        });
        
        button.setOnMouseExited((mouseEvent) -> {
            button.setEffect(null);
        });
        
        this.onTopToggleButton = button;
    }
    
    private void throwWindowOnTopAndBackPeriodicallyWithSecondsLatency(int seconds) {
        if ( this.onTopRestoringIsRunning() ) {
            this.onTopRestoring.playFromStart();
        } else {
            this.onTopRestoring = new PauseTransition(Duration.seconds(seconds));
            this.onTopRestoring.setOnFinished((actionEvent) -> {
                Stage parentStage = this.parentStage();
                parentStage.setAlwaysOnTop(true);
                parentStage.setAlwaysOnTop(false);
                this.onTopRestoring.playFromStart();
            });
            this.onTopRestoring.play();
        } 
    }
    
    private void stopWindowOnTopThrowing() {
        if ( this.onTopRestoringIsRunning() ) {
            this.onTopRestoring.stop();
        } 
    }
    
    private boolean onTopRestoringIsRunning() {
        return nonNull(this.onTopRestoring) && this.onTopRestoring.getStatus().equals(RUNNING);
    }
    
}
