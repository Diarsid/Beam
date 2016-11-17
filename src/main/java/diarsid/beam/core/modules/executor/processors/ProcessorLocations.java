/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.processors;

import java.util.List;

/**
 *
 * @author Diarsid
 */
public interface ProcessorLocations {

    List<String> listLocationAndSubPathContent(
            String locationName, String subPath);
    
    List<String> listLocationContent(String locationName);

    void open(List<String> commandParams);     
}
