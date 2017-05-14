/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.database.sql;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.modules.data.DataBaseModel;

/**
 *
 * @author Diarsid
 */
public interface SqlDataBaseModel extends DataBaseModel {
    
    List<SqlTable> tables();
    
    Optional<SqlTable> getByName(String name);
}
