/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules;

import com.drs.beam.core.Modules;

/**
 *
 * @author Diarsid
 */
public interface ModulesContainerBuilder {
    
    public static Modules buildContainer(){
        return new ModulesContainer();
    }
}
