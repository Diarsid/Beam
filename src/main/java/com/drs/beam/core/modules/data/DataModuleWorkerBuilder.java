/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.data;

import com.drs.beam.shared.modules.ConfigModule;
import com.drs.beam.core.modules.DataModule;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.data.base.builder.DataBaseBuilder;
import com.drs.beam.core.modules.data.daos.DaosInfo;
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
    public DataModule buildModule(){
        DataBase dataBase = DataBaseBuilder
                .buildDataBase(this.ioInnerModule, this.configModule);
        // obtain dao implementations package name.
        // "my.package.with.database.DataBaseInfo" -> "my.package.with.database."
        String daosPackageName = DaosInfo.class
                .getCanonicalName()
                .replace(DaosInfo.class.getSimpleName(), "");
        DataModule dataModule = 
                new DataModuleWorker(this.ioInnerModule, dataBase, daosPackageName);
        
        return dataModule;        
    }
}
