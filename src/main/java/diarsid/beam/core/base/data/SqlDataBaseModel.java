/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.data;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Diarsid
 */
public interface SqlDataBaseModel extends DataBaseModel {
    
    List<SqlTable> tables();
    
    List<SqlConstraint> constraints();
    
    List<SqlObject> objects();
    
    Optional<SqlTable> table(String name);
}
