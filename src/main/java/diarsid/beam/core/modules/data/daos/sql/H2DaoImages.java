/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.daos.sql;

import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.domain.entities.Image;
import diarsid.beam.core.modules.data.DaoImages;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;

import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.daos.sql.RowToEntityConversions.ROW_TO_IMAGE;

/**
 *
 * @author Diarsid
 */
class H2DaoImages 
        extends BeamCommonDao 
        implements DaoImages {
    
    H2DaoImages(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public Optional<Image> getByName(Initiator initiator, String name) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            Image.class, 
                            "SELECT name, bytes " +
                            "FROM images " +
                            "WHERE LOWER(name) IS ? ", 
                            ROW_TO_IMAGE, 
                            lower(name));
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoImages.class, ex);
            super.ioEngine().report(initiator, format("cannot get '%s' image.", name));
            return Optional.empty();
        }
    }

    @Override
    public boolean save(Initiator initiator, Image image) {
        if ( image.hasData() ) {
            this.ioEngine().report(initiator, format("image '%s' data is empty.", image.name()));
            return false;
        }
        
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean exists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT name " +
                            "FROM images " +
                            "WHERE LOWER(name) IS ? ", 
                            lower(image.name()));
            
            int mofidied;
            if ( exists ) {
                mofidied = transact
                        .doUpdateVarargParams(
                                "UPDATE images " +
                                "SET bytes = ? " +
                                "WHERE LOWER(name) IS ? ", 
                                image.bytes(), lower(image.name()));
            } else {
                mofidied = transact
                        .doUpdateVarargParams(
                                "INSERT INTO images (name, bytes) " +
                                "VALUES ( ?, ? ) ", 
                                image.name(), image.bytes());                
            }
            
            if ( mofidied == 1 ) {
                return true;
            } else {
                transact.rollbackAndProceed();
                return false;
            }
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoImages.class, ex);
            super.ioEngine().report(initiator, format("cannot save '%s' image.", image.name()));
            return false;
        }
    }

    @Override
    public boolean removeByName(Initiator initiator, String name) {
        try (JdbcTransaction transact = super.openTransaction()) {
            int removed = transact
                    .doUpdateVarargParams(
                            "DELETE FROM images " +
                            "WHERE LOWER(name) IS ? ", 
                            lower(name));
            
            if ( removed == 1 ) {
                return true;
            } else {
                transact.rollbackAndProceed();
                return false;
            }
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoImages.class, ex);
            super.ioEngine().report(initiator, format("cannot remove '%s' image.", name));
            return false;
        }
    }

    @Override
    public boolean remove(Initiator initiator, Image image) {
        return this.removeByName(initiator, image.name());
    }
}
