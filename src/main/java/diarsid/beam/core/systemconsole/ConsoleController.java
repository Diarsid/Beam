/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.IoChoice;
import diarsid.beam.core.control.io.base.IoMessage;
import diarsid.beam.core.control.io.base.IoQuestion;
import diarsid.beam.core.control.io.base.OuterIoEngine;
import diarsid.beam.core.control.io.base.Variant;
import diarsid.beam.core.rmi.RemoteAccessEndpoint;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.control.io.base.IoChoice.choiceNotMade;
import static diarsid.beam.core.systemconsole.SystemConsole.exitSystemConsole;
import static diarsid.beam.core.systemconsole.SystemIO.provideReader;
import static diarsid.beam.core.systemconsole.SystemIO.provideWriter;
import static diarsid.beam.core.util.CollectionUtils.toUnmodifiableList;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsWordInIgnoreCase;

/**
 *
 * @author Diarsid
 */
public class ConsoleController implements OuterIoEngine {
        
    private final BufferedReader reader;
    private final BufferedWriter writer;    
    private final List<String> yesPatterns = toUnmodifiableList("y", "+", "yes", "ye", "true", "enable");
    private final List<String> stopPatterns = toUnmodifiableList(".", "", "s", " ", "-", "false", "disable");
    private final AtomicBoolean operationInProcess;
    private Initiator initiator;
    private RemoteAccessEndpoint remoteAccess;
    private String consoleName;
    
    public ConsoleController() {
        this.reader = provideReader();
        this.writer = provideWriter();
        this.operationInProcess = new AtomicBoolean(false);
        this.consoleName = "default";
    }

    public void setRemoteAccess(RemoteAccessEndpoint remoteAccess) {
        this.remoteAccess = remoteAccess;
    }
    
    public void setName(String name) {
        this.consoleName = name;
    }
    
    private void startConsoleRunner() {        
        new Thread(() -> {
            String command;            
            while ( true ) {
                try {
                    this.writer.write("Beam[1] > ");
                    this.writer.flush();
                    command = this.reader.readLine().trim(); 
                    this.operationInProcess.set(true);
                    if ( ! command.isEmpty() && nonNull(this.initiator) ) {
                        this.remoteAccess.executeCommand(this.initiator, command);
                    }                    
                    this.operationInProcess.set(false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } 
            }
        }).start();
    }

    @Override
    public boolean resolveYesOrNo(String yesOrNoQuestion) throws IOException {
        this.writer.write(format("     > %s", yesOrNoQuestion));
        this.writer.newLine();
        this.writer.write("     > yes/no : ");
        this.writer.flush();
        return resolve();
    }
    
    private boolean resolve() throws IOException {
        String answer = this.reader.readLine().trim();
        if ( answer.isEmpty() ) {
            return false;
        } else {
            return containsWordInIgnoreCase(this.yesPatterns, answer);
        }
    }

    @Override
    public IoChoice resolveVariants(IoQuestion question) throws IOException {
        Variant variant;
        this.writer.write(format("     > %s", question.getQuestion()));
        for (int i = 0; i < question.getVariants().size(); i++) {
            variant = question.getVariants().get(i);
            if ( variant.hasDisplayText() ) {
                this.writer.write(format("       %d : %s", i, variant.getDisplay()));
            } else {
                this.writer.write(format("       %d : %s", i, variant.get()));
            }
        }
        this.writer.write("     > choose: ");
        this.writer.flush();
        return choiceNotMade();
    }

    @Override
    public void report(String string) throws IOException {        
        if ( this.operationInProcess.get() ) {
            this.writer.write(format("     > %s", string));
            this.writer.newLine();
            this.writer.flush();
        } else {
            this.writer.write(format("Beam[3] > %s", string));
            this.writer.newLine();
            this.writer.write("Beam[3] > ");
            this.writer.flush();
        }
    }

    @Override
    public void reportMessage(IoMessage message) throws IOException {
        for (String s : message.getText()) {
            this.writer.write(format("     > %s", s));
        }
        this.writer.newLine();
        this.writer.write("Beam[4] > ");
        this.writer.flush();
    }

    @Override
    public void close() throws IOException {
        System.out.println("closing...");
        exitSystemConsole();
        System.out.println("closed.");
    }

    @Override
    public void acceptInitiator(Initiator initiator) throws IOException {
        System.out.println("ConsoleController accepting initiator: " + initiator.getId());
        this.initiator = initiator;
        this.startConsoleRunner();
    }

    @Override
    public String getName() {
        return this.consoleName;
    }
}
