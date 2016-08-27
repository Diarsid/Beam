/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.processors;

import java.util.List;

import diarsid.beam.core.modules.executor.workflow.OperationResult;

/**
 *
 * @author Diarsid
 */
public interface ProcessorLocations {

    List<String> listLocationContent(String locationName);

    OperationResult open(List<String> commandParams); 
    
    boolean ifCommandLooksLikeLocationAndPath(List<String> commandParams);
}
