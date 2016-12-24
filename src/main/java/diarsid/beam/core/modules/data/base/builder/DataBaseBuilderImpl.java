/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.base.builder;

import old.diarsid.beam.core.modules.IoInnerModule;

import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.DataBaseBuilder;
import diarsid.beam.core.modules.data.base.DataBasesInfo;
import diarsid.beam.core.modules.ConfigModule;

/**
 *
 * @author Diarsid
 */
public class DataBaseBuilderImpl implements DataBaseBuilder {
    
    public DataBaseBuilderImpl() {
    }
    
    @Override
    public DataBase buildDataBase(
            IoInnerModule ioInnerModule, ConfigModule configModule) {
        
        DataBaseInitializer initializer = new DataBaseInitializer(ioInnerModule);
        DataBaseModel dataModel = new DataBaseModel();
        DataBaseVerifier verifier = 
                new DataBaseVerifier(ioInnerModule, initializer, dataModel);
        String dataBasePackageName = this.getDataBasePackageName(); 
        DataBaseProvider provider = 
                new DataBaseProvider(ioInnerModule, configModule, dataBasePackageName);
        
        DataBase dataBase = provider.getDataBase();
        verifier.verifyDataBase(dataBase);
        
        return dataBase;
    }

    private String getDataBasePackageName() {
        // obtain database implementations package name.
        // "my.package.with.database.DataBaseInfo" -> "my.package.with.database."
        String dataBasePackageName = DataBasesInfo.class
                .getCanonicalName()
                .replace(DataBasesInfo.class.getSimpleName(), "");
        return dataBasePackageName;
    }
}
