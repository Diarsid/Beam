/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.core.jsonconversion;

import java.io.Reader;

/**
 *
 * @author Diarsid
 */
public interface Jsonizer {

    String jsonize(Object obj);

    Object objectivize(Class clazz, Reader reader);    
}
