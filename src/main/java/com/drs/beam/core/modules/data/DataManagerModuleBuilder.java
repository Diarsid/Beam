/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.data;

import com.drs.beam.core.modules.ConfigModule;
import com.drs.beam.core.modules.DataManagerModule;
import com.drs.beam.core.modules.InnerIOModule;
import com.drs.beam.core.modules.data.base.builder.DataBaseBuilder;
import com.drs.beam.core.modules.data.daos.DaosInfo;

/**
 *
 * @author Diarsid
 */
public interface DataManagerModuleBuilder {
    
    static DataManagerModule buildModule(InnerIOModule ioModule, ConfigModule configModule){ 
        DataBase dataBase = DataBaseBuilder.buildDataBase(ioModule, configModule);
        // obtain dao implementations package name.
        // "my.package.with.database.DataBaseInfo" -> "my.package.with.database."
        String daosPackageName = DaosInfo.class
                .getCanonicalName()
                .replace(DaosInfo.class.getSimpleName(), "");
        DataManagerModule dataModule = new DataManager(ioModule, dataBase, daosPackageName);
        return dataModule;
    }
}
