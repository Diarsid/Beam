/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util.classloading;

import java.io.InputStream;

import static java.util.Objects.isNull;

/**
 *
 * @author Diarsid
 */
class ClassResourceLoader {
    
    private final ClassResourceLoadStrategyProvider strategyProvider;
    private ClassResourceLoadStrategy resourceLoadStrategy;    
    
    ClassResourceLoader(ClassResourceLoadStrategyProvider strategyProvider) {
        this.strategyProvider = strategyProvider;
        this.resourceLoadStrategy = strategyProvider.getFirstStrategy();
    }
    
    InputStream getResourcesAsStream(String className) throws ClassResourceLoadFailedException {
        InputStream stream = null;
        while ( isNull(stream) ) {
            try {
                stream = this.resourceLoadStrategy.loadResourceFor(className);
            } catch (ClassResourceLoadStrategyFailedException e) {
                this.replaceFailedStrategyIfAvailable(e);
            } 
        }
        return stream;
    }
    
    private void replaceFailedStrategyIfAvailable(ClassResourceLoadStrategyFailedException e) 
            throws ClassResourceLoadFailedException {
        this.resourceLoadStrategy = this.strategyProvider.getNewStrategyInsteadOfFalied(e);
    }
}
