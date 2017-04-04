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
import diarsid.beam.core.base.control.io.commands.ExtendableCommand;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.PerRowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.commands.Commands.restoreArgumentedCommandFrom;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.base.util.SqlUtil.SqlOperator.AND;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcardList;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLIKE;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.base.util.StringUtils.splitByWildcard;


class H2DaoCommands 
        extends BeamCommonDao
        implements DaoCommands {
    
    private final PerRowConversion<ExtendableCommand> rowToCommandConversion;
    
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
    public Optional<ExtendableCommand> getByExactOriginalAndType(
            Initiator initiator, String original, CommandType type) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            ExtendableCommand.class,
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
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return Optional.empty();
        }
    }

    @Override
    public List<ExtendableCommand> getByExactOriginalOfAnyType(
            Initiator initiator, String original) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ExtendableCommand.class,
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
    public List<ExtendableCommand> fullSearchByOriginalPattern(
            Initiator initiator, String pattern) {
        if ( hasWildcard(pattern) ) {
            return this.findByPartsOriginalPattern(initiator, splitByWildcard(pattern));
        } else {
            return this.findBySingleOriginalPattern(pattern);            
        }
    }

    private List<ExtendableCommand> findBySingleOriginalPattern(String pattern) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ExtendableCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE LOWER(com_original) LIKE ? ",
                            this.rowToCommandConversion,
                            lowerWildcard(pattern))
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    private List<ExtendableCommand> findByPartsOriginalPattern(
            Initiator initiator, List<String> patternParts) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStream(
                            ExtendableCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + multipleLowerLIKE("com_original", patternParts.size(), AND),
                            this.rowToCommandConversion,
                            lowerWildcardList(patternParts))
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<ExtendableCommand> searchInOriginalByPatternAndType(
            Initiator initiator, String pattern, CommandType type) {
        if ( hasWildcard(pattern) ) {
            return this.findByPartsOriginalPatternOfType(initiator, splitByWildcard(pattern), type);
        } else {
            return this.findBySingleOriginalPatternOfType(pattern, type);
        }
    }

    private List<ExtendableCommand> findBySingleOriginalPatternOfType(String pattern, CommandType type) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ExtendableCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( LOWER(com_original) LIKE ? ) AND ( com_type IS ? ) ",
                            this.rowToCommandConversion,
                            lowerWildcard(pattern), type.name())
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    private List<ExtendableCommand> findByPartsOriginalPatternOfType(
            Initiator initiator, List<String> patternParts, CommandType type) {
        try {
            List<String> params = lowerWildcardList(patternParts);
            params.add(type.name());
            return super.getDisposableTransaction()
                    .doQueryAndStream(
                            ExtendableCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( " + 
                                    multipleLowerLIKE("com_original", patternParts.size(), AND) + 
                                    " ) AND ( com_type IS ? ) ",
                            this.rowToCommandConversion,
                            params)
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<ExtendableCommand> fullSearchByExtendedPattern(
            Initiator initiator, String pattern) {
        if ( hasWildcard(pattern) ) {
            return this.findByPartsExtendedPattern(initiator, splitByWildcard(pattern));
        } else {
            return this.findBySingleExtendedPattern(pattern);
        }        
    }

    public List<ExtendableCommand> findBySingleExtendedPattern(String pattern) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ExtendableCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                                    "FROM commands " +
                                    "WHERE LOWER(com_extended) LIKE ? ",
                            this.rowToCommandConversion,
                            lowerWildcard(pattern))
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    private List<ExtendableCommand> findByPartsExtendedPattern(
            Initiator initiator, List<String> patternParts) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStream(
                            ExtendableCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE " + multipleLowerLIKE("com_extended", patternParts.size(), AND),
                            this.rowToCommandConversion,
                            lowerWildcardList(patternParts))
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<ExtendableCommand> searchInExtendedByPatternAndType(
            Initiator initiator, String pattern, CommandType type) {
        if ( hasWildcard(pattern) ) {
            return this.findByPartsExtendedPatternOfType(initiator, splitByWildcard(pattern), type);
        } else {
            return this.findBySingleExtendedPatternOfType(pattern, type);
        }        
    }

    private List<ExtendableCommand> findBySingleExtendedPatternOfType(
            String pattern, CommandType type) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ExtendableCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                                    "FROM commands " +
                                    "WHERE ( LOWER(com_extended) LIKE ? ) AND ( com_type IS ? ) ",
                            this.rowToCommandConversion,
                            lowerWildcard(pattern), type.name())
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    private List<ExtendableCommand> findByPartsExtendedPatternOfType(
            Initiator initiator, List<String> patternParts, CommandType type) {
        try {
            List<String> params = lowerWildcardList(patternParts);
            params.add(type.name());
            return super.getDisposableTransaction()
                    .doQueryAndStream(
                            ExtendableCommand.class,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( " + 
                                    multipleLowerLIKE("com_extended", patternParts.size(), AND) + 
                                    " ) AND ( com_type IS ? ) ",
                            this.rowToCommandConversion,
                            params)
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptyList();
        }
    }

    @Override
    public boolean save(
            Initiator initiator, ExtendableCommand command) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            boolean commandExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT * " +
                            "FROM commands " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ", 
                            lower(command.originalArgument()), command.type().name());
                        
            if ( commandExists ) {
                return transact
                        .doUpdateVarargParams(
                                "UDPATE commands " +
                                "SET com_extended = ? " +
                                "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ", 
                                command.extendedArgument(), 
                                lower(command.originalArgument()), 
                                command.type().name())
                        == 1;
            } else {
                return transact
                        .doUpdateVarargParams(
                                "INSERT INTO commands ( com_type, com_original, com_extended )" +
                                "VALUES ( ?, ?, ? ) " , 
                                command.type().name(),
                                command.originalArgument(),
                                command.extendedArgument())
                        == 1;
            }
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }

    @Override
    public boolean delete(
            Initiator initiator, ExtendableCommand command) {
        try {
            return 1 == super.getDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ",
                            lower(command.originalArgument()), command.type().name());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
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
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
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
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }
}
