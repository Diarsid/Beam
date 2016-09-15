/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.os.search.result;

import java.util.List;

/**
 *
 * @author Diarsid
 */
public interface FileSearchSuccess {
    
    boolean hasSingleFoundFile();
    
    String getFoundFile();
    
    List<String> getMultipleFoundFiles();
}