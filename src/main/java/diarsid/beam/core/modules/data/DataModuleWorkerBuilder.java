/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data;

import java.util.List;

import diarsid.beam.core.application.configuration.Configuration;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.data.daos.sql.H2DaosProvider;
import diarsid.beam.core.modules.data.database.sql.H2DataBase;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseInitializer;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseModel;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseVerifier;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.core.Beam.getSystemInitiator;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.textToMessage;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logs.logError;


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
        this.loadDriver();
        InnerIoEngine ioEngine = this.ioModule.getInnerIoEngine();
        Configuration config = this.applicationComponentsHolderModule.getConfiguration();
        String dataBaseUrl = "jdbc:h2:" + config.getAsString("data.store") + "/BeamData";
        String user = config.getAsString("data.user");
        String pass = config.getAsString("data.pass");
        DataBase dataBase = new H2DataBase(dataBaseUrl, user, pass);
        
        DataBaseModel model = new H2DataBaseModel();
        DataBaseInitializer initializer = new H2DataBaseInitializer(ioEngine, dataBase);
        DataBaseVerifier verifier = new H2DataBaseVerifier(initializer);
        List<String> reports = verifier.verify(dataBase, model);        
        
        if ( nonEmpty(reports) ) {
            ioEngine.reportMessage(getSystemInitiator(), textToMessage(reports));
        }
        
        DaosProvider daosProvider = new H2DaosProvider(dataBase, this.ioModule);
        return new DataModuleWorker(dataBase, daosProvider);
    }
    
    private void loadDriver() {
        try {
            Class.forName("org.h2.Driver");
        } catch (Exception e) {
            logError(DataModuleWorkerBuilder.class, e);
            this.ioModule
                    .getInnerIoEngine()
                    .reportAndExitLater(
                            getSystemInitiator(), 
                            "Data base driver class loading failure.");
            throw new ModuleInitializationException();
        }
    }
}
