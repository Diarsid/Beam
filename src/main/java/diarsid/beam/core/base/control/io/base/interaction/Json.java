/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;

import java.util.List;

/**
 *
 * @author Diarsid
 */
public interface Json {
    
    String stringOf(String name);
    
    int intOf(String name);
    
    boolean booleanOf(String name);
    
    Json jsonOf(String name);
    
    List<Json> jsonListOf(String name);
}
