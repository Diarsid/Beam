package diarsid.beam.core.modules.data.daos.sql;

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
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.PerRowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.join;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.commands.Commands.restoreInvocationCommandFrom;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcardAfter;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerGroupedLikesOr;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLikeAnd;
import static diarsid.beam.core.base.util.SqlUtil.patternToCharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.shift;
import static diarsid.beam.core.base.util.StringUtils.lower;


class H2DaoCommands 
        extends BeamCommonDao
        implements DaoCommands {
    
    private final PerRowConversion<InvocationCommand> rowToCommandConversion;
    
    H2DaoCommands(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
        this.rowToCommandConversion = (row) -> {
            return restoreInvocationCommandFrom(
                    (String) row.get("com_type"), 
                    (String) row.get("com_original"),
                    (String) row.get("com_extended"));
        };
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
                            (firstRow) -> {
                                return Optional.of(this.rowToCommandConversion.convert(firstRow));
                            },
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
                            this.rowToCommandConversion,
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
                            this.rowToCommandConversion,
                            lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } else {
                debug("[DAO COMMANDS] not found : " + pattern);
            }
            
            List<String> criterias = patternToCharCriterias(pattern);
            debug("[DAO COMMANDS] criterias: " + join(" ", criterias));
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + multipleLowerLikeAnd("com_original", criterias.size()),
                            this.rowToCommandConversion,
                            criterias)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } else {
                debug("[DAO COMMANDS CRITERIAS AND] not found : " + pattern);
            }
            
            String andOrCondition = multipleLowerGroupedLikesOr("com_original", criterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition,
                            this.rowToCommandConversion,
                            criterias)
                    .collect(toList());
            
            shift(criterias);
            debug("[DAO COMMANDS] shuffled criterias: " + join(" ", criterias));
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition,
                            this.rowToCommandConversion,
                            criterias)
                    .collect(toList());
            
            debug("[DAO COMMANDS] found by criterias : " + found.size());
            debug("[DAO COMMANDS] found by shuffled criterias : " + shuffleFound.size());
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
                            this.rowToCommandConversion,
                            lowerWildcard(pattern), type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } else {
                debug("[DAO COMMANDS] not found : " + pattern);
            }
            
            List<String> criterias = patternToCharCriterias(pattern);
            debug("[DAO COMMANDS] criterias: " + join(" ", criterias));
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + 
                                    multipleLowerLikeAnd("com_original", criterias.size()) + 
                                    " AND ( com_type IS ? ) ",
                            this.rowToCommandConversion,
                            criterias, type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } else {
                debug("[DAO COMMANDS CRITERIAS AND] not found : " + pattern);
            }
            
            String andOrCondition = multipleLowerGroupedLikesOr("com_original", criterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )",
                            this.rowToCommandConversion,
                            criterias, type)
                    .collect(toList());
            
            shift(criterias);
            debug("[DAO COMMANDS] shuffled criterias: " + join(" ", criterias));
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )",
                            this.rowToCommandConversion,
                            criterias, type)
                    .collect(toList());
            
            debug("[DAO COMMANDS] found by criterias : " + found.size());
            debug("[DAO COMMANDS] found by shuffled criterias : " + shuffleFound.size());
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
                            this.rowToCommandConversion,
                            lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } else {
                debug("[DAO COMMANDS FULL] not found : " + pattern);
            }
            
            List<String> criterias = patternToCharCriterias(pattern);
            debug("[DAO COMMANDS] criterias: " + join(" ", criterias));
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + multipleLowerLikeAnd("com_extended", criterias.size()),
                            this.rowToCommandConversion,
                            criterias)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } else {
                debug("[DAO COMMANDS CRITERIAS AND] not found : " + pattern);
            }
            
            String andOrCondition = multipleLowerGroupedLikesOr("com_extended", criterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition,
                            this.rowToCommandConversion,
                            criterias)
                    .collect(toList());
            
            shift(criterias);
            debug("[DAO COMMANDS] shuffled criterias: " + join(" ", criterias));
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition,
                            this.rowToCommandConversion,
                            criterias)
                    .collect(toList());
            
            debug("[DAO COMMANDS] found by criterias : " + found.size());
            debug("[DAO COMMANDS] found by shuffled criterias : " + shuffleFound.size());
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
                            this.rowToCommandConversion,
                            lowerWildcard(pattern), type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } else {
                debug("[DAO COMMANDS FULL] not found : " + pattern);
            }
            
            List<String> criterias = patternToCharCriterias(pattern);
            debug("[DAO COMMANDS] criterias: " + join(" ", criterias));
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + 
                                    multipleLowerLikeAnd("com_extended", criterias.size()) + 
                                    " AND ( com_type IS ? ) ",
                            this.rowToCommandConversion,
                            criterias, type)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            } else {
                debug("[DAO COMMANDS CRITERIAS AND] not found : " + pattern);
            }
            
            String andOrCondition = multipleLowerGroupedLikesOr("com_extended", criterias.size());
            List<InvocationCommand> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )",
                            this.rowToCommandConversion,
                            criterias, type)
                    .collect(toList());
            
            shift(criterias);
            debug("[DAO COMMANDS] shuffled criterias: " + join(" ", criterias));
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            InvocationCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + andOrCondition + " AND ( com_type IS ? )",
                            this.rowToCommandConversion,
                            criterias, type)
                    .collect(toList());
            
            debug("[DAO COMMANDS] found by criterias : " + found.size());
            debug("[DAO COMMANDS] found by shuffled criterias : " + shuffleFound.size());
            shuffleFound.retainAll(found);
            found.retainAll(shuffleFound);
            
            debug("[]");
            return found;
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }   
    }

    @Override
    public boolean save(
            Initiator initiator, InvocationCommand command) {        
            debug("[DAO COMMANDS] saving: " + command.originalArgument() + ":" + command.stringify());
        try (JdbcTransaction transact = super.openTransaction()) {
            
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
            
            if ( command.argument().isExtended() ) {
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
            }
            
            if ( modified > 0 ) {
                debug("[DAO COMMANDS] saved: " + modified);
            }
            
            return ( modified > 0 );
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
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
