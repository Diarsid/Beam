/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util.classloading;

import java.io.InputStream;


class ModuleClassResourceLoadStrategy implements ClassResourceLoadStrategy {
    
    ModuleClassResourceLoadStrategy() {        
    }

    @Override
    public InputStream loadResourceFor(String className) 
            throws ClassResourceLoadStrategyFailedException {
        // not implemented yet. 
        // requires Java 9 API: Module, Layer.
        throw new ClassResourceLoadStrategyFailedException(this);
    }
}
