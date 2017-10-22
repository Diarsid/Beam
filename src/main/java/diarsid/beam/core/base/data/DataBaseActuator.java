/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.data;

import java.util.List;

import static java.lang.String.format;

import static diarsid.beam.core.base.data.DataBaseType.SQL;

/**
 *
 * @author Diarsid
 */
public interface DataBaseActuator {
    
    public static DataBaseActuator getActuatorFor(DataBase dataBase, DataBaseModel dataBaseModel) 
            throws DataBaseActuationException {
        if ( dataBase.type().equals(SQL) ) {
            if ( dataBaseModel.type().equals(SQL) ) {
                return new SqlDataBaseActuator(dataBase, (SqlDataBaseModel) dataBaseModel);
            } else {
                throw new DataBaseActuationException(format(
                        "DataBaseModels of %s type is not supported.", 
                        dataBaseModel.type().name()));
            }            
        } else {
            throw new DataBaseActuationException(format(
                    "DataBases of %s type is not supported.", 
                    dataBase.type().name()));
        }
    }
    
    List<String> actuateAndGetReport() throws DataBaseActuationException;
    
}
