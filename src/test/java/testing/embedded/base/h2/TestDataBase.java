/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testing.embedded.base.h2;

import diarsid.beam.core.base.data.DataBase;

/**
 *
 * @author Diarsid
 */
public interface TestDataBase extends DataBase {
        
    void setupRequiredTable(String tableCreationSQLScript);
    
    int countRowsInTable(String tableName);
    
    boolean ifAllConnectionsReleased();
}
