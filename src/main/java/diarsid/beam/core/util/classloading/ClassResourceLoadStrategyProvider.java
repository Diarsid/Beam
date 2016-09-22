/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util.classloading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Diarsid
 */
class ClassResourceLoadStrategyProvider {
    
    private final List<ClassResourceLoadStrategy> availableStrategies;
    
    ClassResourceLoadStrategyProvider(ClassResourceLoadStrategy... strategies) {
        this.availableStrategies = new ArrayList<>(Arrays.asList(strategies));
    }    
    
    ClassResourceLoadStrategy getFirstStrategy() {
        return this.availableStrategies.get(0);
    }
    
    ClassResourceLoadStrategy getNewStrategyInsteadOfFalied(
            ClassResourceLoadStrategyFailedException e) 
            throws ClassResourceLoadFailedException {
        int failedStrategyIndex = this.availableStrategies.indexOf(e.getFailedStrategy());
        if ( this.hasMoreStrategies(failedStrategyIndex) ) {
            return this.getStrategyNextTo(failedStrategyIndex);
        } else {
            throw new ClassResourceLoadFailedException("All strategies failed. ");
        }
    }
    
    private boolean hasMoreStrategies(int failedStrIndex) {
        return ( this.availableStrategies.size() > (failedStrIndex + 1) );
    }
    
    private ClassResourceLoadStrategy getStrategyNextTo(int failedStrategyIndex) {
        return this.availableStrategies.get(failedStrategyIndex + 1);
    }
}
