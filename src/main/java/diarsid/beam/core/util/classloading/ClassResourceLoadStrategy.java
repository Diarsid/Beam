/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.util.classloading;

import java.io.InputStream;

/**
 *
 * @author Diarsid
 */
interface ClassResourceLoadStrategy {    
    
    InputStream loadResourceFor(String className) 
            throws ClassResourceLoadStrategyFailedException;
}
