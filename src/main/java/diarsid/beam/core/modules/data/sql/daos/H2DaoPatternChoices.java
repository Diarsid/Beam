/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.Optional;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.modules.data.BeamCommonDao;
import diarsid.beam.core.modules.data.DaoPatternChoices;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_EXTENDED;


class H2DaoPatternChoices
        extends BeamCommonDao 
        implements DaoPatternChoices {

    H2DaoPatternChoices(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public boolean hasMatchOf(String original, String extended, WeightedVariants variants) {
        try {
            return super.openDisposableTransaction()
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM pattern_choices " +
                            "WHERE " +
                            "   ( LOWER(original) IS ? ) AND " +
                            "   ( LOWER(extended) IS ? ) AND " +
                            "   ( variants_stamp IS ? )", 
                            lower(original), lower(extended), variants.stamp());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;            
        }
    }

    @Override
    public Optional<String> findChoiceFor(String original, WeightedVariants variants) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            ROW_TO_EXTENDED,
                            "SELECT * " +
                            "FROM pattern_choices " +
                            "WHERE ( LOWER(original) IS ? ) AND ( variants_stamp IS ? )", 
                            lower(original), variants.stamp());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return Optional.empty();            
        }
    }  
    
    @Override
    public boolean save(String original, String extended, WeightedVariants variants) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean choiceExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM pattern_choices " +
                            "WHERE LOWER(original) IS ? ", 
                            lower(original));
            
            int modified = 0;
            if ( choiceExists ) {
                modified = transact
                        .doUpdateVarargParams(
                                "UPDATE pattern_choices " +
                                "SET " +
                                "   variants_stamp = ?, " +
                                "   extended = ? " +
                                "WHERE LOWER(original) IS ? ", 
                                variants.stamp(), 
                                lower(extended), 
                                lower(original));
            } else {
                modified = transact
                        .doUpdateVarargParams(
                                "INSERT INTO pattern_choices (" +
                                "   original, " +
                                "   extended, " +
                                "   variants_stamp) " +
                                "VALUES ( ?, ?, ? ) ", 
                                lower(original), 
                                lower(extended), 
                                variants.stamp());
            }
            
            transact
                    .ifTrue( modified != 1 )
                    .rollbackAndProceed();
            
            return ( modified == 1 );
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;            
        }
    }
    
    @Override
    public boolean save(InvocationCommand command, WeightedVariants variants) {
        return this.save(command.originalArgument(), command.extendedArgument(), variants);
    }

    @Override
    public boolean delete(String original) {
        try {
            return super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM pattern_choices " +
                            "WHERE LOWER(original) IS ? ", 
                            lower(original))
                    == 1;
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;            
        }
    }

    @Override
    public boolean delete(InvocationCommand command) {
        return this.delete(command.originalArgument());
    }
}
