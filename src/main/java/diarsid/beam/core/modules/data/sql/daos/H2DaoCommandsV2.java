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
import diarsid.beam.core.base.data.util.SqlPatternSelect;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.RowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.objects.Pools.takeFromPool;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logging.logFor;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_INVOCATION_COMMAND;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.rowToNewInvocationCommandWithOriginal;

/**
 *
 * @author Diarsid
 */
class H2DaoCommandsV2 extends H2DaoCommandsV0 {   
        
    H2DaoCommandsV2(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public List<InvocationCommand> searchInOriginalByPattern(
            Initiator initiator, String pattern) {
        try (
                JdbcTransaction transact = super.openTransaction();
                SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class);) 
        {
            List<InvocationCommand> found;            
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND, 
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( LOWER(com_original) IS ? ) ",
                            lower(pattern))
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
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
            
            found = transact
                    .doQueryAndStream(
                            ROW_TO_INVOCATION_COMMAND,
                            patternSelect
                                    .select("com_type, com_original, com_extended")
                                    .from("commands")
                                    .patternForWhereCondition(pattern)
                                    .patternColumnForWhereCondition("com_original")
                                    .composeSql())
                    .collect(toList());
            
            if ( nonEmpty(found) || pattern.length() == 2 ) {
                return found;
            } 
            
            found = transact
                    .doQueryAndStream(
                            ROW_TO_INVOCATION_COMMAND,
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql())
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            found = transact
                    .doQueryAndStream(
                            ROW_TO_INVOCATION_COMMAND,
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql())
                    .collect(toList());
            
            return found;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(format("search on %s", pattern), ex);
            this.ioEngine().report(initiator, format("error on search %s in commands", pattern));
            return emptyList();
        }
    }

    @Override
    public List<InvocationCommand> searchInOriginalByPatternAndType(
            Initiator initiator, String pattern, CommandType type) {
        try (
                JdbcTransaction transact = super.openTransaction(); 
                SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class)) 
        {
            List<InvocationCommand> found;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND, 
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? )",
                            lower(pattern), type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
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
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND, 
                            patternSelect
                                    .select("com_type, com_original, com_extended")
                                    .from("commands")
                                    .patternForWhereCondition(pattern)
                                    .patternColumnForWhereCondition("com_original")
                                    .anotherWhereClauses(" AND ( com_type IS ? ) ")
                                    .composeSql(),
                            type)
                    .collect(toList());
            
            if ( nonEmpty(found) || pattern.length() == 2 ) {
                return found;
            } 
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND, 
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql(),
                            type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND, 
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql(),
                            type)
                    .collect(toList());
            
            return found;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(format("search on %s and type %s", pattern, type), ex);
            this.ioEngine().report(initiator, format("error on search %s in commands", pattern));
            return emptyList();
        }
    }

    @Override
    public List<InvocationCommand> searchInExtendedByPattern(
            Initiator initiator, String pattern) {
        try (
                JdbcTransaction transact = super.openTransaction();
                SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class);) 
        {
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
            
            found = transact
                    .doQueryAndStream(
                            ROW_TO_INVOCATION_COMMAND,
                            patternSelect
                                    .select("com_type, com_original, com_extended")
                                    .from("commands")
                                    .patternForWhereCondition(pattern)
                                    .patternColumnForWhereCondition("com_extended")
                                    .composeSql())
                    .collect(toList());
            
            if ( nonEmpty(found) || pattern.length() == 2 ) {
                return found;
            } 
            
            found = transact
                    .doQueryAndStream(
                            ROW_TO_INVOCATION_COMMAND,
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql())
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            found = transact
                    .doQueryAndStream(
                            ROW_TO_INVOCATION_COMMAND,
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql())
                    .collect(toList());
            
            return found;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(format("search on %s ", pattern), ex);
            this.ioEngine().report(initiator, format("error on search %s in commands", pattern));
            return emptyList();
        }
    }

    @Override
    public List<InvocationCommand> searchInExtendedByPatternAndType(
            Initiator initiator, String pattern, CommandType type) {
        try (
                JdbcTransaction transact = super.openTransaction(); 
                SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class)) 
        {
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
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND, 
                            patternSelect
                                    .select("com_type, com_original, com_extended")
                                    .from("commands")
                                    .patternForWhereCondition(pattern)
                                    .patternColumnForWhereCondition("com_extended")
                                    .anotherWhereClauses(" AND ( com_type IS ? ) ")
                                    .composeSql(),
                            type)
                    .collect(toList());
            
            if ( nonEmpty(found) || pattern.length() == 2 ) {
                return found;
            } 
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND, 
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql(),
                            type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND, 
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql(),
                            type)
                    .collect(toList());
            
            return found;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(format("search on %s and type %s", pattern, type), ex);
            this.ioEngine().report(initiator, format("error on search %s in commands", pattern));
            return emptyList();
        }
    }

    @Override
    public List<InvocationCommand> searchInExtendedByPatternGroupByExtended(
            Initiator initiator, String pattern) {
        try (
                JdbcTransaction transact = super.openTransaction(); 
                SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class)) 
        {            
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
            
            found = transact
                    .doQueryAndStream(
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            patternSelect
                                    .select("com_type, com_extended")
                                    .from("commands")
                                    .patternForWhereCondition(pattern)
                                    .patternColumnForWhereCondition("com_extended")
                                    .groupBySelectColumns()
                                    .composeSql())
                    .collect(toList());
            
            if ( nonEmpty(found) || pattern.length() == 2 ) {
                return found;
            }
            
            found = transact
                    .doQueryAndStream(
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql())
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            found = transact
                    .doQueryAndStream(
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql())
                    .collect(toList());
            
            return found;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(format("search on %s", pattern), ex);
            this.ioEngine().report(initiator, format("error on search %s in commands", pattern));
            return emptyList();
        }
    }

    @Override
    public List<InvocationCommand> searchInExtendedByPatternAndTypeGroupByExtended(
            Initiator initiator, String pattern, CommandType type) {
        try (
                JdbcTransaction transact = super.openTransaction(); 
                SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class)) 
        {            
            RowConversion<InvocationCommand> rowToNewInvocationCommandWithPatternAsOriginal = 
                    rowToNewInvocationCommandWithOriginal(pattern);
            
            List<InvocationCommand> found;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            rowToNewInvocationCommandWithPatternAsOriginal,
                            "SELECT com_type, com_extended " +
                            "FROM commands " +
                            "WHERE ( LOWER(com_extended) LIKE ? ) AND ( com_type IS ? ) " +
                            "GROUP BY com_type, com_extended",
                            lowerWildcard(pattern), type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } 
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            rowToNewInvocationCommandWithPatternAsOriginal, 
                            patternSelect
                                    .select("com_type, com_extended")
                                    .from("commands")
                                    .patternForWhereCondition(pattern)
                                    .patternColumnForWhereCondition("com_extended")
                                    .anotherWhereClauses(" AND ( com_type IS ? ) ")
                                    .groupBySelectColumns()
                                    .composeSql(),
                            type)
                    .collect(toList());
            
            if ( nonEmpty(found) || pattern.length() == 2 ) {
                return found;
            } 
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            rowToNewInvocationCommandWithPatternAsOriginal, 
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql(),
                            type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            rowToNewInvocationCommandWithPatternAsOriginal, 
                            patternSelect
                                    .decreaseRequiredLikeness()
                                    .composeSql(),
                            type)
                    .collect(toList());
            
            return found;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(format("search on %s and type %s", pattern, type), ex);
            this.ioEngine().report(initiator, format("error on search %s in commands", pattern));
            return emptyList();
        }
    }    
    
}
