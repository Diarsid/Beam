/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.domain.entities.Picture;
import diarsid.beam.core.modules.data.BeamCommonDao;
import diarsid.beam.core.modules.data.DaoPictures;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;

import static diarsid.beam.core.base.util.Logging.logFor;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_IMAGE;

/**
 *
 * @author Diarsid
 */
class H2DaoPictures 
        extends BeamCommonDao 
        implements DaoPictures {
    
    H2DaoPictures(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public Optional<Picture> getByName(Initiator initiator, String name) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            ROW_TO_IMAGE, 
                            "SELECT name, bytes " +
                            "FROM images " +
                            "WHERE LOWER(name) IS ? ", 
                            lower(name));
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            logFor(this).error(e.getMessage(), e);
            super.ioEngine().report(initiator, format("cannot get '%s' image.", name));
            return Optional.empty();
        }
    }

    @Override
    public boolean save(Initiator initiator, Picture picture) {
        if ( picture.hasNoData() ) {
            this.ioEngine().report(initiator, format("image '%s' data is empty.", picture.name()));
            return false;
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
            logFor(this).error(e.getMessage(), e);
            super.ioEngine().report(initiator, format("cannot save '%s' image.", picture.name()));
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
            
            if ( removed == 1 || removed == 0 ) {
                return true;
            } else {
                transact.rollbackAndProceed();
                return false;
            }
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            logFor(this).error(e.getMessage(), e);
            super.ioEngine().report(initiator, format("cannot remove '%s' image.", name));
            return false;
        }
    }

    @Override
    public boolean remove(Initiator initiator, Picture image) {
        return this.removeByName(initiator, image.name());
    }
}
