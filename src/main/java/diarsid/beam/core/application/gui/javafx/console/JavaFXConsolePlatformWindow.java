/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.console;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import diarsid.beam.core.application.gui.javafx.GuiJavaFXResources;
import diarsid.beam.core.application.gui.javafx.WindowMover;
import diarsid.beam.core.base.control.io.base.console.ConsoleBlockingExecutor;
import diarsid.beam.core.base.control.io.base.console.ConsolePlatform;
import diarsid.beam.core.base.util.MutableString;
import diarsid.beam.core.base.util.PointableCollection;

import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.geometry.Pos.TOP_CENTER;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyCode.Y;
import static javafx.scene.input.KeyCode.Z;
import static javafx.scene.input.KeyCombination.SHORTCUT_DOWN;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.stage.WindowEvent.WINDOW_HIDDEN;
import static javafx.stage.WindowEvent.WINDOW_SHOWN;

import static diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType.IN_MACHINE;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoIndependently;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.MutableString.emptyMutableString;

/**
 *
 * @author Diarsid
 */
// TODO HIGH do not public
public class JavaFXConsolePlatformWindow extends ConsolePlatform {
    
    private final GuiJavaFXResources windowResources;
    private final WindowMover windowMover;
    private final ConsoleWindowResizer windowResizer;
    private final ConsoleWindowBlockingIO blockingIo;
    private final AtomicInteger consoleCommitedLength;
    private final AtomicInteger consoleTextAreaInternalInputCounter;
    private final AtomicBoolean deleteCommitedTextAllowed;
    private final Object consoleTextAreaLock;
    private final PointableCollection<String> consoleInputBuffer;
    private final AtomicInteger consoleInputBufferCapacity;
    private final Runnable runnableLaunch;
    private final AtomicBoolean isShown;
    private Stage stage;
    private Pane bar;
    private Pane mainArea;
    private TextArea consoleTextArea;
    private boolean ready;

    JavaFXConsolePlatformWindow(
            GuiJavaFXResources javaFxResources, 
            ConsoleWindowBlockingIO consoleBlockingIo,
            ConsoleBlockingExecutor blockingExecutor) {
        super(consoleBlockingIo, blockingExecutor, IN_MACHINE);
        this.windowResources = javaFxResources;
        this.windowMover = new WindowMover();
        this.windowResizer = new ConsoleWindowResizer();
        this.blockingIo = consoleBlockingIo;
        this.consoleCommitedLength = new AtomicInteger();
        this.consoleTextAreaInternalInputCounter = new AtomicInteger();
        this.deleteCommitedTextAllowed = new AtomicBoolean();
        this.consoleTextAreaLock = new Object();
        this.consoleInputBufferCapacity = new AtomicInteger(50);
        this.consoleInputBuffer = new PointableCollection<>(
                this.consoleInputBufferCapacity.get(), "");
        this.ready = false;
        this.isShown = new AtomicBoolean(false);
        this.runnableLaunch = () -> {
            if ( this.ready ) {
                this.show();
            } else {
                this.init();
                this.show();
            }
        };
    }
    
    public void openOrOnTop() {
        if ( this.isShown.get() ) {
            this.throwOnTop();
        } else {
            this.show();
        }
    }
    
    private void throwOnTop() {
        this.stage.setAlwaysOnTop(true);
        this.stage.setAlwaysOnTop(false);
        this.consoleTextArea.requestFocus();
    }
    
    public static JavaFXConsolePlatformWindow createAndLaunchJavaFXConsolePlatform(
            GuiJavaFXResources resources, ConsoleBlockingExecutor blockingExecutor) {
        ConsoleWindowBlockingIO consoleIo = new ConsoleWindowBlockingIO();
        JavaFXConsolePlatformWindow consoleWindow = new JavaFXConsolePlatformWindow(
                resources, consoleIo, blockingExecutor);
        consoleWindow.launch();
        return consoleWindow;
    }
    
    private void launch() {
        Platform.runLater(this.runnableLaunch);
    }
    
    private void show() {
        this.stage.show();
        this.consoleTextArea.requestFocus();
        debug("[CONSOLE WINDOW] editable: " + this.consoleTextArea.isEditable());
    }
    
    private void init() {
        this.createStage();
        this.createBar();
        this.createMainArea();
        this.startListenBlockingConsoleIncome();
        this.fillStageWithScene();
    }
    
    private void hide() {
        this.stage.hide();
    }
    
    private void fillStageWithScene() {
        this.stage.setScene(this.createConsoleWindowScene());
        this.stage.sizeToScene();
        this.ready = true;
    }
    
