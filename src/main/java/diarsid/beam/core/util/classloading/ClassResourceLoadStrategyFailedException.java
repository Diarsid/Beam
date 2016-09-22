/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.util.classloading;

/**
 *
 * @author Diarsid
 */
class ClassResourceLoadStrategyFailedException extends Exception {

    private final ClassResourceLoadStrategy failedStrategyImplClass;
    
    ClassResourceLoadStrategyFailedException(ClassResourceLoadStrategy failedStrategyImplClass) {
        this.failedStrategyImplClass = failedStrategyImplClass;
    }
    
    ClassResourceLoadStrategy getFailedStrategy() {
        return this.failedStrategyImplClass;
    }
}
