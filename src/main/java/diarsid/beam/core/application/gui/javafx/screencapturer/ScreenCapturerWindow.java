/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.screencapturer;

import java.awt.Rectangle;
import java.awt.Robot;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import diarsid.beam.core.application.environment.Configuration;
import diarsid.beam.core.application.gui.javafx.BeamHiddenRoot;
import diarsid.beam.core.application.gui.javafx.GuiJavaFXResources;
import diarsid.beam.core.application.gui.javafx.WindowMover;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.domain.entities.Picture;

import static javafx.scene.control.OverrunStyle.ELLIPSIS;

import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;


/**
 *
 * @author Diarsid
 */
public class ScreenCapturerWindow implements Runnable {

    private final GuiJavaFXResources windowResources;
    private final BeamHiddenRoot beamHiddenRoot;
    private final WindowMover windowMover;
    private final ScreenCapturerWindowResizer windowResizer;
    private final ScreenCapturer screenCapturer;    
    private final BlockingQueue<ValueFlow<Picture>> blockingValueFlowQueue;
    private final Object blockingQueueLock;
    private final Object captureLock;
    private String pageName;
    private Stage stage;
    private Label controlPaneLabel;
    private Pane controlPane;
    private Pane screenCapturePane;
    private boolean ready;
    private boolean hasAwaiter;

    ScreenCapturerWindow(
            ScreenCapturer screenCapturer, 
            GuiJavaFXResources windowResources, 
            BeamHiddenRoot beamHiddenRoot) {
        this.windowResources = windowResources;
        this.beamHiddenRoot = beamHiddenRoot;
        this.windowMover = new WindowMover();
        this.windowResizer = new ScreenCapturerWindowResizer(142, 90);
        this.screenCapturer = screenCapturer;
        this.blockingValueFlowQueue = new ArrayBlockingQueue<>(1, true);
        this.blockingQueueLock = new Object();
        this.captureLock = new Object();
        this.pageName = "";
        this.ready = false;
        this.hasAwaiter = false;
    }
    
    public static ScreenCapturerWindow buildScreenCapturerWindow(
            Configuration configuration, 
            Robot robot, 
            GuiJavaFXResources windowResources,
            BeamHiddenRoot beamHiddenRoot) {
        ScreenCapturer screenCapturer = new ScreenCapturer(
                robot, configuration.asBoolean("ui.images.capture.webpages.resize"));
        return new ScreenCapturerWindow(screenCapturer, windowResources, beamHiddenRoot);
    }
    
    @Override
    public void run() {
        if ( this.ready ) {
            this.show();
        } else {
            this.init();
        }
    }
    
    private void show() {
        this.controlPaneLabel.setText(this.pageName);        
        this.windowResizer.toDefaultSize();
        this.stage.centerOnScreen();
        this.stage.show();
    }
    
    private void close() {
        this.stage.close();
        this.windowResizer.toDefaultSize();
        this.pageName = "";
    }
    
    private void init() {        
        this.createStage();        
        this.createControlPaneLabel();        
        this.createControlPane();
        this.createScreenCapturePane();        
        this.createManePaneAndScene();
    }

    private void createManePaneAndScene() {
        VBox mainVBox = new VBox();
        mainVBox.setStyle("-fx-background-color: transparent; ");
        mainVBox.setAlignment(Pos.BOTTOM_RIGHT);
        mainVBox.getChildren().addAll(this.controlPane, this.screenCapturePane);
        
        Scene scene = new Scene(mainVBox);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(this.windowResources.cssFilePath());
        this.stage.setScene(scene);
        this.stage.sizeToScene();
        this.ready = true;
    }

    private void createStage() {
        this.stage = new Stage();
        this.stage.initStyle(StageStyle.TRANSPARENT);
        this.stage.setAlwaysOnTop(true);
        this.stage.setMinWidth(110);
        this.stage.setMinHeight(90);
        this.stage.setResizable(true);
        this.stage.centerOnScreen();
        this.stage.initOwner(this.beamHiddenRoot.newHiddenStage());
        this.stage.setOnCloseRequest((windowEvent) -> {
            try {
                this.blockingValueFlowQueue.put(valueFlowStopped());
            } catch (InterruptedException ex) {
                // TODO MEDIUM
            }
        });
        
        this.windowMover.acceptStage(this.stage);
        this.windowResizer.acceptStage(this.stage);
    }

    private void createControlPaneLabel() {
        this.controlPaneLabel = new Label();
        this.controlPaneLabel.setTextOverrun(ELLIPSIS);
        this.controlPaneLabel.getStyleClass().add("screen-capture-control-pane-label");
        this.windowResizer.acceptLabel(this.controlPaneLabel);
    }
    
