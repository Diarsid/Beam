/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.domain.entities.Attribute;
import diarsid.beam.core.modules.data.DaoKeyValueStorage;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.PerRowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.domain.entities.Attribute.optionalAttribute;


class H2DaoKeyValueStorage 
        extends BeamCommonDao
        implements DaoKeyValueStorage {
    
    private final PerRowConversion<Attribute> rowToAttributeConversion;
    
    H2DaoKeyValueStorage(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
        this.rowToAttributeConversion = (row) -> {
            return new Attribute(
                    (String) row.get("key"),
                    (String) row.get("value"));
        };
    }

    @Override
    public Optional<String> get(String key) {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            String.class,
                            "SELECT key, value " +
                            "FROM key_value " +
                            "WHERE LOWER(key) IS ? ",
                            (firstRow) -> {
                                return Optional.of( (String) firstRow.get("value"));
                            },
                            lower(key));
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return Optional.empty();
        }
    }

    @Override
    public boolean save(String key, String value) {
        try (JdbcTransaction transact = super.getTransaction()) {
            boolean exists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT key " +
                            "FROM key_value " +
                            "WHERE LOWER(key) IS ? ",
                            lower(key));
            
            int saved;
            if ( exists ) {
                saved = transact
                        .doUpdateVarargParams(
                                "UPDATE key_value " +
                                "SET value = ? " +
                                "WHERE LOWER(key) IS ? ", 
                                value, lower(key));
            } else {
                saved = transact
                        .doUpdateVarargParams(
                                "INSERT INTO key_value (key, value) " +
                                "VALUES ( ?, ? )", 
                                key, value);
            }
            
            transact
                    .ifTrue( saved != 1 )
                    .rollbackAndProceed();
            
            return ( saved == 1 );
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }        
    }

    @Override
    public boolean delete(String key) {
        try {
            return super.getDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM key_value " +
                            "WHERE LOWER(key) IS ? ", 
                            lower(key)) 
                    == 1;
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return false;
        }
    }

    @Override
    public Map<String, String> getAll() {
        Map<String, String> all = new HashMap<>();
        try {
            super.getDisposableTransaction()
                    .doQuery(
                            "SELECT key, value " +
                            "FROM key_value",
                            (row) -> {
                                all.put(
                                        (String) row.get("key"), 
                                        (String) row.get("value"));
                            });
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
        }
        return all;
    }

    @Override
    public Optional<Attribute> getAttribute(String key) {
        return optionalAttribute(key, this.get(key));
    }

    @Override
    public boolean saveAttribute(Attribute attribute) {
        return this.save(attribute.name(), attribute.content());
    }

    @Override
    public boolean deleteAttribute(Attribute attribute) {
        return this.delete(attribute.name());
    }

    @Override
    public Set<Attribute> getAllAttributes() {
        try {
            return super.getDisposableTransaction()
                    .doQueryAndStream(
                            Attribute.class,
                            "SELECT key, value " +
                            "FROM key_value",
                            this.rowToAttributeConversion)
                    .collect(toSet());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptySet();
        }
    }
}
