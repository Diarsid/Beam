/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package stubs.io;

import java.util.List;
import java.util.Random;

import com.drs.beam.server.entities.task.Task;
import com.drs.beam.server.modules.io.InnerIOModule;

/**
 *
 * @author Diarsid
 */
public class StubInnerIOModule implements InnerIOModule{
    // Fields =============================================================================
    private final String stubIO = "Stub.IO > ";

    // Constructors =======================================================================

    // Methods ============================================================================
    @Override
    public void showTask(Task task){
        System.out.println(stubIO + "showTask: " + task.getTimeOutputString());
    } 
    
    @Override
    public void reportInfo(String... info){
        System.out.println(stubIO + "reportInfo: " + String.join(" | ", info));
    }
    
    @Override
    public void reportMessage(String... info){
        System.out.println(stubIO + "reportMessage: " + String.join(" | ", info));
    }
    
    @Override
    public void reportError(String... error){
        System.out.println(stubIO + "reportError: " + String.join(" | ", error));
    }
    
    @Override
    public void reportErrorAndExitLater(String... error){
        System.out.println(stubIO + "reportErrorAndExit: " + String.join(" | ", error));
    }
    
    @Override
    public void reportException(Exception e, String... description){
        System.out.println(stubIO + "reportException: " + e.getClass().getSimpleName());
        System.out.println(stubIO + "reportException: " + String.join(" | ", description));
    } 
    
    @Override
    public void reportExceptionAndExitLater(Exception e, String... description){
        System.out.println(stubIO + "reportExceptionAndExit: " + e.getClass().getSimpleName());
        System.out.println(stubIO + "reportExceptionAndExit: " + String.join(" | ", description));
    }
    
    @Override
    public int resolveVariantsWithExternalIO(String message, List<String> variants){
        // should return int between 1 and variants.size(), means zero element in list 
        // is first.
        // this method does not used for empty lists.
        int variantsQty = variants.size();
        Random random = new Random();
        return random.nextInt(variantsQty + 1);
    }
}
