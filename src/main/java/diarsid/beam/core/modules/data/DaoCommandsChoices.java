/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.domain.patternsanalyze.WeightedVariants;

/**
 *
 * @author Diarsid
 */
public interface DaoCommandsChoices {
    
    boolean isChoiceDoneFor(String original, WeightedVariants variants);
    
    boolean save(InvocationCommand command, WeightedVariants variants);
    
    boolean delete(String original);
    
    boolean delete(InvocationCommand command);
}
