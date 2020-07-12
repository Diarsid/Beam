/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.Optional;

import diarsid.beam.core.base.analyze.variantsweight.Variants;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.beam.core.modules.data.DaoLocationSubPathChoices;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;
import diarsid.jdbc.transactions.exceptions.TransactionTerminationException;

import static diarsid.beam.core.base.control.flow.Flows.voidFlowDone;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.support.strings.StringUtils.lower;


class H2DaoLocationSubPathChoices 
        extends BeamCommonDao 
        implements DaoLocationSubPathChoices {

    public H2DaoLocationSubPathChoices(DataBase dataBase) {
        super(dataBase);
    }

    @Override
    public boolean saveSingle(LocationSubPath subPath, String pattern) 
            throws DataExtractionException {
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
                                lower(subPath.subPath()), 
                                lower(subPath.name()), 
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
                                subPath.locationName(), 
                                lower(subPath.subPath()), 
                                lower(subPath.name()));
            }
            
            if ( modified != 1 ) {
                transact.rollbackAndProceed();
            }
             
            return true;
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean saveWithVariants(            
            LocationSubPath subPath, 
            String pattern, 
            Variants variants) 
            throws DataExtractionException {
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
            
        } catch (
                TransactionHandledSQLException | 
                TransactionHandledException | 
                TransactionTerminationException e) {
            throw super.logAndWrap(e);
        } 
    }

    @Override
    public boolean isChoiceExistsForSingle(
            LocationSubPath subPath, String pattern) throws DataExtractionException {
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
                            lower(subPath.locationName()), 
                            lower(subPath.subPath()));
        } catch (TransactionHandledException|TransactionHandledSQLException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public Optional<LocationSubPath> getChoiceFor(
            String pattern, Variants variants) throws DataExtractionException {
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
            throw super.logAndWrap(e);
        }
    }
    
    @Override
    public VoidFlow remove(LocationSubPath subPath) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean exist = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT pattern " +
                            "FROM subpath_choices " +
                            "WHERE ( LOWER(pattern) IS ? )", 
                            lower(subPath.pattern()));
            
            if ( ! exist ) {
                return voidFlowDone();
            }
            
            int modified = transact
                    .doUpdateVarargParams(
                            "DELETE " +
                            "FROM subpath_choices " +
                            "WHERE ( LOWER(pattern) IS ? )", 
                            lower(subPath.pattern()));
            
            if ( modified == 1 ) {
                return voidFlowDone();
            } else {
                transact.rollbackAndProceed();
                return voidFlowFail("sub_path removing error: " + modified + " rows modified!");
            }
            
        } catch (TransactionHandledException|TransactionHandledSQLException e) {
            throw super.logAndWrap(e);
        }
    }
    
}