    private Scene createConsoleWindowScene() {      
        VBox consoleInnerBox = new VBox();
        consoleInnerBox.setAlignment(TOP_CENTER);        
        consoleInnerBox.getChildren().addAll(this.bar, this.mainArea);
        consoleInnerBox.getStyleClass().add("console-inner-box");
        this.windowResizer.listen(consoleInnerBox);     
        
        VBox consoleOuterBox = new VBox();
        consoleOuterBox.getStyleClass().add("console-outer-box");        
        consoleOuterBox.setAlignment(TOP_CENTER);        
        consoleOuterBox.setEffect(this.windowResources.opacityBlackShadow());
        
        consoleOuterBox.getChildren().addAll(consoleInnerBox);
        
        Scene scene = new Scene(consoleOuterBox);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(this.windowResources.cssFilePath());
        
        return scene;
    }
    
    private void createStage() {
        this.stage = new Stage();
        this.stage.initStyle(StageStyle.TRANSPARENT);
        this.stage.setResizable(true);
        this.stage.centerOnScreen();
        this.stage.setOnCloseRequest((windowEvent) -> {
            this.hide();
        });
        this.windowMover.acceptStage(this.stage);
        this.windowResizer.acceptStage(this.stage);
        
        this.stage.addEventHandler(WINDOW_HIDDEN, (windowEvent) -> {
            this.isShown.set(false);
        });
        
        this.stage.addEventHandler(WINDOW_SHOWN, (windowEvent) -> {            
            this.isShown.set(true);
        });
    }
    
    private void createBar() {
        HBox barBox = new HBox(5);
        barBox.getStyleClass().add("console-bar");
        barBox.setAlignment(CENTER_LEFT);
        barBox.getChildren().addAll(this.createBarPoint(), this.createBarLabel());
        barBox.setPadding(new Insets(0, 3, 3, 0));
        this.windowMover.boundTo(barBox);        
        this.bar = barBox;
    }
    
    private Label createBarLabel() {
        Label barHeader = new Label("Beam > Console");
        barHeader.getStyleClass().add("console-bar-header");
        return barHeader;
    }
    
    private Label createBarPoint() {
        Label point = new Label();
        
        point.setMaxHeight(14);
        point.setMinHeight(14);
        point.setMaxWidth(14);
        point.setMinWidth(14);
        point.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 14px; " +
                "-fx-border-color: #FFDE00; " + 
                "-fx-border-width: 4px; " +
                "-fx-border-radius: 14px; ");
        
