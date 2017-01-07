/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data;

import diarsid.beam.core.modules.ConfigHolderModule;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.data.base.H2DataBase;
import diarsid.beam.core.modules.data.daos.sql.h2.H2DaosProvider;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.core.config.Config.CORE_JDBC_URL;

/**
 *
 * @author Diarsid
 */
public class DataModuleWorkerBuilder implements GemModuleBuilder<DataModule> {
    
    private final IoModule ioModule;
    private final ConfigHolderModule configHolderModule;
    
    public DataModuleWorkerBuilder(IoModule ioModule, ConfigHolderModule configHolderModule) {
        this.ioModule = ioModule;
        this.configHolderModule = configHolderModule;
    }

    @Override
    public DataModule buildModule() {
        DataBase dataBase = new H2DataBase(this.configHolderModule.get(CORE_JDBC_URL));
        DaosProvider daosProvider = new H2DaosProvider(dataBase, ioModule);
        return new DataModuleWorker(dataBase, daosProvider);
    }
}
