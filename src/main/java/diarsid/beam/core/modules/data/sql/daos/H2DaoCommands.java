package diarsid.beam.core.modules.data.sql.daos;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.modules.data.BeamCommonDao;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.RowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.SqlUtil.isResultsQuantiyEnoughForMulticharCriteria;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcardAfter;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerGroupedLikesAndOr;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerGroupedLikesOrAnd;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLikeAnd;
import static diarsid.beam.core.base.util.SqlUtil.patternToCharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.patternToMulticharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.shift;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_INVOCATION_COMMAND;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.rowToNewInvocationCommandWithOriginal;


class H2DaoCommands 
        extends BeamCommonDao
        implements DaoCommands {
        
    H2DaoCommands(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public Optional<InvocationCommand> getByExactOriginalAndType(
            Initiator initiator, String original, CommandType type) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( com_type IS ? ) AND ( LOWER(com_original) IS ? ) ",
                            ROW_TO_INVOCATION_COMMAND,
                            type.name(), lower(original));
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return Optional.empty();
        }
    }

    @Override
    public List<InvocationCommand> getByExactOriginalOfAnyType(
            Initiator initiator, String original) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE LOWER(com_original) IS ? ",
                            ROW_TO_INVOCATION_COMMAND,
                            lower(original))
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<InvocationCommand> searchInOriginalByPattern(
            Initiator initiator, String pattern) {        
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<InvocationCommand> found;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE LOWER(com_original) LIKE ? ",
                            ROW_TO_INVOCATION_COMMAND,
                            lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            List<String> criterias = patternToCharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + multipleLowerLikeAnd("com_original", criterias.size()),
                            ROW_TO_INVOCATION_COMMAND,
                            criterias)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            String andOrCondition = multipleLowerGroupedLikesAndOr("com_original", criterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition,
                            ROW_TO_INVOCATION_COMMAND,
                            criterias)
                    .collect(toList());
            
            shift(criterias);
            
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition,
                            ROW_TO_INVOCATION_COMMAND,
                            criterias)
                    .collect(toList());
            
            shuffleFound.retainAll(found);
            found.retainAll(shuffleFound);
            
            return found;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<InvocationCommand> searchInOriginalByPatternAndType(
            Initiator initiator, String pattern, CommandType type) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<InvocationCommand> found;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( LOWER(com_original) LIKE ? ) AND ( com_type IS ? )",
                            ROW_TO_INVOCATION_COMMAND,
                            lowerWildcard(pattern), type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            List<String> criterias = patternToCharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + 
                                    multipleLowerLikeAnd("com_original", criterias.size()) + 
                                    " AND ( com_type IS ? ) ",
                            ROW_TO_INVOCATION_COMMAND,
                            criterias, type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            String andOrCondition = multipleLowerGroupedLikesAndOr("com_original", criterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )",
                            ROW_TO_INVOCATION_COMMAND,
                            criterias, type)
                    .collect(toList());
            
            shift(criterias);
            
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )",
                            ROW_TO_INVOCATION_COMMAND,
                            criterias, type)
                    .collect(toList());
            
            shuffleFound.retainAll(found);
            found.retainAll(shuffleFound);
            
            return found;
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<InvocationCommand> searchInExtendedByPattern(
            Initiator initiator, String pattern) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<InvocationCommand> found;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE LOWER(com_extended) LIKE ? ",
                            ROW_TO_INVOCATION_COMMAND,
                            lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            List<String> multicharCriterias = patternToMulticharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + multipleLowerGroupedLikesOrAnd(
                                    "com_extended", multicharCriterias.size()),
                            ROW_TO_INVOCATION_COMMAND,
                            multicharCriterias)
                    .collect(toList());    
            
            debug("[DAO COMMANDS] found by multichar criteria: " + found.size());
            if ( isResultsQuantiyEnoughForMulticharCriteria(found) ) {
                return found;
            }

            List<String> charCriterias = patternToCharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + multipleLowerLikeAnd("com_extended", charCriterias.size()),
                            ROW_TO_INVOCATION_COMMAND,
                            charCriterias)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            String andOrCondition = multipleLowerGroupedLikesAndOr("com_extended", charCriterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition,
                            ROW_TO_INVOCATION_COMMAND,
                            charCriterias)
                    .collect(toList());
            
            shift(charCriterias);
            
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition,
                            ROW_TO_INVOCATION_COMMAND,
                            charCriterias)
                    .collect(toList());
            
            shuffleFound.retainAll(found);
            found.retainAll(shuffleFound);
            
            return found;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<InvocationCommand> searchInExtendedByPatternAndType(
            Initiator initiator, String pattern, CommandType type) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<InvocationCommand> found;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( LOWER(com_extended) LIKE ? ) AND ( com_type IS ? )",
                            ROW_TO_INVOCATION_COMMAND,
                            lowerWildcard(pattern), type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            List<String> multicharCriterias = patternToMulticharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + 
                                    multipleLowerGroupedLikesOrAnd(
                                            "com_extended", multicharCriterias.size()) + 
                                    " AND ( com_type IS ? ) ",
                            ROW_TO_INVOCATION_COMMAND,
                            multicharCriterias, type)
                    .collect(toList());
            
            debug("[DAO COMMANDS] found by multichar criteria: " + found.size());
            if ( isResultsQuantiyEnoughForMulticharCriteria(found) ) {
                return found;
            }
            
            List<String> charCriterias = patternToCharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + 
                                    multipleLowerLikeAnd("com_extended", charCriterias.size()) + 
                                    " AND ( com_type IS ? ) ",
                            ROW_TO_INVOCATION_COMMAND,
                            charCriterias, type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            String andOrCondition = multipleLowerGroupedLikesAndOr("com_extended", charCriterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )",
                            ROW_TO_INVOCATION_COMMAND,
                            charCriterias, type)
                    .collect(toList());
            
            shift(charCriterias);
            
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )",
                            ROW_TO_INVOCATION_COMMAND,
                            charCriterias, type)
                    .collect(toList());
            
            shuffleFound.retainAll(found);
            found.retainAll(shuffleFound);
            
            return found;
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }   
    }

    @Override
    public List<InvocationCommand> searchInExtendedByPatternGroupByExtended(
            Initiator initiator, String pattern) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            RowConversion<InvocationCommand> rowToNewInvocationCommandWithPatternAsOriginal = 
                    rowToNewInvocationCommandWithOriginal(pattern);
            
            List<InvocationCommand> found;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE LOWER(com_extended) LIKE ? " +
                            "GROUP BY com_type, com_extended",
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            List<String> multicharCriterias = patternToMulticharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE " + multipleLowerGroupedLikesOrAnd(
                                    "com_extended", multicharCriterias.size()) +
                            "GROUP BY com_type, com_extended",
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            multicharCriterias)
                    .collect(toList());    
            
            debug("[DAO COMMANDS] found by multichar criteria: " + found.size());
            if ( isResultsQuantiyEnoughForMulticharCriteria(found) ) {
                return found;
            }

            List<String> charCriterias = patternToCharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE " + multipleLowerLikeAnd("com_extended", charCriterias.size()) +
                            "GROUP BY com_type, com_extended",
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            charCriterias)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            String andOrCondition = multipleLowerGroupedLikesAndOr("com_extended", charCriterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition +
                            "GROUP BY com_type, com_extended",
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            charCriterias)
                    .collect(toList());
            
            shift(charCriterias);
            
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition +
                            "GROUP BY com_type, com_extended",
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            charCriterias)
                    .collect(toList());
                        
            shuffleFound.retainAll(found);
            found.retainAll(shuffleFound);
            
            return found;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<InvocationCommand> searchInExtendedByPatternAndTypeGroupByExtended(
            Initiator initiator, String pattern, CommandType type) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            RowConversion<InvocationCommand> rowToNewInvocationCommandWithPatternAsOriginal = 
                    rowToNewInvocationCommandWithOriginal(pattern);
            
            List<InvocationCommand> found;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE ( LOWER(com_extended) LIKE ? ) AND ( com_type IS ? )" +
                            "GROUP BY com_type, com_extended",
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            lowerWildcard(pattern), type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            List<String> multicharCriterias = patternToMulticharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE " + 
                                    multipleLowerGroupedLikesOrAnd(
                                            "com_extended", multicharCriterias.size()) + 
                                    " AND ( com_type IS ? ) " +
                            "GROUP BY com_type, com_extended",
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            multicharCriterias, type)
                    .collect(toList());
            
            debug("[DAO COMMANDS] found by multichar criteria: " + found.size());
            if ( isResultsQuantiyEnoughForMulticharCriteria(found) ) {
                return found;
            }
            
            List<String> charCriterias = patternToCharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE " + 
                                    multipleLowerLikeAnd("com_extended", charCriterias.size()) + 
                                    " AND ( com_type IS ? ) " +
                            "GROUP BY com_type, com_extended",
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            charCriterias, type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            String andOrCondition = multipleLowerGroupedLikesAndOr("com_extended", charCriterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )" +
                            "GROUP BY com_type, com_extended",
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            charCriterias, type)
                    .collect(toList());
            
            shift(charCriterias);
            
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )" +
                            "GROUP BY com_type, com_extended",
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            charCriterias, type)
                    .collect(toList());
            
            shuffleFound.retainAll(found);
            found.retainAll(shuffleFound);
            
            return found;
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }   
    }

    @Override
    public boolean save(
            Initiator initiator, InvocationCommand command) {        
        try (JdbcTransaction transact = super.openTransaction()) {
            return this.saveUsingTransaction(command, transact);
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }
    
    @Override
    public boolean save(
            Initiator initiator, List<InvocationCommand> commands) {       
        commands.forEach(command -> debug("[DAO COMMANDS] saving: " + command.originalArgument() + ":" + command.stringify()));
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
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
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
    
    private int saveBasicallyUsingTransaction(
            InvocationCommand command, JdbcTransaction transact) 
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
            debug("[DAO COMMANDS] saved: " + modified);
        }

        return modified;
    }
    
    @Override
    public boolean delete(
            Initiator initiator, InvocationCommand command) {
        debug("[DAO COMMANDS] delete all: " + command.originalArgument() + ":" + command.extendedArgument());
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
                debug("[DAO COMMANDS] deleted: " + modified);
            }
            
            return ( modified > 0 );
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }

    @Override
    public boolean deleteByExactOriginalOfAllTypes(
            Initiator initiator, String original) {
        debug("[DAO COMMANDS] delete by original: " + original);
        try {
            return 0 < super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE LOWER(com_original) IS ? ",
                            lower(original));
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }

    @Override
    public boolean deleteByExactOriginalOfType(
            Initiator initiator, String original, CommandType type) {
        debug("[DAO COMMANDS] delete by original and type: " + original + ", " + type.name());
        try {
            return 1 == super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ",
                            lower(original), type);
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }

    @Override
    public boolean deleteByExactExtendedOfType(
            Initiator initiator, String extended, CommandType type) {
        debug("[DAO COMMANDS] delete by extended and type: " + extended + ", " + type.name());
        try {
            return 1 < super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE ( LOWER(com_extended) IS ? ) AND ( com_type IS ? ) ",
                            lower(extended), type);
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }

    @Override
    public boolean deleteByPrefixInExtended(
            Initiator initiator, String prefixInExtended, CommandType type) {
        debug("[DAO COMMANDS] delete by extended-prefix and type: " + prefixInExtended + ", " + type.name());
        try {
            return 1 < super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE ( LOWER(com_extended) LIKE ? ) AND ( com_type IS ? ) ",
                            lowerWildcardAfter(prefixInExtended), type);
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }
}
