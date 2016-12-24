/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data;

import old.diarsid.beam.core.modules.DataModule;
import old.diarsid.beam.core.modules.IoInnerModule;

import diarsid.beam.core.modules.data.base.builder.DataBaseBuilderImpl;
import diarsid.beam.core.modules.data.daos.DaosInfo;
import diarsid.beam.core.modules.ConfigModule;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
class DataModuleWorkerBuilder implements GemModuleBuilder<DataModule> {
    
    private final IoInnerModule ioInnerModule;
    private final ConfigModule configModule;
    
    DataModuleWorkerBuilder(IoInnerModule ioModule, ConfigModule configModule) {
        this.ioInnerModule = ioModule;
        this.configModule = configModule;
    }
    
    @Override
    public DataModule buildModule() {
        DataBase dataBase = this.getDataBase();
        String daosPackageName = this.getDaosPackageName();
        DataModule dataModule = new DataModuleWorker(
                this.ioInnerModule, dataBase, daosPackageName);
        
        return dataModule;        
    }

    private DataBase getDataBase() {
        DataBaseBuilder builder = new DataBaseBuilderImpl();        
        return builder.buildDataBase(this.ioInnerModule, this.configModule);
    }

    private String getDaosPackageName() {
        // obtain dao implementations package name.
        // "my.package.with.database.DataBaseInfo" -> "my.package.with.database."
        String daosPackageName = DaosInfo.class
                .getCanonicalName()
                .replace(DaosInfo.class.getSimpleName(), "");
        return daosPackageName;        
    }
}
