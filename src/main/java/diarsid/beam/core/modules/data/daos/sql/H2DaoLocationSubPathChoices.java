/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.daos.sql;

import java.util.Optional;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.beam.core.modules.data.DaoLocationSubPathChoices;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;
import diarsid.jdbc.transactions.exceptions.TransactionTerminationException;

import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.daos.sql.RowToEntityConversions.ROW_TO_SUBPATH;


class H2DaoLocationSubPathChoices 
        extends BeamCommonDao 
        implements DaoLocationSubPathChoices {

    public H2DaoLocationSubPathChoices(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public boolean save(
            Initiator initiator, 
            LocationSubPath subPath, 
            String pattern, 
            WeightedVariants variants) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean exists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT subpath " +
                            "FROM subpath_choices " +
                            "WHERE ( LOWER(pattern) IS ? )", 
                            lower(pattern));
            
            int modified;
            if ( exists ) {
                modified = transact
                        .doUpdateVarargParams(
                                "UPDATE subpath_choices " +
                                "SET " +
                                "   loc_name = ?, " +
                                "   subpath = ?, " +
                                "   variants_stamp = ? " +
                                "WHERE ( LOWER(pattern) IS ? ) ", 
                                subPath.locationName(), 
                                subPath.subPath(), 
                                variants.stamp(), 
                                lower(pattern));
            } else {
                modified = transact
                        .doUpdateVarargParams(
                                "INSERT INTO subpath_choices (" +
                                "   pattern, " +
                                "   loc_name, " +
                                "   subpath, " +
                                "   variants_stamp) " +
                                "VALUES ( ?, ?, ?, ? ) ", 
                                pattern, 
                                subPath.locationName(), 
                                subPath.subPath(), 
                                variants.stamp());
            }
            
            if ( modified != 1 ) {
                transact.rollbackAndTerminate();
            }
             
            return true;
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            // TODO LOW
            return false;
        } catch (TransactionTerminationException e) {
            // TODO LOW
            return false;
        }
    }

    @Override
    public Optional<LocationSubPath> getChoiceFor(Initiator initiator, String pattern, WeightedVariants variants) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            LocationSubPath.class, 
                            "SELECT loc.loc_name, loc_path, subpath " + 
                            "FROM " + 
                            "    locations AS loc " + 
                            "    JOIN " + 
                            "    subpath_choices AS sc " + 
                            "    ON loc.loc_name = sc.loc_name " + 
                            "WHERE " +
                            "   ( LOWER(pattern) IS ? ) AND " +
                            "   ( LOWER(variants_stamp) IS ? ) ", 
                            ROW_TO_SUBPATH, 
                            lower(pattern), variants.stamp());
        } catch (TransactionHandledException|TransactionHandledSQLException e) {
            // TODO LOW
            return Optional.empty();
        }
    }
    
}
