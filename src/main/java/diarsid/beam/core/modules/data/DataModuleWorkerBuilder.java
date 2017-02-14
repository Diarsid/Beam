/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data;

import java.util.List;

import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.data.daos.sql.H2DaosProvider;
import diarsid.beam.core.modules.data.database.sql.H2DataBase;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseInitializer;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseModel;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseVerifier;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.core.Beam.getSystemInitiator;
import static diarsid.beam.core.control.io.base.Messages.text;
import static diarsid.beam.core.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.util.Logs.logError;


/**
 *
 * @author Diarsid
 */
public class DataModuleWorkerBuilder implements GemModuleBuilder<DataModule> {
    
    private final IoModule ioModule;
    
    public DataModuleWorkerBuilder(IoModule ioModule) {
        this.ioModule = ioModule;
    }

    @Override
    public DataModule buildModule() {
        this.loadDriver();
        InnerIoEngine ioEngine = this.ioModule.getInnerIoEngine();
        DataBase dataBase = new H2DataBase("jdbc:h2:./../res/data/BeamData");
        
        DataBaseModel model = new H2DataBaseModel();
        DataBaseInitializer initializer = new H2DataBaseInitializer(ioEngine, dataBase);
        DataBaseVerifier verifier = new H2DataBaseVerifier(initializer);
        List<String> reports = verifier.verify(dataBase, model);        
        
        if ( nonEmpty(reports) ) {
            ioEngine.reportMessage(getSystemInitiator(), text(reports));
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
