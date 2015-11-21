/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.io;

import com.drs.beam.core.modules.IoModule;

/**
 *
 * @author Diarsid
 */
public interface IoModuleBuilder {
    
    static IoModule buildModule(){
        return new IoModuleWorker();
    }
}
