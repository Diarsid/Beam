/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.database.sql;

import diarsid.beam.core.modules.data.DataBaseInitializer;

/**
 *
 * @author Diarsid
 */
public interface SqlDataBaseInitializer extends DataBaseInitializer {
    
    String initializeTableIfNecessaryAndProvideReport(SqlTable table);
}
