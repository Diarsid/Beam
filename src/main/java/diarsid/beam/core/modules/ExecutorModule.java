/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.StoppableBeamModule;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.EmptyCommand;
import diarsid.beam.core.base.control.io.commands.executor.CallBatchCommand;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorDefaultCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenPathCommand;
import diarsid.beam.core.base.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.base.control.io.commands.executor.SeePageCommand;

/**
 *
 * @author Diarsid
 */
public interface ExecutorModule extends StoppableBeamModule {
    
    void openLocation(Initiator initiator, OpenLocationCommand command);
    
    void openPath(Initiator initiator, OpenPathCommand command);
    
    void runProgram(Initiator initiator, RunProgramCommand command);
    
    void callBatch(Initiator initiator, CallBatchCommand command);
    
    void browsePage(Initiator initiator, SeePageCommand command);
    
    void executeDefault(Initiator initiator, ExecutorDefaultCommand command);
    
    void openNotes(Initiator initiator, EmptyCommand command);
    
    void openTargetInNotes(Initiator initiator, ArgumentsCommand command);
    
    void openPathInNotes(Initiator initiator, ArgumentsCommand command);
    
    void listLocation(Initiator initiator, ArgumentsCommand command);
    
    void listPath(Initiator initiator, ArgumentsCommand command);
    
}
