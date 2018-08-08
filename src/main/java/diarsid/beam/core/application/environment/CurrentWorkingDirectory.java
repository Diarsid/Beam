/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import diarsid.beam.core.Beam;
import diarsid.beam.core.base.exceptions.WorkflowBrokenException;
import diarsid.beam.core.base.util.Possible;

import static java.lang.String.format;

import static diarsid.beam.core.base.util.Possible.possibleButEmpty;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;

/**
 *
 * @author Diarsid
 */
public class CurrentWorkingDirectory {
    
    private static final CurrentWorkingDirectory INSTANCE;
    
    static {
        INSTANCE = new CurrentWorkingDirectory();
    }
    
    private final Path current;
    private final Possible<String> beamJarName;
    private final boolean containsBeamPackagesRoot;    

    private CurrentWorkingDirectory() {
        this.current = Paths.get(".").toAbsolutePath().normalize();
        String beamClassName = Beam.class.getCanonicalName();
        String beamPackagesRoot = beamClassName.substring(0, beamClassName.indexOf('.'));
        AtomicBoolean hasBeamPackagesRoot = new AtomicBoolean(false);
        this.beamJarName = possibleButEmpty();
        
        try {
            Files.list(this.current)
                    .map(path -> path.getFileName().toString())
                    .forEach(fileName -> {
                        if ( fileName.equals(beamPackagesRoot) ) {
                            hasBeamPackagesRoot.set(true);
                        }
                        if ( containsIgnoreCase(fileName, "beam") && fileName.endsWith(".jar") ) {
                            this.beamJarName.resetTo(fileName);
                        }
                    });
        } catch (IOException e) {
            throw new WorkflowBrokenException(
                    format("unexpected IOException during current working directory scanning: %s", 
                           e.getMessage()));
        }    
        
        if ( this.beamJarName.isNotPresent() && ! hasBeamPackagesRoot.get() ) {
            String message = format(
                    "Beam packages root (%s) not found in current working directory: %s!", 
                    beamPackagesRoot, this.current.toString());
            throw new WorkflowBrokenException(message);
        }
        
        this.containsBeamPackagesRoot = hasBeamPackagesRoot.get();        
    }
    
    public static CurrentWorkingDirectory currentWorkingDirectory() {
        return INSTANCE;
    }
    
    public Path path() {
        return this.current;
    }
    
    public String currentClassClasspath() {
        return this.beamJarName.or(".");
    } 
    
    public boolean isCurrentClassPackedInJar() {
        return this.beamJarName.isPresent();
    }
    
    public boolean isCurrentClassUnpacked() {
        return this.containsBeamPackagesRoot;
    }
}
