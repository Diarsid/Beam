/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mocks.io;

import java.util.List;
import java.util.Random;

import com.drs.beam.server.entities.task.Task;
import com.drs.beam.server.modules.io.InnerIOModule;

/**
 *
 * @author Diarsid
 */
public class MockInnerIOModule implements InnerIOModule{
    // Fields =============================================================================
    private final String mockIO = "Mock.IO > ";

    // Constructors =======================================================================

    // Methods ============================================================================
    @Override
    public void showTask(Task task){
        System.out.println(mockIO + "showTask: " + task.getTimeOutputString());
    } 
    
    @Override
    public void reportInfo(String... info){
        System.out.println(mockIO + "reportInfo: " + String.join(" | ", info));
    }
    
    @Override
    public void reportMessage(String... info){
        System.out.println(mockIO + "reportMessage: " + String.join(" | ", info));
    }
    
    @Override
    public void reportError(String... error){
        System.out.println(mockIO + "reportError: " + String.join(" | ", error));
    }
    
    @Override
    public void reportErrorAndExit(String... error){
        System.out.println(mockIO + "reportErrorAndExit: " + String.join(" | ", error));
    }
    
    @Override
    public void reportException(Exception e, String... description){
        System.out.println(mockIO + "reportException: " + e.getClass().getSimpleName());
        System.out.println(mockIO + "reportException: " + String.join(" | ", description));
    } 
    
    @Override
    public void reportExceptionAndExit(Exception e, String... description){
        System.out.println(mockIO + "reportExceptionAndExit: " + e.getClass().getSimpleName());
        System.out.println(mockIO + "reportExceptionAndExit: " + String.join(" | ", description));
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
