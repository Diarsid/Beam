/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.database.sql;

/**
 *
 * @author Diarsid
 */
public interface SqlObject {
    
    String name();
    
    String creationScript();
    
    SqlObjectType type();
    
}
