/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.external.sysconsole;

import com.drs.beam.external.ExternalIOInterface;
import com.drs.gem.injector.core.Declaration;
import com.drs.gem.injector.core.GemInjector;

/**
 *
 * @author Diarsid
 */
public class SysConsole {
    
    private static ExternalIOInterface externalIo;
    
    private SysConsole() {
    }
    
    public static void main(String[] args) {
        Declaration modules = new SysConsoleModulesDeclaration();
        GemInjector.buildContainer("console", modules);
        GemInjector.getContainer("console").init();
        GemInjector.clear();
    }
    
    public static void saveExternalIOinStaticContext(ExternalIOInterface ex) {
        externalIo = ex;
    }
}
