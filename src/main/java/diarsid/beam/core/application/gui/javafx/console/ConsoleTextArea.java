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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

import diarsid.beam.core.base.util.MutableString;

import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyCode.Y;
import static javafx.scene.input.KeyCode.Z;
import static javafx.scene.input.KeyCombination.SHORTCUT_DOWN;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;

import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoIndependently;
import static diarsid.beam.core.base.util.MutableString.emptyMutableString;

/**
 *
 * @author Diarsid
 */
class ConsoleTextArea {
    
    private final JavaFXConsolePlatformWindow console;
    private final AtomicInteger consoleCommitedLength;
    private final AtomicInteger consoleTextAreaInternalInputCounter;
    private final AtomicBoolean deleteCommitedTextAllowed;
    private final Object consoleTextAreaLock;
    
    private TextArea textArea;

    ConsoleTextArea(JavaFXConsolePlatformWindow console) {
        this.console = console;
        this.consoleCommitedLength = new AtomicInteger();
        this.consoleTextAreaInternalInputCounter = new AtomicInteger();
        this.deleteCommitedTextAllowed = new AtomicBoolean();
        this.consoleTextAreaLock = new Object();
    }
    
    final void imitateEnterPressed() {
        this.textArea.appendText("\n");
    }
    
    final void clear() {
        synchronized ( this.consoleTextAreaLock ) {
            this.deleteCommitedTextAllowed.set(true);
            this.textArea.setText("Beam > ");
            this.textArea.commitValue();
            this.deleteCommitedTextAllowed.set(false);
            this.consoleCommitedLength.set("Beam > ".length());
            this.textArea.positionCaret("Beam > ".length());
        }
    }
    
    final boolean hasClearableContent() {
        return this.textArea.getText().length() > "Beam > ".length();
    }

    final String text() {
        return this.textArea.getText();
    }

    final int caretPosition() {
        return this.textArea.getCaretPosition();
    }
    
    final void requestFocus() {
        this.textArea.requestFocus();
    }
    
    final void imitateCommandInput(String command) {
        synchronized ( this.consoleTextAreaLock ) {
            this.textArea.appendText(command);
            this.imitateEnterPressed();
        }
    }
    
    final TextArea jfxTextArea() {
        TextArea newTextArea = new TextArea();
        newTextArea.setTextFormatter(this.createTextFormatter());
        newTextArea.addEventFilter(KEY_PRESSED, this.createEnterKeyInterceptor());
        newTextArea.addEventFilter(KEY_PRESSED, this.createCtrlZYKeyCombinationInterceptor());
        newTextArea.addEventFilter(KEY_PRESSED, this.createUpDownArrowsInterceptor());
        newTextArea.setMinHeight(200);
        newTextArea.setMinWidth(500);
        newTextArea.getStyleClass().add("console-text-area");
        this.textArea = newTextArea;
        return this.textArea;
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
    
    private void changeUncommitedTextTo(String bufferedInput) {
        if ( bufferedInput.isEmpty() ) {
            return;
        }
        this.textArea.replaceText(
                this.consoleCommitedLength.get(), 
                this.textArea.getText().length(), 
                bufferedInput);
        this.textArea.commitValue();
        this.textArea.positionCaret(this.textArea.getText().length());
    }
    
    private EventHandler<KeyEvent> createUpDownArrowsInterceptor() {
        return (keyEvent) -> {            
            if ( keyEvent.getCode().equals(UP) ) {
                synchronized ( this.consoleTextAreaLock ) {
                    this.changeUncommitedTextTo(this.console.inputBuffer().toLastAndGet());
                }    
                keyEvent.consume();
            } else if ( keyEvent.getCode().equals(DOWN) ) {
                synchronized ( this.consoleTextAreaLock ) {
                    this.changeUncommitedTextTo(this.console.inputBuffer().toFirstAndGet());
                }    
                keyEvent.consume();
            }
        };
    }
    
    private EventHandler<KeyEvent> createEnterKeyInterceptor() {
        return (keyEvent) -> {
            if ( ! keyEvent.getCode().equals(ENTER) ) {
                return;
            }
            
            synchronized ( this.consoleTextAreaLock ) {
                if ( this.textArea.getCaretPosition() == 
                        this.textArea.getText().length() ) {
                    return;
                }

                int caretPosition = this.textArea.getCaretPosition();
                int rawInputTextLength = this.textArea.getText().length();
                int commitedTextLength = this.consoleCommitedLength.get();

                if ( caretPosition < rawInputTextLength ) {
                    if ( caretPosition >= commitedTextLength ) {
                        keyEvent.consume();
                        this.imitateEnterPressed();
                    } else {
                        this.textArea.deleteText(commitedTextLength, rawInputTextLength);
                        keyEvent.consume();
                    }
                }
            }
        };
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
            this.textArea.setEditable(false);                
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
            this.textArea.setEditable(editable);
            int newTextLength = change.getControlNewText().length();
            this.consoleCommitedLength.set(newTextLength);
            this.setCaretCorrectPosition(change, newTextLength);
            this.consoleTextAreaInternalInputCounter.decrementAndGet();
        } else {
            // case: usual input, not commited to console, can be deleted.
            this.adjustChangeIfAppliedToCommited(change);
        }            
    }

    private void interceptOnDeleted(Change change) {
        if ( this.deleteCommitedTextAllowed.get() ) {
            return;
        }
        int commitedTextLength = this.consoleCommitedLength.get();
        if ( change.getRangeStart() < commitedTextLength ) {
            if ( change.getRangeEnd() > commitedTextLength ) {
                this.textArea.deleteText(
                        commitedTextLength, this.textArea.getText().length());
            }
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
    
    final void startListenBlockingConsoleIncome() {        
        // mutableChangeProcess is a lock shared only between two threads:
        //  - JavaFX Application Thread can only .notify
        //  - JavaFX Console Listener Thread can only .wait
        // There are no any other threads accessing this lock.
        Object mutableChangeProcess = new Object();
        MutableString mutableNewLine = emptyMutableString();
        Runnable consoleTextAreaMutableChange = () -> {
            synchronized ( this.consoleTextAreaLock ) {
                this.consoleTextAreaInternalInputCounter.incrementAndGet();
                this.textArea.setEditable(false);
                this.textArea.appendText(mutableNewLine.getAndEmpty());
                this.textArea.commitValue();
                synchronized ( mutableChangeProcess ) {
                    mutableChangeProcess.notify();
                }
            }
        };
        
        asyncDoIndependently("JavaFX Console Listener Thread", ()-> {
            while ( true ) {                
                try {
                    mutableNewLine.muteTo(this.console.blockingIo().blockingGetPrintedString());
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
    
    private void acceptInput(String input) {
        try {
            this.console.blockingIo().blockingSetString(input);
            if ( input.length() > 1 ) {
                this.console.inputBuffer().add(input);
            }
        } catch (InterruptedException e) {
            // TODO ?
        }
    }
    
}
