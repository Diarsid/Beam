/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.executor.os;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import static java.awt.SystemColor.desktop;

import com.drs.beam.core.modules.InnerIOModule;

/**
 *
 * @author Diarsid
 */
public class RunnableDesktopOpenTask implements Runnable {
    private final String target;
    private final InnerIOModule ioEngine;

    RunnableDesktopOpenTask(InnerIOModule ioEngine, String target) {
        this.target = target;
        this.ioEngine = ioEngine;
    }
    
    @Override
    public void run() {
        try {
            Desktop.getDesktop().open(new File(target));
        } catch (IOException e) {
            ioEngine.reportException(e, "Run task with Desktop -> IOException: given path may be invalid.");
        } catch (IllegalArgumentException argumentException) {
            ioEngine.reportError("Unknown target");
        }
    }
}
