/*
 * project: Beam
 * author: Diarsid
 */
package old.diarsid.beam.core.modules;

import java.util.List;

import diarsid.beam.core.StoppableBeamModule;
import diarsid.beam.core.modules.tasks.TimeMessage;

/**
 * This interface defines methods for an output within the program.
 * 
 * These methods are used by another parts of the program to report 
 * about messages, exceptions, tasks, events and so on.
 * 
 * An implementation class has to describe concrete ways how exactly 
 * given output information should be displayed according to it`s 
 * types, settings and other requirements.
 * 
 * @author Diarsid
 */
public interface IoInnerModule extends StoppableBeamModule {  
    
    void showTask(TimeMessage task);  
    
    void showTasksNotification(String periodOfNotification, List<TimeMessage> tasks);
    
    void reportInfo(String... info);
    
    void reportMessage(String... info);
    
    void reportError(String... error);
    
    void reportErrorAndExitLater(String... error);
    
    void reportException(Exception e, String... description); 
    
    void reportExceptionAndExitLater(Exception e, String... description); 
    
    boolean askUserYesOrNo(String yesOrNoQuestion);
    
    int  resolveVariants(String message, List<String> variants);
}
