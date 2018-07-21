/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.Optional;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.beam.core.modules.data.BeamCommonDao;
import diarsid.beam.core.modules.data.DaoLocationSubPathChoices;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;
import diarsid.jdbc.transactions.exceptions.TransactionTerminationException;

import static diarsid.beam.core.base.control.flow.Flows.voidFlowCompleted;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.util.StringUtils.lower;


class H2DaoLocationSubPathChoices 
        extends BeamCommonDao 
        implements DaoLocationSubPathChoices {

    public H2DaoLocationSubPathChoices(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public boolean saveSingle(Initiator initiator, LocationSubPath subPath, String pattern) {
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
                                subPath.name(), 
                                lower(subPath.subPath()), 
                                lower(subPath.fullName()), 
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
                                lower(pattern), 
                                subPath.name(), 
                                lower(subPath.subPath()), 
                                lower(subPath.fullName()));
            }
            
            if ( modified != 1 ) {
                transact.rollbackAndProceed();
            }
             
            return true;
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            // TODO LOW
            return false;
        }
    }

    @Override
    public boolean saveWithVariants(
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
                                subPath.name(), 
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
                                subPath.name(), 
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
    public boolean isChoiceExistsForSingle(
            Initiator initiator, LocationSubPath subPath, String pattern) {
        try {
            return super.openDisposableTransaction()
                    .doesQueryHaveResultsVarargParams(
                            "SELECT pattern " +
                            "FROM subpath_choices " +
                            "WHERE " +
                            "    ( LOWER(pattern) IS ? ) AND " +
                            "    ( LOWER(loc_name) IS ? ) AND " +
                            "    ( LOWER(subpath) IS ? )", 
                            lower(pattern), 
                            lower(subPath.name()), 
                            lower(subPath.subPath()));
        } catch (TransactionHandledException|TransactionHandledSQLException e) {
            // TODO
            return false;
        }
    }

    @Override
    public Optional<LocationSubPath> getChoiceFor(
            Initiator initiator, String pattern, WeightedVariants variants) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams( 
                            (row) -> {
                                return new LocationSubPath(
                                        pattern, 
                                        row.get("loc_name", String.class),
                                        row.get("loc_path", String.class),
                                        row.get("subpath", String.class));
                            }, 
                            "SELECT loc.loc_name, loc_path, subpath " + 
                            "FROM " + 
                            "    locations AS loc " + 
                            "    JOIN " + 
                            "    subpath_choices AS sc " + 
                            "    ON loc.loc_name = sc.loc_name " + 
                            "WHERE " +
                            "   ( LOWER(pattern) IS ? ) AND " +
                            "   ( LOWER(variants_stamp) IS ? ) ",
                            lower(pattern), variants.stamp());
        } catch (TransactionHandledException|TransactionHandledSQLException e) {
            // TODO LOW
            return Optional.empty();
        }
    }
    
    @Override
    public VoidFlow remove(Initiator initiator, LocationSubPath subPath) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean exist = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT pattern " +
                            "FROM subpath_choices " +
                            "WHERE ( LOWER(pattern) IS ? )", 
                            lower(subPath.pattern()));
            
            if ( ! exist ) {
                return voidFlowCompleted();
            }
            
            int modified = transact
                    .doUpdateVarargParams(
                            "DELETE " +
                            "FROM subpath_choices " +
                            "WHERE ( LOWER(pattern) IS ? )", 
                            lower(subPath.pattern()));
            
            if ( modified == 1 ) {
                return voidFlowCompleted();
            } else {
                transact.rollbackAndProceed();
                return voidFlowFail("sub_path removing error: " + modified + " rows modified!");
            }
            
        } catch (TransactionHandledException|TransactionHandledSQLException e) {
            // TODO LOW
            return voidFlowFail(e.getMessage());
        }
    }
    
}
