/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.daos.sql;

import java.util.Optional;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.modules.data.DaoCommandsChoices;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.daos.sql.RowToEntityConversions.ROW_TO_COMMAND_TYPE;


class H2DaoCommandsChoices
        extends BeamCommonDao 
        implements DaoCommandsChoices {

    H2DaoCommandsChoices(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public boolean isChoiceDoneFor(String original, WeightedVariants variants) {
        debug("[DAO COMMANDS CHOICES] is done '" + original + "' for " + variants.stamp() + " ?");
        try {
            return super.openDisposableTransaction()
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM commands_choices " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_variants_stamp IS ? )", 
                            lower(original), variants.stamp());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;            
        }
    }

    @Override
    public Optional<CommandType> isTypeChoiceDoneFor(String original, WeightedVariants variants) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(CommandType.class,
                            "SELECT * " +
                            "FROM commands_choices " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_variants_stamp IS ? )", 
                            ROW_TO_COMMAND_TYPE,
                            lower(original), variants.stamp());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return Optional.empty();            
        }
    }  
    @Override
    public boolean save(InvocationCommand command, WeightedVariants variants) {
        debug("[DAO COMMANDS CHOICES] save: " + command.stringify() + ", stamp: " + variants.stamp());
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean choiceExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM commands_choices " +
                            "WHERE com_original IS ? ", 
                            lower(command.originalArgument()));
            
            int modified = 0;
            if ( choiceExists ) {
                modified = transact
                        .doUpdateVarargParams(
                                "UPDATE commands_choices " +
                                "SET " +
                                "   com_variants_stamp = ?, " +
                                "   com_type = ? " +
                                "WHERE com_original IS ? ", 
                                variants.stamp(), command.type(), lower(command.originalArgument()));
            } else {
                modified = transact
                        .doUpdateVarargParams(
                                "INSERT INTO commands_choices (" +
                                "   com_original, " +
                                "   com_type, " +
                                "   com_variants_stamp) " +
                                "VALUES ( ?, ?, ? ) ", 
                                lower(command.originalArgument()), command.type(), variants.stamp());
            }
            
            transact
                    .ifTrue( modified != 1 )
                    .rollbackAndProceed();
            
            debug("[DAO COMMANDS CHOICES] saved: " + ( modified == 1 ));
            return ( modified == 1 );
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;            
        }
    }

    @Override
    public boolean delete(String original) {
        try {
            return super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands_choices " +
                            "WHERE LOWER(com_original) IS ? ", 
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
