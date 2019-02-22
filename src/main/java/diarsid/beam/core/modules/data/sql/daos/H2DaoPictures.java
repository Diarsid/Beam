/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.Optional;

import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Picture;
import diarsid.beam.core.modules.data.DaoPictures;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;

import static diarsid.support.strings.StringUtils.lower;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_IMAGE;

/**
 *
 * @author Diarsid
 */
class H2DaoPictures 
        extends BeamCommonDao 
        implements DaoPictures {
    
    H2DaoPictures(DataBase dataBase) {
        super(dataBase);
    }

    @Override
    public Optional<Picture> getByName(String name) throws DataExtractionException {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            ROW_TO_IMAGE, 
                            "SELECT name, bytes " +
                            "FROM images " +
                            "WHERE LOWER(name) IS ? ", 
                            lower(name));
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean save(Picture picture) throws DataExtractionException {
        if ( picture.hasNoData() ) {
            throw new DataExtractionException(format("image '%s' data is empty.", picture.name()));
        }
        
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean exists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT name " +
                            "FROM images " +
                            "WHERE LOWER(name) IS ? ", 
                            lower(picture.name()));
            
            int mofidied;
            if ( exists ) {
                mofidied = transact
                        .doUpdateVarargParams(
                                "UPDATE images " +
                                "SET bytes = ? " +
                                "WHERE LOWER(name) IS ? ", 
                                picture.bytes(), lower(picture.name()));
            } else {
                mofidied = transact
                        .doUpdateVarargParams(
                                "INSERT INTO images (name, bytes) " +
                                "VALUES ( ?, ? ) ", 
                                picture.name(), picture.bytes());                
            }
            
            if ( mofidied == 1 ) {
                return true;
            } else {
                transact.rollbackAndProceed();
                return false;
            }
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean removeByName(String name) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            int removed = transact
                    .doUpdateVarargParams(
                            "DELETE FROM images " +
                            "WHERE LOWER(name) IS ? ", 
                            lower(name));
            
            if ( removed == 1 || removed == 0 ) {
                return true;
            } else {
                transact.rollbackAndProceed();
                return false;
            }
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean remove(Picture image) throws DataExtractionException {
        return this.removeByName(image.name());
    }
}
