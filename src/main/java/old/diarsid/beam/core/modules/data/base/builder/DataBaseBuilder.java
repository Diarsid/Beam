/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package old.diarsid.beam.core.modules.data.base.builder;


import old.diarsid.beam.core.modules.data.DataBase;

import diarsid.beam.core.modules.ConfigHolderModule;
import diarsid.beam.core.modules.IoModule;

/**
 *
 * @author Diarsid
 */
public interface DataBaseBuilder {
    
    DataBase buildDataBase(IoModule ioModule, ConfigHolderModule configModule);
}
