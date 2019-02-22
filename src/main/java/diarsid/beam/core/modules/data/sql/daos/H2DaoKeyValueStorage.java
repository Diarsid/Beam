/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.domain.entities.Attribute;
import diarsid.beam.core.modules.data.DaoKeyValueStorage;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.RowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import static diarsid.support.strings.StringUtils.lower;
import static diarsid.beam.core.domain.entities.Attribute.optionalAttribute;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_ATTRIBUTE;


class H2DaoKeyValueStorage 
        extends BeamCommonDao
        implements DaoKeyValueStorage {
    
    private final RowConversion<String> firstRowToValueConversion;
    
    H2DaoKeyValueStorage(DataBase dataBase) {
        super(dataBase);
        this.firstRowToValueConversion = (firstRow) -> {
            return (String) firstRow.get("value");
        };
    }

    @Override
    public Optional<String> get(String key) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            this.firstRowToValueConversion,
                            "SELECT key, value " +
                            "FROM key_value " +
                            "WHERE LOWER(key) IS ? ",
                            lower(key));
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return Optional.empty();
        }
    }

    @Override
    public boolean save(String key, String value) {
        try (JdbcTransaction transact = super.openTransaction()) {
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
            return super.openDisposableTransaction()
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
            super.openDisposableTransaction()
                    .doQuery(
                            (row) -> {
                                all.put(
                                        (String) row.get("key"), 
                                        (String) row.get("value"));
                            },
                            "SELECT key, value " +
                            "FROM key_value");
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
            return super.openDisposableTransaction()
                    .doQueryAndStream(
                            ROW_TO_ATTRIBUTE,
                            "SELECT key, value " +
                            "FROM key_value")
                    .collect(toSet());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            return emptySet();
        }
    }
}
