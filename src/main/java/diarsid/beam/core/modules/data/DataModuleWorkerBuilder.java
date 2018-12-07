/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data;

import java.util.List;

import org.slf4j.Logger;

import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataBaseActuationException;
import diarsid.beam.core.base.data.DataBaseActuator;
import diarsid.beam.core.base.data.DataBaseModel;
import diarsid.beam.core.base.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.data.sql.daos.H2DaosProvider;
import diarsid.beam.core.modules.data.sql.database.H2DataBase;
import diarsid.beam.core.modules.data.sql.database.H2DataBaseModel;
import diarsid.support.configuration.Configuration;

import com.drs.gem.injector.module.GemModuleBuilder;

import static java.lang.String.format;

import static diarsid.beam.core.base.data.DataBaseActuator.getActuatorFor;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.support.log.Logging.logFor;


/**
 *
 * @author Diarsid
 */
public class DataModuleWorkerBuilder implements GemModuleBuilder<DataModule> {
    
    private final ApplicationComponentsHolderModule applicationComponentsHolderModule;
    
    public DataModuleWorkerBuilder(
            ApplicationComponentsHolderModule applicationComponentsHolderModule) {
        this.applicationComponentsHolderModule = applicationComponentsHolderModule;
    }

    @Override
    public DataModule buildModule() {
        Configuration config = this.applicationComponentsHolderModule.configuration();
        this.loadDriver(config.asString("data.driver"));
        
        DataBase dataBase = new H2DataBase(config);
        DataBaseModel dataBaseModel = new H2DataBaseModel();
        
        this.actuateDataBase(dataBase, dataBaseModel);
        
        DaosProvider daosProvider = new H2DaosProvider(
                dataBase, this.applicationComponentsHolderModule);
        DataModule dataModule = new DataModuleWorker(dataBase, daosProvider);
        
        return dataModule;
    }
    
    private void loadDriver(String driverClassName) {
        try {
            Class.forName(driverClassName);
        } catch (Exception e) {
            String message = format("Cannot load %s JDBC driver.", driverClassName);
            logFor(this).error(message, e);
            throw new ModuleInitializationException(message);
        }
    }
    
    private void actuateDataBase(DataBase dataBase, DataBaseModel dataBaseModel) {
        try {
            DataBaseActuator actuator = getActuatorFor(dataBase, dataBaseModel);
            List<String> reports = actuator.actuateAndGetReport();
            
            if ( nonEmpty(reports) ) {
                Logger logger = logFor(this);
                reports.forEach(report -> logger.info(report));
            }
        } catch (DataBaseActuationException e) {
            logFor(this).error(e.getMessage(), e);
            throw new ModuleInitializationException(e.getMessage());
        }
    }
}
