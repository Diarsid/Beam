package diarsid.beam.core.modules.io.javafxgui.window;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import diarsid.beam.core.modules.io.javafxgui.ReusableTaskWindow;
import diarsid.beam.core.modules.io.javafxgui.WindowController;
import diarsid.beam.core.modules.io.javafxgui.WindowPosition;
import diarsid.beam.core.modules.io.javafxgui.WindowResources;

import static javafx.animation.Animation.Status.RUNNING;
import static javafx.scene.layout.VBox.setMargin;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Diarsid
 */
abstract class BeamWindow implements Comparable<BeamWindow> {    
    
    private final WindowController controller;
    private final WindowResources resources;
    
    private Stage stage;
    private Button onTopControllerButton;
    private PauseTransition onTopRestoring;
    
    BeamWindow(WindowResources resources, WindowController c) {
        this.resources = resources;
        this.controller = c;
    }
    
    void prepareStage() {
        this.stage = new Stage();
        this.onTopControllerButton = this.newOnTopControllerButton();
        this.stage.initStyle(StageStyle.TRANSPARENT);
        this.stage.setAlwaysOnTop(true);
        this.stage.setMinWidth(300);
        this.stage.setMinHeight(200);
        this.stage.setResizable(false);
        
        this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                closeThis();
            }
        });
    }    
        
    final void closeThis() {
        this.stage.hide();
        stage.setAlwaysOnTop(true);
        onTopControllerButton.setId("on-top-toggle-button-on");  
        this.stopWindowOnTopThrowing();
        this.controller.windowClosed();
        if ( this.getClass().equals(TaskWindow.class) ) {
            this.resources.addTaskWindowToReusable( (ReusableTaskWindow) this );
        }   
        if ( this.onTopRestoring != null ) {
            this.onTopRestoring.stop();
        }
    }    
    
    final void showThis() {
        WindowPosition position = controller.getNewWindowPosition();
        if ( (position.getX() != 0) && (Double.isNaN(this.stage.getX())) ) {
            this.stage.setX(position.getX());
            this.stage.setY(position.getY());
        } 
        this.stage.sizeToScene();
        this.stage.show();        
        this.controller.reportLastWindowPosition(stage.getX(), stage.getY());
    }
    
    final void setContent(Pane contentPane) {
        DropShadow sh = new DropShadow();
        sh.setHeight(sh.getHeight() * 1.3);
        sh.setWidth(sh.getWidth() * 1.3);
        sh.setSpread(sh.getSpread() * 1.3);
        sh.setColor(Color.DARKSLATEGRAY);
        VBox mainVBox = new VBox();
        mainVBox.setEffect(sh);
        mainVBox.setId("main");
        mainVBox.setAlignment(Pos.BOTTOM_RIGHT);
        mainVBox.setPadding(new Insets(3));
        mainVBox.getChildren().addAll(contentPane, onTopControllerButton);
        setMargin(contentPane, new Insets(10, 10, 0, 10));
        Scene scene = new Scene(mainVBox);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(this.resources.getPathToCssFile());
        this.stage.setScene(scene);        
    }    
    
    final void setTitle(String title) {
        this.stage.setTitle(title);
    }
    
    final void setErrorIcon() {
        this.stage.getIcons().add(this.resources.getErrorIconImage());
    }
    
    final void setTaskIcon() {
        this.stage.getIcons().add(this.resources.getTaskIconImage());
    }
    
    final void setMessageIcon() {
        this.stage.getIcons().add(this.resources.getMessageIconImage());
    }
    
    final DropShadow buttonShadow() {
        return this.resources.getButtonShadow();
    }
    
    final Button getOnTopControlButton() {
        return this.onTopControllerButton;
    }
    
    Button newOkButton(String text) {
        Button button = new Button(text);    
        button.setId("ok-button");
        button.getStyleClass().add("button");
        button.setMinWidth(100);
        button.setMinHeight(30);
        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                closeThis();
            }
        });     
        
        button.setOnMouseEntered(new EventHandler<MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {                
                button.setEffect(resources.getButtonShadow());
            }
        });
        
        button.setOnMouseExited(new EventHandler<MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                button.setEffect(null);
            }
        });
        
        return button;
    }
    
    Button newOnTopControllerButton() {
        Button button = new Button();
        button.setId("on-top-toggle-button-on");
        button.setMinSize(14, 14);
        button.setMaxSize(14, 14);
        button.setAlignment(Pos.CENTER_RIGHT);
        
        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if ( stage.isAlwaysOnTop() ) {
                    stage.setAlwaysOnTop(false);
                    stage.toBack();
                    onTopControllerButton.setId("on-top-toggle-button-off");
                    //waitAndRestoreOnTop();      
                    throwWindowOnTopAndBackPeriodicallyWithSecondsLatency(300);
                } else {
                    stage.setAlwaysOnTop(true);
                    onTopControllerButton.setId("on-top-toggle-button-on");  
                    stopWindowOnTopThrowing();
                }                               
            }
        }); 
        
        button.setOnMouseEntered(new EventHandler<MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {                
                button.setEffect(resources.getButtonShadow());
            }
        });
        
        button.setOnMouseExited(new EventHandler<MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                button.setEffect(null);
            }
        });
        
        return button;
    }
    
    private void waitAndRestoreOnTop() {  
        if ( onTopRestoring != null && onTopRestoring.getStatus().equals(RUNNING)) {
            this.onTopRestoring.playFromStart();
        } else {
            this.onTopRestoring = new PauseTransition(Duration.seconds(60));
            this.onTopRestoring.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    stage.setAlwaysOnTop(true);
                    onTopControllerButton.setId("on-top-toggle-button-on");
                }
            });
            this.onTopRestoring.play();
        }        
    }
    
    private void throwWindowOnTopAndBackPeriodicallyWithSecondsLatency(int seconds) {
        if ( onTopRestoring != null && onTopRestoring.getStatus().equals(RUNNING)) {
            this.onTopRestoring.playFromStart();
        } else {
            this.onTopRestoring = new PauseTransition(Duration.seconds(seconds));
            this.onTopRestoring.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    stage.setAlwaysOnTop(true);
                    stage.setAlwaysOnTop(false);
                    onTopRestoring.playFromStart();
                }
            });
            this.onTopRestoring.play();
        } 
    }
    
    private void stopWindowOnTopThrowing() {
        if ( onTopRestoring != null && onTopRestoring.getStatus().equals(RUNNING)) {
            this.onTopRestoring.stop();
        } 
    }
    
    final Image getErrorImage() {
        return this.resources.getErrorImage();
    } 
    
    final Image getTaskImage() {
        return this.resources.getTaskImage();
    } 
        
    final Image getMessageImage() {
        return this.resources.getMessageImage();
    } 
    
    @Override
    public int compareTo(BeamWindow other) {
        double thisSum = this.stage.getX() + this.stage.getY();
        double otherSum = other.stage.getX() + other.stage.getY();
        if ( thisSum < otherSum ) {
            return -1;
        } else if ( thisSum > otherSum ) {
            return 1;
        } else {
            return 0;
        }
    }
}
