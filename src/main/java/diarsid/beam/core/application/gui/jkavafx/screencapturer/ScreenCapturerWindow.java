/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.jkavafx.screencapturer;

import java.awt.Rectangle;
import java.awt.Robot;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import diarsid.beam.core.application.gui.javafx.WindowResources;
import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.domain.entities.Picture;

import static javafx.geometry.Insets.EMPTY;

import static diarsid.beam.core.base.control.flow.Operations.valueCompletedWith;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationFail;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationStopped;


/**
 *
 * @author Diarsid
 */
public class ScreenCapturerWindow implements Runnable {

    private final WindowResources windowResources;
    private final WindowMover windowMover;
    private final WindowResizer windowResizer;
    private final ScreenCapturer screenCapturer;    
    private final BlockingQueue<ValueOperation<Picture>> valueOperationAwaitQueue;
    private final Object queueLock;
    private final Object captureLock;
    private String pageName;
    private Stage stage;
//    private Label controlPaneLabel;
    private Pane controlPane;
    private Pane screenCapturePane;
    private boolean ready;
    private boolean hasAwaiter;

    public ScreenCapturerWindow(Robot robot, WindowResources windowResources) {
        this.windowResources = windowResources;
        this.windowMover = new WindowMover();
        this.windowResizer = new WindowResizer(142, 90);
        this.screenCapturer = new ScreenCapturer(robot);
        this.valueOperationAwaitQueue = new ArrayBlockingQueue<>(1, true);
        this.queueLock = new Object();
        this.captureLock = new Object();
        this.pageName = "";
        this.ready = false;
        this.hasAwaiter = false;
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
//        this.controlPaneLabel.setText("x");
        this.stage.centerOnScreen();
        this.stage.show();
    }
    
    private void close() {
        this.stage.close();
        this.windowResizer.toDefaultSize();
        this.pageName = "";
    }
    
    private void init() {        
        this.stage = new Stage();
        this.stage.initStyle(StageStyle.TRANSPARENT);
        this.stage.setAlwaysOnTop(true);
        this.stage.setMinWidth(110);
        this.stage.setMinHeight(90);
        this.stage.setResizable(true);
        this.stage.centerOnScreen();
        this.stage.setOnCloseRequest((windowEvent) -> {
            try {
                this.valueOperationAwaitQueue.put(valueOperationStopped());
            } catch (InterruptedException ex) {
                // TODO MEDIUM
            }
        });
        
        this.windowMover.acceptStage(this.stage);
        this.windowResizer.acceptStage(this.stage);
        
//        this.controlPaneLabel = new Label();
//        this.controlPaneLabel.setTextOverrun(ELLIPSIS);
//        this.controlPane = createControlPane(this.controlPaneLabel);
        this.controlPane = createControlPane();
        this.screenCapturePane = createScreenCapturePane();
        
        VBox mainVBox = new VBox();
        mainVBox.setStyle("-fx-background-color: transparent; ");
        mainVBox.setAlignment(Pos.BOTTOM_RIGHT);
        mainVBox.getChildren().addAll(this.controlPane, this.screenCapturePane);
        
        Scene scene = new Scene(mainVBox);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(this.windowResources.getPathToCssFile());
        this.stage.setScene(scene);
        this.stage.sizeToScene();
        this.ready = true;
    }
    
    public ValueOperation<Picture> blockingGetCaptureFor(String pageName) {
        this.cancelPreviouslyAwaitedTaskIfAny();        
        return this.beginNewAwaitedTask(pageName);
    }

    private void cancelPreviouslyAwaitedTaskIfAny() {
        synchronized ( this.queueLock ) {
            if ( this.hasAwaiter ) {
                try {
                    this.valueOperationAwaitQueue.put(valueOperationStopped());
                } catch (InterruptedException ex) {
                    // TODO MEDIUM
                    ex.printStackTrace();
                }
            }
        }
    }

    private ValueOperation<Picture> beginNewAwaitedTask(String pageName) {
        synchronized ( this.captureLock ) {
            this.hasAwaiter = true;
            this.pageName = pageName;
            Platform.runLater(this);
            ValueOperation<Picture> pictureFlow;
            try {
                pictureFlow = this.valueOperationAwaitQueue.take();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                pictureFlow = valueOperationFail("waiting for screen capture has been interrupted.");
            }
            this.hasAwaiter = false;
            return pictureFlow;
        }
    }
    
    private Pane createControlPane() {
        HBox hBox = new HBox(15); 
        
        hBox.setOnMousePressed((mouseEvent) -> {
            this.windowMover.onMousePressed(mouseEvent);
        });
        
        hBox.setOnMouseDragged((mouseEvent) -> {
            this.windowMover.onMouseDragged(mouseEvent);
        });
        
        hBox.setSpacing(4);
        DropShadow sh = new DropShadow();
        sh.setHeight(sh.getHeight() * 1.4);
        sh.setWidth(sh.getWidth() * 1.4);
        sh.setSpread(sh.getSpread() * 1.4);
        sh.setColor(Color.BLACK);
        hBox.setEffect(sh);
        hBox.setId("screen-capture-control-pane");
        hBox.setPadding(new Insets(3));

        Button captureButton = new Button();    
        captureButton.setId("ok-button");
        captureButton.getStyleClass().add("button");        
        captureButton.setPadding(EMPTY);
        captureButton.setMinWidth(20);
        captureButton.setMinHeight(20);
        captureButton.setMaxWidth(20);
        captureButton.setMaxHeight(20);
        captureButton.setOnAction((actionEvent) -> {
            this.makeScreenCapture();
        });
        captureButton.setOnMouseExited((actionEvent) -> {
            
        });
        captureButton.setOnMouseEntered((actionEvent) -> {
            
        });
        
        Button cancelButton = new Button();    
        cancelButton.setId("ok-button");
        cancelButton.getStyleClass().add("button");
        cancelButton.setPadding(EMPTY);
        cancelButton.setMinWidth(20);
        cancelButton.setMinHeight(20);
        cancelButton.setMaxWidth(20);
        cancelButton.setMaxHeight(20);
        cancelButton.setOnAction((actionEvent) -> {
            this.cancelScreenCapture();
            this.close();
        });
        
        hBox.getChildren().addAll(captureButton, cancelButton);
        hBox.setAlignment(Pos.CENTER_LEFT);
        
        return hBox;
    }

    private void cancelScreenCapture() {
        try {
            this.valueOperationAwaitQueue.put(valueOperationStopped());
        } catch (InterruptedException ex) {
            // TODO MEDIUM
        }
    }

    private void makeScreenCapture() {
        try {
            ValueOperation<Picture> pictureFlow;
            Optional<byte[]> bytes = this.captureScreenRectangle();
            
            if ( bytes.isPresent() ) {
                pictureFlow = valueCompletedWith(new Picture(pageName, bytes.get()));
            } else {
                pictureFlow = valueOperationFail("cannot get picture bytes.");
            }
            
            this.valueOperationAwaitQueue.put(pictureFlow);
            this.close();
        } catch (InterruptedException ex) {
            // TODO MEDIUM
            ex.printStackTrace();
        }
    }

    private Optional<byte[]> captureScreenRectangle() {
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
    
    private Pane createScreenCapturePane() {
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
        
        return screenCaptureBox;
    }
    
}
