package diarsid.beam.core.modules.data.sql.daos;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.SqlUtil.lowerWildcardAfter;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_INVOCATION_COMMAND;
import static diarsid.support.log.Logging.logFor;


abstract class H2DaoCommandsV0 
        extends BeamCommonDao
        implements DaoCommands {
        
    H2DaoCommandsV0(DataBase dataBase) {
        super(dataBase);
    }
    
    @Override
    public Optional<InvocationCommand> getByExactOriginalAndType(
            String original, CommandType type) 
            throws DataExtractionException {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( com_type IS ? ) AND ( LOWER(com_original) IS ? ) ",
                            type.name(), lower(original));
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public List<InvocationCommand> getByExactOriginalOfAnyType(String original) 
            throws DataExtractionException {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE LOWER(com_original) IS ? ",
                            lower(original))
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean save(InvocationCommand command) throws DataExtractionException {        
        try (JdbcTransaction transact = super.openTransaction()) {
            return this.saveUsingTransaction(command, transact);
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }
    
    @Override
    public boolean save(List<? extends InvocationCommand> commands) throws DataExtractionException {       
        commands.forEach(command -> logFor(this).info(
                "saving: " + command.originalArgument() + ":" + command.stringify()));
        try (JdbcTransaction transact = super.openTransaction()) {
            boolean done = false;
            for (InvocationCommand command : commands) {
                done = this.saveUsingTransaction(command, transact);
                if ( ! done ) {
                    transact.rollbackAndProceed();
                    return false;
                }
            }
            return done;
        } catch (TransactionHandledSQLException|TransactionHandledException e) {     
            throw super.logAndWrap(e);
        }
    }
    
    private boolean saveUsingTransaction(InvocationCommand command, JdbcTransaction transact) 
            throws TransactionHandledSQLException, TransactionHandledException {
        int modified;
        
        if ( command.argument().isOriginalEqualToExtended() ) {
            modified = this.saveBasicallyUsingTransaction(command, transact);
        } else {
            modified = this.saveAdvancinglyUsingTransaction(command, transact);
        }
        
        return modified > 0;
    }
    
    private int saveBasicallyUsingTransaction(InvocationCommand command, JdbcTransaction transact) 
            throws TransactionHandledSQLException, TransactionHandledException {
        
        boolean commandExists = transact
                .doesQueryHaveResultsVarargParams(
                        "SELECT * " +
                        "FROM commands " +
                        "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ", 
                        lower(command.originalArgument()), command.type());

        int modified = 0;
        if ( commandExists ) {
            modified = modified + transact
                    .doUpdateVarargParams(
                            "UPDATE commands " +
                            "SET com_extended = ? " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ", 
                            command.extendedArgument(), 
                            lower(command.originalArgument()), 
                            command.type());
        } else {
            modified = modified + transact
                    .doUpdateVarargParams(
                            "INSERT INTO commands ( com_type, com_original, com_extended )" +
                            "VALUES ( ?, ?, ? ) " , 
                            command.type(),
                            command.originalArgument(),
                            command.extendedArgument());
        }
        
        return modified;
    }
    
    private int saveAdvancinglyUsingTransaction(
            InvocationCommand command, JdbcTransaction transact) 
            throws TransactionHandledSQLException, TransactionHandledException {        
        
        int modified = this.saveBasicallyUsingTransaction(command, transact);

        boolean extendedExistsInOriginal = transact
                .doesQueryHaveResultsVarargParams(
                    "SELECT * " +
                    "FROM commands " +
                    "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ", 
                    lower(command.extendedArgument()), command.type());

        if ( extendedExistsInOriginal ) {
            modified = modified + transact
                    .doUpdateVarargParams(
                            "UPDATE commands " +
                            "SET com_extended = ? " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ", 
                            command.extendedArgument(), 
                            lower(command.extendedArgument()), 
                            command.type());
        } else {
            modified = modified + transact
                    .doUpdateVarargParams(
                            "INSERT INTO commands ( com_type, com_original, com_extended )" +
                            "VALUES ( ?, ?, ? ) " , 
                            command.type(),
                            command.extendedArgument(),
                            command.extendedArgument());
        }

        if ( modified > 0 ) {
            logFor(this).info("saved: " + modified);
        }

        return modified;
    }
    
    @Override
    public boolean delete(InvocationCommand command) throws DataExtractionException {
        logFor(this).info(
                "delete all: " + command.originalArgument() + ":" + command.extendedArgument());
        try (JdbcTransaction transact = super.openTransaction()) {
            
            int modified = transact
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ",
                            lower(command.originalArgument()), command.type());
            
            if ( command.argument().isExtended() ) {
                modified = modified + transact
                        .doUpdateVarargParams(
                                "DELETE FROM commands " +
                                "WHERE ( LOWER(com_extended) IS ? ) AND ( com_type IS ? ) ",
                                lower(command.extendedArgument()), command.type());
            }
            
            if ( modified > 0 ) {
                logFor(this).info("deleted: " + modified);
            }
            
            return ( modified > 0 );
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean deleteByExactOriginalOfAllTypes(String original) 
            throws DataExtractionException {
        logFor(this).info("delete by original: " + original);
        try {
            return 0 < super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE LOWER(com_original) IS ? ",
                            lower(original));
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean deleteByExactOriginalOfType(String original, CommandType type) 
            throws DataExtractionException {
        logFor(this).info("delete by original and type: " + original + ", " + type.name());
        try {
            return 1 == super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ",
                            lower(original), type);
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean deleteByExactExtendedOfType(String extended, CommandType type) 
            throws DataExtractionException {
        logFor(this).info("delete by extended and type: " + extended + ", " + type.name());
        try {
            return 1 < super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE ( LOWER(com_extended) IS ? ) AND ( com_type IS ? ) ",
                            lower(extended), type);
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean deleteByPrefixInExtended(String prefixInExtended, CommandType type) 
            throws DataExtractionException {
        logFor(this).info(
                "delete by extended-prefix and type: " + prefixInExtended + ", " + type.name());
        try {
            return 1 < super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE ( LOWER(com_extended) LIKE ? ) AND ( com_type IS ? ) ",
                            lowerWildcardAfter(prefixInExtended), type);
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }
    
}
