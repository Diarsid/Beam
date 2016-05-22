/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.shared.modules.ConfigModule;

/**
 *
 * @author Diarsid
 */
public interface DataBaseBuilder {
    
    DataBase buildDataBase(
            IoInnerModule ioInnerModule, ConfigModule configModule);
}
