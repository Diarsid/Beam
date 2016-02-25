/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.starter;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Diarsid
 */
class BatchScriptsExecutor {
    
    private final BatchScriptsProvider scripts;
    private final Desktop desktop;
    
    BatchScriptsExecutor(BatchScriptsProvider sp) {
        this.scripts = sp;
        this.desktop = Desktop.getDesktop();
    }
    
    void runBeam() throws IOException {
        this.desktop.open(this.scripts.getBeamCoreScript());
    }
    
    void runConsole() throws IOException {
        this.desktop.open(this.scripts.getBeamSysConsoleScript());
    }
}
