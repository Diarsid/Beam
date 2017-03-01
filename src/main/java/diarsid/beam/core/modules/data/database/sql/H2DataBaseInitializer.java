/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.database.sql;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;

import static diarsid.beam.core.Beam.getSystemInitiator;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsWordInIgnoreCase;


public class H2DataBaseInitializer implements SqlDataBaseInitializer {
    
    private final DataBase dataBase;
    private final InnerIoEngine ioEngine;
    private final List<String> existingTableNames;
    
    public H2DataBaseInitializer(InnerIoEngine ioEngine, DataBase dataBase) {
        this.ioEngine = ioEngine;
        this.dataBase = dataBase;
        this.existingTableNames = this.discoverTableNames();
    }
                
    private List<String> discoverTableNames() {
        List<String> existingNames = new ArrayList<>();
        try {            
            this.dataBase.transactionFactory()
                    .createDisposableTransaction()
                    .useJdbcDirectly(connection -> {
                        ResultSet rs = connection
                                .getMetaData()
                                .getTables(null, null, "%", null);
                        while ( rs.next() ) {
                            existingNames.add(rs.getString("TABLE_NAME"));
                        }
                    });
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            this.handleExceptionAndTerminate(ex);
        }
        return existingNames;
    }            

    private void handleExceptionAndTerminate(Exception ex) {
        logError(H2DataBaseInitializer.class, ex);
        this.ioEngine.reportAndExitLater(
                getSystemInitiator(), "Cannot verify existing data base tables.");
        throw new ModuleInitializationException();
    }
    
    private boolean tableNotFoundInExisting(SqlTable table) {
        return ! containsWordInIgnoreCase(this.existingTableNames, table.getName());
    }

    @Override
    public String initializeTableIfNecessaryAndProvideReport(SqlTable table) {
        if ( this.tableNotFoundInExisting(table) ) {
            return this.initializeTableAndGetReport(table);
        } else {
            return "";
        }
    }
    
    private String initializeTableAndGetReport(SqlTable table) {
        try {
            this.dataBase
                    .transactionFactory()
                    .createDisposableTransaction()
                    .doUpdate(table.getSqlCreationScript()); 
            
            return format("SQL table '%s' has been created.", table.getName());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DataBaseInitializer.class, ex);
            this.ioEngine.reportAndExitLater(
                    getSystemInitiator(), format("Cannot create '%s' SQL table.", table.getName()));
            throw new ModuleInitializationException();
        } 
    }
}