        return point;
    }
    
    private void createMainArea() {
        VBox mainAreaVBox = new VBox();
        mainAreaVBox.getStyleClass().add("console-main-area");
        this.createConsoleTextArea();
        mainAreaVBox.getChildren().add(this.consoleTextArea);
        this.mainArea = mainAreaVBox;
    }
    
    private void createConsoleTextArea() {
        TextArea textArea = new TextArea();
        textArea.setTextFormatter(this.createTextFormatter());
        textArea.addEventFilter(KEY_PRESSED, this.createEnterKeyInterceptor());
        textArea.addEventFilter(KEY_PRESSED, this.createCtrlZYKeyCombinationInterceptor());
        textArea.addEventFilter(KEY_PRESSED, this.createUpDownArrowsInterceptor());
        textArea.setMinHeight(200);
        textArea.setMinWidth(500);
        textArea.getStyleClass().add("console-text-area");
        this.windowResizer.affect(textArea); 
        textArea.setContextMenu(this.createContextMenu());
        this.consoleTextArea = textArea;
    }
    
    private ContextMenu createContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(
                this.createClearMenuItem(), 
                this.createCloseMenuItem(), 
                this.createDefualtSizeMenuItem(),
                this.createSettingsMenuItem());
        contextMenu.getItems()
                .stream()
                .forEach(menuItem -> {
                    menuItem.getStyleClass().add("console-menu-item");
                    menuItem.setGraphic(this.createMenuItemPoint());
                });
        return contextMenu;
    }
    
    private Label createMenuItemPoint() {        
        Label point = new Label();
        point.getStyleClass().add("console-menu-item-point");
        return point;
    }
    
    private MenuItem createClearMenuItem() {
        MenuItem clear = new MenuItem("clear");
        clear.setOnAction(event -> {
            synchronized ( this.consoleTextAreaLock ) {
                this.deleteCommitedTextAllowed.set(true);
                this.consoleTextArea.setText("Beam > ");
                this.consoleTextArea.commitValue();
                this.deleteCommitedTextAllowed.set(false);
                this.consoleCommitedLength.set("Beam > ".length());
                this.consoleTextArea.positionCaret("Beam > ".length());
            }    
        });
        return clear;
    }
    
    private MenuItem createCloseMenuItem() {
        MenuItem close = new MenuItem("close");
        close.setOnAction(event -> {
            this.hide();
        });
        return close;
    }
    
    private MenuItem createDefualtSizeMenuItem() {
        MenuItem defualtSize = new MenuItem("default size");
        defualtSize.setOnAction(event -> {
            this.windowResizer.affectableToDefaultSize();
        });
        return defualtSize;
    }
    
    private MenuItem createSettingsMenuItem() {
        MenuItem settings = new MenuItem("settings");
        settings.setOnAction(event -> {
            // TODO MIDDLE
        });
        return settings;
    }
    
    private EventHandler<KeyEvent> createUpDownArrowsInterceptor() {
        return (keyEvent) -> {            
            if ( keyEvent.getCode().equals(UP) ) {
                synchronized ( this.consoleTextAreaLock ) {
                    this.changeUncommitedTextTo(this.consoleInputBuffer.toLastAndGet());
                }    
                keyEvent.consume();
            } else if ( keyEvent.getCode().equals(DOWN) ) {
                synchronized ( this.consoleTextAreaLock ) {
                    this.changeUncommitedTextTo(this.consoleInputBuffer.toFirstAndGet());
                }    
                keyEvent.consume();
            }
        };
    }
    
    private void changeUncommitedTextTo(String bufferedInput) {
        if ( bufferedInput.isEmpty() ) {
            return;
        }
        this.consoleTextArea.replaceText(
                this.consoleCommitedLength.get(), 
                this.consoleTextArea.getText().length(), 
                bufferedInput);
        this.consoleTextArea.commitValue();
        this.consoleTextArea.positionCaret(this.consoleTextArea.getText().length());
    }
    
    private EventHandler<KeyEvent> createCtrlZYKeyCombinationInterceptor() {
        KeyCodeCombination ctrlZ = new KeyCodeCombination(Z, SHORTCUT_DOWN);
        KeyCodeCombination ctrlY = new KeyCodeCombination(Y, SHORTCUT_DOWN);
        return (keyEvent) -> {
            if ( ctrlZ.match(keyEvent) || ctrlY.match(keyEvent) ) {
                keyEvent.consume();
            }
        };
    }
    
    private EventHandler<KeyEvent> createEnterKeyInterceptor() {
        return (keyEvent) -> {
            if ( keyEvent.getCode().equals(ENTER) ) {
                synchronized ( this.consoleTextAreaLock ) {
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
            }
        };
    }
    
    private void startListenBlockingConsoleIncome() {        
        // mutableChangeProcess is a lock shared only between two threads:
        //  - JavaFX Application Thread can only .notify
        //  - JavaFX Console Listener Thread can only .wait
        // There are no any other threads accessing this lock.
        Object mutableChangeProcess = new Object();
        MutableString mutableNewLine = emptyMutableString();
        Runnable consoleTextAreaMutableChange = () -> {
            synchronized ( this.consoleTextAreaLock ) {
                this.consoleTextAreaInternalInputCounter.incrementAndGet();
                this.consoleTextArea.setEditable(false);
                this.consoleTextArea.appendText(mutableNewLine.getAndEmpty());
                this.consoleTextArea.commitValue();
                synchronized ( mutableChangeProcess ) {
                    mutableChangeProcess.notify();
                }
            }
        };
        
        asyncDoIndependently("JavaFX Console Listener Thread", ()-> {
            while ( true ) {                
                try {
                    String newLineIntoConsole = this.blockingIo.blockingGetPrintedString();
                    mutableNewLine.muteTo(newLineIntoConsole);
                    Platform.runLater(consoleTextAreaMutableChange);
                    synchronized ( mutableChangeProcess ) {
                        mutableChangeProcess.wait();
                    }
                } catch (InterruptedException ignore) {
                    // do nothing
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
            this.acceptInput(newInput);
            int newTextLength = change.getControlNewText().length();
            this.consoleCommitedLength.set(newTextLength);
            this.setCaretCorrectPosition(change, newTextLength);
        } else if ( this.consoleTextAreaInternalInputCounter.get() > 0 ) {
            // case: something printed to console as a response, 
            // commited to console, cannot be deleted
            boolean editable = changeText.endsWith(" > ") || changeText.endsWith(" : ");
            this.consoleTextArea.setEditable(editable);
            int newTextLength = change.getControlNewText().length();
            this.consoleCommitedLength.set(newTextLength);
            this.setCaretCorrectPosition(change, newTextLength);
            this.consoleTextAreaInternalInputCounter.decrementAndGet();
        } else {
            // case: usual input, not commited to console, can be deleted.
            this.adjustChangeIfAppliedToCommited(change);
        }            
    }
    
    private void acceptInput(String input) {
        try {
            this.blockingIo.blockingSetString(input);
            if ( input.length() > 1 ) {
                this.consoleInputBuffer.add(input);
            }
        } catch (InterruptedException e) {
            // TODO ?
        }
    }

    private void interceptOnDeleted(Change change) {
        if ( this.deleteCommitedTextAllowed.get() ) {
            return;
        }
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
        return "JavaFX Console";
    }

    @Override
    public void whenStopped() {
        Platform.runLater(() -> this.hide());
    }

    @Override
    public void whenInitiatorAccepted() {
        // TODO ?
    }

    @Override
    public boolean isActiveWhenClosed() {
        return true;
    }
}
