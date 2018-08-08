/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.data.DataBase;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.RowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.SqlUtil.isResultsQuantiyEnoughForMulticharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerGroupedLikesAndOr;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerGroupedLikesOrAnd;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLikeAnd;
import static diarsid.beam.core.base.util.SqlUtil.patternToCharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.patternToMulticharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.shift;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_INVOCATION_COMMAND;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.rowToNewInvocationCommandWithOriginal;

/**
 *
 * @author Diarsid
 */
class H2DaoCommandsV1 extends H2DaoCommandsV0 {
        
    H2DaoCommandsV1(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public List<InvocationCommand> searchInOriginalByPattern(
            Initiator initiator, String pattern) {        
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<InvocationCommand> found;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE LOWER(com_original) LIKE ? ",
                            lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            List<String> criterias = patternToCharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + multipleLowerLikeAnd("com_original", criterias.size()),
                            criterias)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            String andOrCondition = multipleLowerGroupedLikesAndOr("com_original", criterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition,
                            criterias)
                    .collect(toList());
            
            shift(criterias);
            
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition,
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
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( LOWER(com_original) LIKE ? ) AND ( com_type IS ? )",
                            lowerWildcard(pattern), type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            List<String> criterias = patternToCharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + 
                                    multipleLowerLikeAnd("com_original", criterias.size()) + 
                                    " AND ( com_type IS ? ) ",
                            criterias, type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            String andOrCondition = multipleLowerGroupedLikesAndOr("com_original", criterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )",
                            criterias, type)
                    .collect(toList());
            
            shift(criterias);
            
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )",
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
        // TODO HIGH
        if ( pattern.length() == 2 ) {
            
        } else {
            // do current actions
        }
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<InvocationCommand> found;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE LOWER(com_extended) LIKE ? ",
                            lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            List<String> multicharCriterias = patternToMulticharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + multipleLowerGroupedLikesOrAnd(
                                    "com_extended", multicharCriterias.size()),
                            multicharCriterias)
                    .collect(toList());    
            
            if ( isResultsQuantiyEnoughForMulticharCriterias(found, multicharCriterias.size()) ) {
                return found;
            }

            List<String> charCriterias = patternToCharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + multipleLowerLikeAnd("com_extended", charCriterias.size()),
                            charCriterias)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            String andOrCondition = multipleLowerGroupedLikesAndOr(
                    "com_extended", charCriterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition,
                            charCriterias)
                    .collect(toList());
            
            shift(charCriterias);
            
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition,
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
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( LOWER(com_extended) LIKE ? ) AND ( com_type IS ? )",
                            lowerWildcard(pattern), type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            List<String> multicharCriterias = patternToMulticharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + 
                                    multipleLowerGroupedLikesOrAnd(
                                            "com_extended", multicharCriterias.size()) + 
                                    " AND ( com_type IS ? ) ",
                            multicharCriterias, type)
                    .collect(toList());
            
            if ( isResultsQuantiyEnoughForMulticharCriterias(found, multicharCriterias.size()) ) {
                return found;
            }
            
            List<String> charCriterias = patternToCharCriterias(pattern);
            
//            found = transact
//                    .doQueryAndStreamVarargParams(
//                            InvocationCommand.class,
//                            "SELECT com_type, com_original, com_extended " +
//                            "FROM commands " +
//                            "WHERE " + 
//                                    multipleLowerLikeAnd("com_extended", charCriterias.size()) + 
//                                    " AND ( com_type IS ? ) ",
//                            ROW_TO_INVOCATION_COMMAND,
//                            charCriterias, type)
//                    .collect(toList());
//            
//            if ( nonEmpty(found) ) {
//                return found;
//            }
            
            String andOrCondition = multipleLowerGroupedLikesAndOr("com_extended", charCriterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )",
                            charCriterias, type)
                    .collect(toList());
            
            shift(charCriterias);
            
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )",
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
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE LOWER(com_extended) LIKE ? " +
                            "GROUP BY com_type, com_extended",
                            lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
//            List<String> multicharCriterias = patternToMulticharCriterias(pattern);
//            
//            found = transact
//                    .doQueryAndStreamVarargParams(
//                            InvocationCommand.class,
//                            "SELECT com_type, com_extended " +
//                            "FROM commands " +
//                            "WHERE " + multipleLowerGroupedLikesOrAnd(
//                                    "com_extended", multicharCriterias.size()) +
//                            "GROUP BY com_type, com_extended",
//                            rowToNewInvocationCommandWithPatternAsOriginal,
//                            multicharCriterias)
//                    .collect(toList());    
//            
//            debug("[DAO COMMANDS] found by multichar criteria: " + found.size());
//            if ( isResultsQuantiyEnoughForMulticharCriterias(found, multicharCriterias.size()) ) {
//                return found;
//            }

            List<String> charCriterias = patternToCharCriterias(pattern);
            
//            found = transact
//                    .doQueryAndStreamVarargParams(
//                            InvocationCommand.class,
//                            "SELECT com_type, com_extended " +
//                            "FROM commands " +
//                            "WHERE " + multipleLowerLikeAnd("com_extended", charCriterias.size()) +
//                            "GROUP BY com_type, com_extended",
//                            rowToNewInvocationCommandWithPatternAsOriginal,
//                            charCriterias)
//                    .collect(toList());
//            
//            if ( nonEmpty(found) ) {
//                return found;
//            } 
            
            String andOrCondition = multipleLowerGroupedLikesAndOr("com_extended", charCriterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition +
                            "GROUP BY com_type, com_extended",
                            charCriterias)
                    .collect(toList());
            
            shift(charCriterias);
            
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition +
                            "GROUP BY com_type, com_extended",
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
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE ( LOWER(com_extended) LIKE ? ) AND ( com_type IS ? )" +
                            "GROUP BY com_type, com_extended",
                            lowerWildcard(pattern), type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            List<String> multicharCriterias = patternToMulticharCriterias(pattern);
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE " + 
                                    multipleLowerGroupedLikesOrAnd(
                                            "com_extended", multicharCriterias.size()) + 
                                    " AND ( com_type IS ? ) " +
                            "GROUP BY com_type, com_extended",
                            multicharCriterias, type)
                    .collect(toList());
            
            if ( isResultsQuantiyEnoughForMulticharCriterias(found, multicharCriterias.size()) ) {
                return found;
            }
            
            List<String> charCriterias = patternToCharCriterias(pattern);
            
//            found = transact
//                    .doQueryAndStreamVarargParams(
//                            InvocationCommand.class,
//                            "SELECT com_type, com_extended " +
//                            "FROM commands " +
//                            "WHERE " + 
//                                    multipleLowerLikeAnd("com_extended", charCriterias.size()) + 
//                                    " AND ( com_type IS ? ) " +
//                            "GROUP BY com_type, com_extended",
//                            rowToNewInvocationCommandWithPatternAsOriginal,
//                            charCriterias, type)
//                    .collect(toList());
//            
//            if ( nonEmpty(found) ) {
//                return found;
//            } 
            
            String andOrCondition = multipleLowerGroupedLikesAndOr("com_extended", charCriterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )" +
                            "GROUP BY com_type, com_extended",
                            charCriterias, type)
                    .collect(toList());
            
            shift(charCriterias);
            
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )" +
                            "GROUP BY com_type, com_extended",
                            charCriterias, type)
                    .collect(toList());
            
            shuffleFound.retainAll(found);
            found.retainAll(shuffleFound);
            
            return found;
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }   
    }
}
