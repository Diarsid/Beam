/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.data;


import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsWordInIgnoreCase;


class SqlDataBaseActuator implements DataBaseActuator {
        
    private final DataBase dataBase;
    private final SqlDataBaseModel dataBaseModel;
    private final List<String> existingTableNames;
    private final List<String> existingForeignKeyNames;
    private final List<String> report;
    
    SqlDataBaseActuator(DataBase dataBase, SqlDataBaseModel dataBaseModel) {
        this.dataBase = dataBase;
        this.dataBaseModel = dataBaseModel;
        this.existingTableNames = new ArrayList<>();
        this.existingForeignKeyNames = new ArrayList<>();
        this.report = new ArrayList<>();
    }
    
    @Override
    public List<String> actuateAndGetReport() throws DataBaseActuationException {
        this.discoverTableNames();
        this.discoverForeignKeyNames();
        this.createRequiredTablesIfMissed();
        this.createRequiredForeignKeysIfMissed();
        return this.report;
    }
                
    private void discoverTableNames() throws DataBaseActuationException {
        try {            
            this.dataBase.transactionFactory()
                    .createDisposableTransaction()
                    .useJdbcDirectly(connection -> {
                        ResultSet results = connection
                                .getMetaData()
                                .getTables(null, null, "%", null);
                        while ( results.next() ) {
                            this.existingTableNames.add(results.getString("TABLE_NAME"));
                        }
                    });            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(SqlDataBaseActuator.class, ex);
            throw new DataBaseActuationException("Cannot discover data base table names.");
        }
    }
    
    private void discoverForeignKeyNames() throws DataBaseActuationException {
        try {
            this.dataBase.transactionFactory()
                    .createDisposableTransaction()
                    .useJdbcDirectly(connection -> {
                        DatabaseMetaData metaData = connection.getMetaData();
                        ResultSet results;
                        for (String tableName : this.existingTableNames) {
                            results = metaData.getImportedKeys(null, null, tableName);
                            while ( results.next() ) {
                                this.existingForeignKeyNames.add(results.getString("FK_NAME"));
                            }
                        }
                    });
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(SqlDataBaseActuator.class, ex);
            throw new DataBaseActuationException("Cannot discover data base constraint names.");
        }
    }
    
    private void createRequiredTablesIfMissed() throws DataBaseActuationException {
        List<SqlTable> requiredMissedTables = this.dataBaseModel
                .tables()
                .stream()
                .filter(table -> this.tableNotFoundInExisting(table))
                .collect(toList());
        
        if ( requiredMissedTables.isEmpty() ) {
            return;
        }
        
        this.createSqlObjectsAndCollectReports(requiredMissedTables);
    }
    
    private void createRequiredForeignKeysIfMissed() throws DataBaseActuationException {
        List<SqlConstraint> requiredMissedConstraints = this.dataBaseModel
                .constraints()
                .stream()
                .filter(constraint -> this.foreignKeyNotFoundInExisting(constraint))
                .collect(toList());
        
        if ( requiredMissedConstraints.isEmpty() ) {
            return;
        }
        
        this.createSqlObjectsAndCollectReports(requiredMissedConstraints);
    }
    
    private boolean tableNotFoundInExisting(SqlTable table) {
        return ! containsWordInIgnoreCase(this.existingTableNames, table.name());
    }
    
    private boolean foreignKeyNotFoundInExisting(SqlConstraint constraint) {
        return ! containsWordInIgnoreCase(this.existingForeignKeyNames, constraint.name());
    }
    
    private void createSqlObjectsAndCollectReports(List<? extends SqlObject> missedSqlObjects) 
            throws DataBaseActuationException {
        List<SqlObject> createdSqlObjects = new ArrayList<>();
        SqlObject currentlyProcessedObject = null;
        
        try (JdbcTransaction transact = this.dataBase.transactionFactory().createTransaction()) {
            
            for (SqlObject sqlObject : missedSqlObjects) {
                currentlyProcessedObject = sqlObject;
                transact.doUpdate(sqlObject.creationScript()); 
                createdSqlObjects.add(sqlObject);
            }
        
            List<String> perSqlObjectReports = createdSqlObjects
                    .stream()
                    .map(sqlObject -> format(
                            "SQL %s '%s' has been created.", 
                            sqlObject.type().name(), sqlObject.name()))
                    .collect(toList());
                    
            this.report.addAll(perSqlObjectReports);
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            
            logError(SqlDataBaseActuator.class, ex);
            
            String message;
            if ( nonNull(currentlyProcessedObject) ) {
                message = format(
                        "Cannot create SQL %s '%s': %s", 
                        currentlyProcessedObject.type().name(), 
                        currentlyProcessedObject.name(),
                        ex.getMessage());
            } else {
                message = ex.getMessage();
            }            
            
            throw new DataBaseActuationException(message);
        } 
    }
}
