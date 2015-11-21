/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.data.base.builder;

import com.drs.beam.core.modules.ConfigModule;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.data.DataBase;
import com.drs.beam.core.modules.data.base.DataBasesInfo;

/**
 *
 * @author Diarsid
 */
public interface DataBaseBuilder {
    
    static DataBase buildDataBase(IoInnerModule ioModule, ConfigModule configModule){
        DataBaseInitializer initializer = new DataBaseInitializer(ioModule);
        DataBaseModel dataModel = new DataBaseModel();
        DataBaseVerifier verifier = new DataBaseVerifier(ioModule, initializer, dataModel);
        // obtain database implementations package name.
        // "my.package.with.database.DataBaseInfo" -> "my.package.with.database."
        String dataBasePackageName = DataBasesInfo.class
                .getCanonicalName()
                .replace(DataBasesInfo.class.getSimpleName(), ""); 
        DataBaseProvider provider = 
                new DataBaseProvider(ioModule, configModule, dataBasePackageName);
        
        DataBase dataBase = provider.getDataBase();
        verifier.verifyDataBase(dataBase);
        
        return dataBase;
    }
}
