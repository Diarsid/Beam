/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data;

import java.util.List;

import diarsid.beam.core.application.environment.Configuration;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataBaseActuationException;
import diarsid.beam.core.base.data.DataBaseActuator;
import diarsid.beam.core.base.data.DataBaseModel;
import diarsid.beam.core.base.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.data.sql.daos.H2DaosProvider;
import diarsid.beam.core.modules.data.sql.database.H2DataBase;
import diarsid.beam.core.modules.data.sql.database.H2DataBaseModel;

import com.drs.gem.injector.module.GemModuleBuilder;

import static java.lang.String.format;

import static diarsid.beam.core.Beam.systemInitiator;
import static diarsid.beam.core.base.data.DataBaseActuator.getActuatorFor;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.infoWithHeader;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.info;


/**
 *
 * @author Diarsid
 */
public class DataModuleWorkerBuilder implements GemModuleBuilder<DataModule> {
    
    private final IoModule ioModule;
    private final ApplicationComponentsHolderModule applicationComponentsHolderModule;
    
    public DataModuleWorkerBuilder(
            ApplicationComponentsHolderModule applicationComponentsHolderModule,
            IoModule ioModule) {
        this.ioModule = ioModule;
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
                dataBase, this.ioModule, this.applicationComponentsHolderModule);
        return new DataModuleWorker(dataBase, daosProvider);
    }
    
    private void loadDriver(String driverClassName) {
        try {
            Class.forName(driverClassName);
        } catch (Exception e) {
            logError(DataModuleWorkerBuilder.class, e);
            this.ioModule
                    .getInnerIoEngine()
                    .reportAndExitLater(
                            systemInitiator(), 
                            format("Cannot load %s JDBC driver.", driverClassName));
            throw new ModuleInitializationException();
        }
    }
    
    private void actuateDataBase(DataBase dataBase, DataBaseModel dataBaseModel) {
        try {
            DataBaseActuator actuator = getActuatorFor(dataBase, dataBaseModel);
            List<String> reports = actuator.actuateAndGetReport();
            
            if ( nonEmpty(reports) ) {
                this.ioModule
                        .getInnerIoEngine()
                        .reportMessage(systemInitiator(), info(reports));
            }
        } catch (DataBaseActuationException ex) {
            this.ioModule
                    .getInnerIoEngine()
                    .reportAndExitLater(
                            systemInitiator(), ex.getMessage());
            throw new ModuleInitializationException();
        }
    }
}
