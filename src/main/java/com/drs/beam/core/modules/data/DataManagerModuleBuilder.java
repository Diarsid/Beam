/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.data;

import com.drs.beam.core.modules.data.base.DataBase;
import com.drs.beam.core.modules.DataManagerModule;
import com.drs.beam.core.modules.InnerIOModule;

/**
 *
 * @author Diarsid
 */
public interface DataManagerModuleBuilder {
    
    static DataManagerModule buildModule(InnerIOModule ioModule){        
        DataBaseInitializer initializer = new DataBaseInitializer(ioModule);
        DataBaseModel dataModel = new DataBaseModel();
        DataBaseVerifier verifier = new DataBaseVerifier(ioModule, initializer, dataModel);
        DataBaseProvider provider = new DataBaseProvider(ioModule);
        
        DataBase dataBase = provider.getDataBase();
        verifier.verifyDataBase(dataBase);
        
        DaoProvider daoProvider = new DaoProvider(ioModule, dataBase);
        
        DataManagerModule dataModule = new DataManager(dataBase, daoProvider);
        return dataModule;
    }
}
