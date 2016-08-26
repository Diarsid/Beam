/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.context;

import java.util.List;

/**
 *
 * @author Diarsid
 */
public interface ExecutorContextLifecycleController {
    
    void createContextForCommand(List<String> commandParams);
    
    void destroyCurrentContext();    
}