    public ValueFlow<Picture> blockingGetCaptureFor(String pageName) {
        this.cancelPreviouslyAwaitedTaskIfAny();        
        return this.beginNewAwaitedTask(pageName);
    }

    private void cancelPreviouslyAwaitedTaskIfAny() {
        synchronized ( this.blockingQueueLock ) {
            if ( this.hasAwaiter ) {
                try {
                    this.blockingValueFlowQueue.put(valueFlowStopped());
                } catch (InterruptedException ex) {
                    // TODO MEDIUM
                    ex.printStackTrace();
                }
            }
        }
    }

    private ValueFlow<Picture> beginNewAwaitedTask(String pageName) {
        synchronized ( this.captureLock ) {
            this.hasAwaiter = true;
            this.pageName = pageName;
            Platform.runLater(this);
            ValueFlow<Picture> pictureFlow;
            try {
                pictureFlow = this.blockingValueFlowQueue.take();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                pictureFlow = valueFlowFail("waiting for screen capture has been interrupted.");
            }
            this.hasAwaiter = false;
            return pictureFlow;
        }
    }
    
    private void createControlPane() {
        HBox innerBox = new HBox(); 
        
        this.windowMover.boundTo(innerBox);
        
        innerBox.setSpacing(5);        
        innerBox.getStyleClass().add("screen-capture-control-pane-inner-box");
        
        innerBox.getChildren().addAll(
                this.createCaptureButton(), 
                this.createCancelButton(), 
                this.controlPaneLabel);
        innerBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox outerBox = new VBox();
        outerBox.getStyleClass().add("screen-capture-control-pane-outer-box");        
        outerBox.setEffect(this.windowResources.opacityBlackShadow());
        outerBox.getChildren().add(innerBox);
        
        this.controlPane = outerBox;
    }

    private Button createCancelButton() {
        Button cancelButton = new Button();
        this.shapeScreenCapturePaneButton(cancelButton);
        
        cancelButton.setOnAction((actionEvent) -> {
            this.cancelScreenCapture();
            this.close();
        });
        
        return cancelButton;
    }

    private Button createCaptureButton() {
        Button captureButton = new Button();
        this.shapeScreenCapturePaneButton(captureButton);
        
        captureButton.setOnAction((actionEvent) -> {
            this.makeScreenCapture();
        });
        
        captureButton.setOnMouseExited((actionEvent) -> {
            
        });
        
        captureButton.setOnMouseEntered((actionEvent) -> {
            
        });
        
        return captureButton;
    }
    
    private void shapeScreenCapturePaneButton(Button button) {
        button.getStyleClass().addAll("beam-button", "screen-capture-control-pane-button");
        button.setMinWidth(22);
        button.setMinHeight(22);
        button.setMaxWidth(22);
        button.setMaxHeight(22);
    }

    private void cancelScreenCapture() {
        try {
            this.blockingValueFlowQueue.put(valueFlowStopped());
        } catch (InterruptedException ex) {
            // TODO MEDIUM
        }
    }

    private void makeScreenCapture() {
        try {
            this.blockingValueFlowQueue.put(
                    this.captureScreenRectangle()
                            .toFlowWith(bytes -> new Picture(this.pageName, bytes)));
            this.close();
        } catch (InterruptedException ex) {
            // TODO MEDIUM
            ex.printStackTrace();
        }
    }

    private ValueFlow<byte[]> captureScreenRectangle() {
        Bounds capturePaneScreenCoordinates =
                this.screenCapturePane.localToScreen(
                        this.screenCapturePane.getBoundsInLocal());
        Rectangle screen = new Rectangle(
                (int) capturePaneScreenCoordinates.getMinX() + 16,
                (int) capturePaneScreenCoordinates.getMinY() + 5,
                (int) capturePaneScreenCoordinates.getWidth() - 32,
                (int) capturePaneScreenCoordinates.getHeight() - 10);
        
        return this.screenCapturer.captureRectangle(screen);
    }
    
    private void createScreenCapturePane() {
        VBox screenCaptureBox = new VBox(15);
        
        screenCaptureBox.setMinWidth(142);
        screenCaptureBox.setMinHeight(90);
        screenCaptureBox.setAlignment(Pos.TOP_CENTER);
        screenCaptureBox.setId("screen-capture-capture-pane");
        
        this.windowResizer.acceptPane(screenCaptureBox);
        
        screenCaptureBox.setOnMousePressed((mouseEvent) -> {
            this.windowResizer.mousePressed(mouseEvent);
        });
        
        screenCaptureBox.setOnMouseDragged((mouseEvent) -> {
            this.windowResizer.mouseDragged(mouseEvent);
        });
        
        this.screenCapturePane = screenCaptureBox;
    }
    
}
