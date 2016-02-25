/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.base.builder;

import diarsid.beam.shared.modules.ConfigModule;

import diarsid.beam.core.modules.IoInnerModule;

import diarsid.beam.core.modules.data.DataBase;

import diarsid.beam.core.modules.data.base.DataBasesInfo;

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
