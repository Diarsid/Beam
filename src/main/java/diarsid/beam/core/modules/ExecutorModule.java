/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.StoppableBeamModule;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.executor.CallBatchCommand;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorDefaultCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationTargetCommand;
import diarsid.beam.core.base.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.base.control.io.commands.executor.BrowsePageCommand;

/**
 *
 * @author Diarsid
 */
public interface ExecutorModule extends StoppableBeamModule {
    
    void openLocation(Initiator initiator, OpenLocationCommand command);
    
    void openLocationTarget(Initiator initiator, OpenLocationTargetCommand command);
    
    void runProgram(Initiator initiator, RunProgramCommand command);
    
    void callBatch(Initiator initiator, CallBatchCommand command);
    
    void browsePage(Initiator initiator, BrowsePageCommand command);
    
    void executeDefault(Initiator initiator, ExecutorDefaultCommand command);
    
    void listLocation(Initiator initiator, ArgumentsCommand command);
    
    void listPath(Initiator initiator, ArgumentsCommand command);
    
}
