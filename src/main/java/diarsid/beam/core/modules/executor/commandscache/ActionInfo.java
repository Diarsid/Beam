/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.commandscache;

import java.util.List;

/**
 *
 * @author Diarsid
 */
public interface ActionInfo {

    String getActionArgument();

    List<String> getActionVariants();    
}
