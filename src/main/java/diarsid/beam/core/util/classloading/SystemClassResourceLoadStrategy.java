/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util.classloading;

import java.io.InputStream;

import static java.util.Objects.isNull;


class SystemClassResourceLoadStrategy implements ClassResourceLoadStrategy {
    
    SystemClassResourceLoadStrategy() {
    }

    @Override
    public InputStream loadResourceFor(String className) 
            throws ClassResourceLoadStrategyFailedException {
        InputStream is = ClassLoader
                .getSystemResourceAsStream(className.replace(".", "/")+".class");
        if ( isNull(is) ) {
            throw new ClassResourceLoadStrategyFailedException(this);
        } else {
            return is;
        }        
    }
}
