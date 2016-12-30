/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.base;

import old.diarsid.beam.core.modules.data.DataBase;

/**
 *
 * @author Diarsid
 */
public interface TestDataBase extends DataBase {
    
    void setupRequiredTable(String tableCreationSQLScript);
}
