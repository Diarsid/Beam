/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.Optional;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;

/**
 *
 * @author Diarsid
 */
public interface DaoPatternChoices {
    
    boolean isChoiceMatchTo(String original, String extended, WeightedVariants variants);
    
    Optional<String> findChoiceFor(String original, WeightedVariants variants);
    
    boolean save(InvocationCommand command, WeightedVariants variants);
    
    boolean delete(String original);
    
    boolean delete(InvocationCommand command);
}
