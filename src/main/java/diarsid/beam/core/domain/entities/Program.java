/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import diarsid.beam.core.application.ProgramsCatalog;
import diarsid.beam.core.domain.actions.Callback;

import static diarsid.beam.core.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.util.Logs.logError;
import static diarsid.beam.core.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.util.PathUtils.indexOfLastFileSeparator;

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
            return this.fullName.substring(
                    indexOfLastFileSeparator(this.fullName), 
                    this.fullName.length());
        } else {
            return this.fullName;
        }        
    }
    
    public String getSubPath() {
        if ( containsPathSeparator(this.fullName) ) {
            return this.fullName.substring(
                    0, 
                    indexOfLastFileSeparator(this.fullName));
        } else {
            return "";
        }
    }

    public String getFullName() {
        return this.fullName;
    }
    
    public void invoke(Callback programNotFoundCallback, Callback exceptionCallback) {
        asyncDo(() -> {
            File program = this.programsCatalog.asFile(this);
            if ( program.exists() && program.isFile() ) {
                try {
                    Desktop.getDesktop().open(program);            
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
