/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.console;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import diarsid.beam.core.application.gui.javafx.WindowMover;
import diarsid.beam.core.application.gui.javafx.WindowResources;
import diarsid.beam.core.base.control.io.base.console.ConsoleBlockingExecutor;
import diarsid.beam.core.base.control.io.base.console.ConsolePlatform;

import static javafx.geometry.Pos.BOTTOM_RIGHT;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;

import static diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType.IN_MACHINE;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoIndependently;

/**
 *
 * @author Diarsid
 */
// TODO HIGH do not public
public class JavaFXConsolePlatform 
        extends 
                ConsolePlatform
        implements 
                Runnable {
    
    private final WindowResources windowResources;
    private final WindowMover windowMover;
    private final ConsoleWindowBlockingIO blockingIo;
    private final AtomicInteger consoleCommitedLength;
    private final AtomicInteger consoleTextAreaInternalInputCounter;
    private final Object consoleTextAreaLock;
    private Stage stage;
    private Pane bar;
    private Pane mainArea;
    private TextArea consoleTextArea;
    private boolean ready;

    JavaFXConsolePlatform(
            WindowResources windowResources, 
            ConsoleWindowBlockingIO consoleBlockingIo,
            ConsoleBlockingExecutor blockingExecutor) {
        super(consoleBlockingIo, blockingExecutor, IN_MACHINE);
        this.windowResources = windowResources;
        this.windowMover = new WindowMover();
        this.blockingIo = consoleBlockingIo;
        this.consoleCommitedLength = new AtomicInteger();
        this.consoleTextAreaInternalInputCounter = new AtomicInteger();
        this.consoleTextAreaLock = new Object();
        this.ready = false;
    }
    
    public static ConsolePlatform createAndLaunchJavaFXConsolePlatform(
            WindowResources windowResources, ConsoleBlockingExecutor blockingExecutor) {
        ConsoleWindowBlockingIO consoleIo = new ConsoleWindowBlockingIO();
        JavaFXConsolePlatform consoleWindow = new JavaFXConsolePlatform(
                windowResources, consoleIo, blockingExecutor);
        Platform.runLater(consoleWindow);
        return consoleWindow;
    }
    
    @Override
    public void run() {
        if ( this.ready ) {
            this.show();
        } else {
            this.initAndShow();
        }
    }
    
    private void show() {
        this.stage.show();
    }
    
    private void hide() {
        this.stage.hide();
    }
    
    private void initAndShow() {
        this.createStage();
        this.createBar();
        this.createMainArea();
        this.startListenBlockingConsoleIncome();
        
        VBox mainVBox = new VBox();
        mainVBox.setStyle(
                "-fx-border-insets: 10px; " +
                "-fx-background-insets: 10px; " +
                "-fx-background-color: #FF9F00; " +
                "-fx-background-radius: 12px; " +
                "-fx-padding: 10px;"        
//                "-fx-border-color: #ffbf00; " +
//                "-fx-border-width: 4px; " +
//                "-fx-border-radius: 10px; "
        );
        mainVBox.setAlignment(Pos.BOTTOM_RIGHT);
        DropShadow sh = new DropShadow();
        sh.setHeight(sh.getHeight() * 1.35);
        sh.setWidth(sh.getWidth() * 1.35);
        sh.setSpread(sh.getSpread() * 1.35);
        Color opacityBlack = new Color(0, 0, 0, 0.4);
        sh.setColor(opacityBlack);
        mainVBox.setEffect(sh);
        
        VBox visibleBox = new VBox();
        visibleBox.setAlignment(BOTTOM_RIGHT);
        
        visibleBox.getChildren().addAll(this.bar, this.mainArea);
        visibleBox.setStyle("-fx-padding: 4px;");
        mainVBox.getChildren().addAll(visibleBox);
        
        Scene scene = new Scene(mainVBox);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(this.windowResources.getPathToCssFile());
        this.stage.setScene(scene);
        this.stage.sizeToScene();
        this.ready = true;
        
        this.show();
    }
    
    private void createStage() {
        this.stage = new Stage();
        this.stage.initStyle(StageStyle.TRANSPARENT);
        this.stage.setMinWidth(800);
        this.stage.setMinHeight(100);
        this.stage.setResizable(true);
        this.stage.centerOnScreen();
        this.windowMover.acceptStage(this.stage);
    }
    
    private void createBar() {
        HBox barHBox = new HBox();
        
        Label label = new Label("Beam > Console");
        label.setStyle(
                "-fx-font: 14px \"Consolas\"; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; "        
        );
        barHBox.getChildren().add(label);
        barHBox.setPadding(new Insets(3));
        
        barHBox.setOnMousePressed((mouseEvent) -> {
            this.windowMover.onMousePressed(mouseEvent);
        });
        
        barHBox.setOnMouseDragged((mouseEvent) -> {
            this.windowMover.onMouseDragged(mouseEvent);
        });
        
        this.bar = barHBox;
    }
    
    private void createMainArea() {
        VBox mainAreaVBox = new VBox();
        mainAreaVBox.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 11px; " + 
                "-fx-border-color: #FFDE00; " + 
                "-fx-border-width: 4px; " + 
                "-fx-border-radius: 10px;" +
                "-fx-padding: 5px;" 
        );        
        
        TextArea textArea = new TextArea();
        textArea.setTextFormatter(this.createTextFormatter());
        textArea.addEventFilter(KEY_PRESSED, this.createEnterKeyInterceptor());
        textArea.setMinHeight(100);
        textArea.setMinWidth(400);
        textArea.setStyle(
                "-fx-background-color: white; " +
                "-fx-focus-color: transparent; " +
                "-fx-text-box-border: transparent; " + 
                "-fx-faint-focus-color: transparent; " + 
                "-fx-font: 13px \"Consolas\"; "
        );
                
        mainAreaVBox.getChildren().add(textArea);
        
        this.consoleTextArea = textArea;    
        this.mainArea = mainAreaVBox;
    }
    
    private EventHandler<KeyEvent> createEnterKeyInterceptor() {
        return (keyEvent) -> {
            if ( keyEvent.getCode().equals(ENTER) ) {
                if ( this.consoleTextArea.getCaretPosition() == 
                        this.consoleTextArea.getText().length() ) {
                    return;
                }
                int caretPosition = this.consoleTextArea.getCaretPosition();
                int rawInputTextLength = this.consoleTextArea.getText().length();
                int commitedTextLength = this.consoleCommitedLength.get();
                if (    caretPosition >= commitedTextLength &&
                        caretPosition < rawInputTextLength ) {
                    this.consoleTextArea.deleteText(caretPosition, rawInputTextLength);                    
                } else if ( caretPosition < rawInputTextLength ) {
                    this.consoleTextArea.deleteText(commitedTextLength, rawInputTextLength);
                    keyEvent.consume();
                }
            }
        };
    }
    
    private void startListenBlockingConsoleIncome() {
        asyncDoIndependently("JavaFX Console internal output listener Thread", ()-> {
            while ( true ) {                
                try {
                    String newLineIntoConsole = this.blockingIo.blockingGetPrintedString();
                    Platform.runLater(() -> {
                        synchronized ( this.consoleTextAreaLock ) {
                            this.consoleTextAreaInternalInputCounter.incrementAndGet();
                            this.consoleTextArea.setEditable(false);
                            this.consoleTextArea.appendText(newLineIntoConsole);
                            this.consoleTextArea.commitValue();
                        } 
                    });                       
                } catch (InterruptedException e) {
                    // nothing
                }
            }
        });
    }
    
    private TextFormatter createTextFormatter() {        
        UnaryOperator<Change> changeInterceptor = (change) -> {    
            synchronized ( this.consoleTextAreaLock ) {
                if ( change.isDeleted() ) {
                    this.interceptOnDeleted(change);                
                } else if ( change.isAdded()) {
                    this.interceptOnAdded(change);
                } else {
                    this.interceptOnOther(change);
                }
//                if ( this.consoleTextArea.getScrollTop() < MAX_VALUE ) {
//                    this.consoleTextArea.setScrollTop(MAX_VALUE);
//                }
                return change;
            }
        };
        return new TextFormatter<>(new DefaultStringConverter(), "", changeInterceptor);
    }
    
    private void interceptOnOther(Change change) {
        int controlNewTextLength = change.getControlNewText().length();
        if ( change.getControlNewText().length() < this.consoleCommitedLength.get() ) {
            this.consoleCommitedLength.set(controlNewTextLength);
        }
    }

    private void interceptOnAdded(Change change) {
        String changeText = change.getText();
        if ( changeText.equals("\n") ) {
            // case: enter pressed, commited to console, cannot be deleted
            this.consoleTextArea.setEditable(false);                
            String text = change.getControlText();
            int start = this.consoleCommitedLength.get();
            int end = text.length();
            String newInput = text.substring(start, end);
            try {
                this.blockingIo.blockingSetString(newInput);
            } catch (InterruptedException e) {
                //
            }
            int newTextLength = change.getControlNewText().length();
            this.consoleCommitedLength.set(newTextLength);
            this.setCaretCorrectPosition(change, newTextLength);
//            if ( this.consoleTextArea.getScrollTop() < MAX_VALUE ) {
//                this.consoleTextArea.setScrollTop(MAX_VALUE);
//            }
        } else if ( this.consoleTextAreaInternalInputCounter.get() > 0 ) {
            // case: something printed to console as a response, 
            // commited to console, cannot be deleted
            boolean editable = changeText.endsWith(" > ") || changeText.endsWith(" : ");
//                        this.consoleTextAreaInternalInputCounter.get() == 1;
            this.consoleTextArea.setEditable(editable);
            int newTextLength = change.getControlNewText().length();
            this.consoleCommitedLength.set(newTextLength);
            this.setCaretCorrectPosition(change, newTextLength);
            this.consoleTextAreaInternalInputCounter.decrementAndGet();
        } else {
            // case: usual input, not commited to console, can be deleted.
            this.adjustChangeIfAppliedToCommited(change);
//            if ( this.consoleTextArea.getScrollTop() < MAX_VALUE ) {
//                this.consoleTextArea.setScrollTop(MAX_VALUE);
//            }
        }            
    }

    private void interceptOnDeleted(Change change) {
        int commitedTextLength = this.consoleCommitedLength.get();
        if ( change.getRangeStart() < commitedTextLength ) {
            change.setRange(commitedTextLength, commitedTextLength);
            this.setCaretCorrectPosition(change, commitedTextLength);
        }
    }
    
    private void adjustChangeIfAppliedToCommited(Change change) {
        if ( change.getRangeStart() < this.consoleCommitedLength.get() ) {
            int oldTextLength = change.getControlText().length();
            change.setRange(oldTextLength, oldTextLength);
            this.setCaretCorrectPosition(change, change.getControlNewText().length());
        }
    }
    
    private void setCaretCorrectPosition(Change change, int position) {
        change.setCaretPosition(position);
        change.setAnchor(position);
        change.selectRange(position, position);
    }

    @Override
    public String name() {
        return "Native JavaFX Console Platform";
    }

    @Override
    public void whenStopped() {
        Platform.runLater(() -> this.hide());
    }

    @Override
    public void whenInitiatorAccepted() {
        // TODO ?
    }
}
