/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import diarsid.beam.core.application.catalogs.ProgramsCatalog;

import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.indexOfLastPathSeparator;

import diarsid.beam.core.base.control.io.base.interaction.CallbackEmpty;

/**
 *
 * @author Diarsid
 */
public class Program {
    
    private final ProgramsCatalog programsCatalog;
    private final String fullName;
    
    public Program(ProgramsCatalog programsCatalog, String fullName) {
        this.programsCatalog = programsCatalog;
        this.fullName = fullName;
    }
    
    public String getSimpleName() {
        if ( containsPathSeparator(this.fullName) ) {
            return this.fullName.substring(indexOfLastPathSeparator(this.fullName), 
                    this.fullName.length());
        } else {
            return this.fullName;
        }        
    }
    
    public String getSubPath() {
        if ( containsPathSeparator(this.fullName) ) {
            return this.fullName.substring(0, 
                    indexOfLastPathSeparator(this.fullName));
        } else {
            return "";
        }
    }

    public String getFullName() {
        return this.fullName;
    }
    
    public void runAsync(
            CallbackEmpty successCallback, 
            CallbackEmpty programNotFoundCallback, 
            CallbackEmpty exceptionCallback) {
        asyncDo(() -> {
            File program = this.programsCatalog.asFile(this);
            if ( program.exists() && program.isFile() ) {
                try {
                    Desktop.getDesktop().open(program);  
                    successCallback.call();
                } catch (IOException | IllegalArgumentException e) {
                    exceptionCallback.call();
                    logError(this.getClass(), e);
                }
            } else {
                programNotFoundCallback.call();
            }            
        });
    }
}
