/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.external.sysconsole.modules;

import java.io.IOException;
import java.util.List;

import diarsid.beam.core.entities.Location;
import diarsid.beam.core.entities.WebPage;
import diarsid.beam.core.modules.executor.StoredExecutorCommand;
import diarsid.beam.core.modules.tasks.TaskMessage;

import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface ConsolePrinterModule extends GemModule {

    void printBeam() throws IOException;

    void printBeamErrorWithMessageLn(String[] message) throws IOException;

    void printBeamWithMessage(String s) throws IOException;

    void printBeamWithMessageLn(String... message) throws IOException;

    void printBeamWithUnderLn(String... message) throws IOException;

    void printChoices(List<String> choices) throws IOException;

    void printCommands(List<StoredExecutorCommand> commands) throws IOException;

    void printHelp();

    void printLocations(List<Location> locations) throws IOException;
    
    void printLocationContent(List<String> locationContent) throws IOException;

    void printSpace(String s) throws IOException;

    void printSpaceLn(String s) throws IOException;

    void printTasks(String label, List<TaskMessage> tasks) throws IOException;

    void printUnder(String s) throws IOException;

    void printUnderLn(String s) throws IOException;
    
    void printDirs(List<String> dirs) throws IOException;

    void printWebPages(List<WebPage> pages, boolean compressOutput) throws IOException;
    
    void printTimeFormats() throws IOException;
    
    void exitMessage() throws IOException;
    
    void showTask(TaskMessage task) throws IOException;
}
