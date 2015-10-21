/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.config;

import com.drs.beam.core.modules.ConfigModule;

/**
 *
 * @author Diarsid
 */
public interface ConfigModuleBuilder {
    
    static ConfigModule buildModule(){
        return new ConfigProvider();
    }
}
