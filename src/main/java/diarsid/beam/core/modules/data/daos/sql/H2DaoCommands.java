package diarsid.beam.core.modules.data.daos.sql;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.util.List;
import java.util.Optional;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.commands.ArgumentedCommand;
import diarsid.beam.core.control.io.commands.CommandType;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.PerRowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.control.io.commands.Commands.restoreArgumentedCommandFrom;
import static diarsid.beam.core.util.SqlUtil.SqlOperator.AND;
import static diarsid.beam.core.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.util.SqlUtil.lowerWildcardList;
import static diarsid.beam.core.util.SqlUtil.lowerWildcardListAnd;
import static diarsid.beam.core.util.SqlUtil.multipleLowerLike;
import static diarsid.beam.core.util.StringUtils.lower;


class H2DaoCommands 
        extends BeamCommonDao
        implements DaoCommands {
    
    private final PerRowConversion<ArgumentedCommand> rowToCommandConversion;
    
    H2DaoCommands(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
        this.rowToCommandConversion = (row) -> {
            return restoreArgumentedCommandFrom(
                    (String) row.get("com_type"), 
                    (String) row.get("com_original"),
                    (String) row.get("com_extended"));
        };
    }

    @Override
    public Optional<ArgumentedCommand> getByExactOriginalOfType(
            Initiator initiator, String original, CommandType type) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            ArgumentedCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( com_type IS ? ) AND ( LOWER(com_original) IS ? ) ",
                            (firstRow) -> {
                                return Optional.of(restoreArgumentedCommandFrom(
                                        (String) firstRow.get("com_type"), 
                                        (String) firstRow.get("com_original"), 
                                        (String) firstRow.get("com_extended")));
                            },
                            type.name(), lower(original));
        } catch (TransactionHandledSQLException ex) {
            
            return Optional.empty();
        }
    }

    @Override
    public List<ArgumentedCommand> getByExactOriginalOfAnyType(
            Initiator initiator, String original) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ArgumentedCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE LOWER(com_original) IS ? ",
                            this.rowToCommandConversion,
                            lower(original))
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<ArgumentedCommand> fullSearchByOriginalPattern(
            Initiator initiator, String pattern) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ArgumentedCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE LOWER(com_original) LIKE ? ",
                            this.rowToCommandConversion,
                            lowerWildcard(pattern))
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<ArgumentedCommand> fullSearchByOriginalPatternParts(
            Initiator initiator, List<String> patternParts) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStream(
                            ArgumentedCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + multipleLowerLike("com_original", patternParts.size(), AND),
                            this.rowToCommandConversion,
                            lowerWildcardList(patternParts))
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<ArgumentedCommand> fullSearchByOriginalPatternOfType(
            Initiator initiator, String pattern, CommandType type) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ArgumentedCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( LOWER(com_original) LIKE ? ) AND ( com_type IS ? ) ",
                            this.rowToCommandConversion,
                            lowerWildcard(pattern), type.name())
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<ArgumentedCommand> fullSearchByOriginalPatternPartsOfType(
            Initiator initiator, List<String> patternParts, CommandType type) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStream(
                            ArgumentedCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( " + 
                                    multipleLowerLike("com_original", patternParts.size(), AND) + 
                                    " ) AND ( com_type IS ? ) ",
                            this.rowToCommandConversion,
                            lowerWildcardListAnd(patternParts, type.name()))
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<ArgumentedCommand> fullSearchByExtendedPattern(
            Initiator initiator, String pattern) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ArgumentedCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE LOWER(com_extended) LIKE ? ",
                            this.rowToCommandConversion,
                            lowerWildcard(pattern))
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<ArgumentedCommand> fullSearchByExtendedPatternParts(
            Initiator initiator, List<String> patternParts) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStream(
                            ArgumentedCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + multipleLowerLike("com_extended", patternParts.size(), AND),
                            this.rowToCommandConversion,
                            lowerWildcardList(patternParts))
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<ArgumentedCommand> fullSearchByExtendedPatternOfType(
            Initiator initiator, String pattern, CommandType type) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ArgumentedCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( LOWER(com_extended) LIKE ? ) AND ( com_type IS ? ) ",
                            this.rowToCommandConversion,
                            lowerWildcard(pattern), type.name())
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<ArgumentedCommand> fullSearchByExtendedPatternPartsOfType(
            Initiator initiator, List<String> patternParts, CommandType type) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStream(
                            ArgumentedCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( " + 
                                    multipleLowerLike("com_extended", patternParts.size(), AND) + 
                                    " ) AND ( com_type IS ? ) ",
                            this.rowToCommandConversion,
                            lowerWildcardListAnd(patternParts, type.name()))
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            
            return emptyList();
        }
    }

    @Override
    public boolean save(
            Initiator initiator, ArgumentedCommand command) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            boolean commandExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM commands " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ", 
                            lower(command.stringifyOriginal()), command.type().name());
                        
            if ( commandExists ) {
                return transact
                        .doUpdateVarargParams(
                                "UDPATE commands " +
                                "SET com_extended = ? " +
                                "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ", 
                                command.stringifyExtended(), 
                                lower(command.stringifyOriginal()), 
                                command.type().name())
                        == 1;
            } else {
                return transact
                        .doUpdateVarargParams(
                                "INSERT INTO commands ( com_type, com_original, com_extended )" +
                                "VALUES ( ?, ?, ? ) " , 
                                command.type().name(),
                                command.stringifyOriginal(),
                                command.stringifyExtended())
                        == 1;
            }
            
        } catch (TransactionHandledSQLException ex) {
            
            return false;
        }
    }

    @Override
    public boolean delete(
            Initiator initiator, ArgumentedCommand command) {
        try {
            return 1 == super.getDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ",
                            lower(command.stringifyOriginal()), command.type().name());
        } catch (TransactionHandledSQLException ex) {
            
            return false;
        }
    }

    @Override
    public boolean deleteByExactOriginalOfAllTypes(
            Initiator initiator, String original) {
        try {
            return 0 < super.getDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE LOWER(com_original) IS ? ",
                            lower(original));
        } catch (TransactionHandledSQLException ex) {
            
            return false;
        }
    }

    @Override
    public boolean deleteByExactOriginalOfType(
            Initiator initiator, String original, CommandType type) {
        try {
            return 1 == super.getDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ",
                            lower(original), type.name());
        } catch (TransactionHandledSQLException ex) {
            
            return false;
        }
    }
}
