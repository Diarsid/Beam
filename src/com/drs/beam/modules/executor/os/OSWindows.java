/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.executor.os;

import com.drs.beam.modules.executor.os.exceptions.NoLocationException;
import com.drs.beam.modules.executor.os.exceptions.NoTargetException;
import com.drs.beam.modules.io.InnerIOInterface;
import com.drs.beam.util.config.ConfigContainer;
import com.drs.beam.util.config.ConfigParam;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
        
/**
 *
 * @author Diarsid
 */
public class OSWindows implements OS{    
    // Fields =============================================================================
    private final String PROGRAMS_LOCATION; 
    private final InnerIOInterface ioEngine;
    
    // Constructors =======================================================================
    public OSWindows(InnerIOInterface io) {
        this.PROGRAMS_LOCATION = ConfigContainer.getParam(ConfigParam.PROGRAMS_LOCATION)
                .replace("\\", "/")
                .toLowerCase()
                .intern();
        this.ioEngine = io;
    }
    
    // Methods ============================================================================
    @Override
    public void openLocation(String location){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{            
                    Desktop.getDesktop().open(new File(location));
                } catch (IOException e){
                    ioEngine.informAboutException(e, false);
                } catch (IllegalArgumentException argumentException){
                    ioEngine.informAboutError(location+" file doesn`t exist.", false);
                }
            }
        }).start();
        this.ioEngine.inform("opening...");
    }
    
    @Override
    public void openFileInLocation(String file, String location){
        // targetName pattern: myPr / myFil
        // corrected target name: myProject / myFile.ext or "" if not exists
        try{
            file = checkNameInLocation(file, location);
        } catch (NoLocationException nle){
            this.ioEngine.inform("Location not found.");
            return;
        } catch (NoTargetException nte){
            this.ioEngine.inform("File not found.");
            return;
        }
        
        if (file.length() > 0){
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder
                    .append(location)
                    .append("/")
                    .append(file);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{            
                        Desktop.getDesktop().open(new File(pathBuilder.toString()));
                    } catch (IOException e){
                        ioEngine.informAboutException(e, false);
                    } catch (IllegalArgumentException argumentException){
                        ioEngine.informAboutError("Unknown target", false);
                    }
                }
            }).start();
            this.ioEngine.inform("opening...");
        }        
    }
    
    @Override
    public void openFileInLocationWithProgram(String file, String location, String program){
        try{
            file = checkNameInLocation(file, location);
        } catch (NoLocationException nle){
            this.ioEngine.inform("Location not found.");
            return;
        } catch (NoTargetException nte){
            this.ioEngine.inform("File not found.");
            return;
        }
        
        try{
            program = checkNameInLocation(program, PROGRAMS_LOCATION);
        } catch (NoLocationException nle){
            this.ioEngine.inform("Program`s location not found.");
            return;
        } catch (NoTargetException nte){
            this.ioEngine.inform("Program not found.");
            return;
        }
        
        if (file.length()>0 && program.length()>0){
            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder
                    .append("cmd /c start ")
                    .append(PROGRAMS_LOCATION.replace("/", "\\"))
                    .append("\\")
                    .append(program)
                    .append(" ")
                    .append(location.replace("/", "\\"))
                    .append("\\")
                    .append(file);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Runtime.getRuntime().exec(commandBuilder.toString());
                    } catch (IOException e){
                        ioEngine.informAboutException(e, false);
                    } catch (IllegalArgumentException argumentException){
                        ioEngine.informAboutError("Unknown target", false);
                    }
                }
            }).start();
            this.ioEngine.inform("running...");
        }
    }
    
    @Override
    public void runProgram(String program){
        // program pattern: notep, NetBe
        // corrected program names: notepad.exe, NetBeans.lnk or "" if not exists
        try{
            program = checkNameInLocation(program, PROGRAMS_LOCATION);
        } catch (NoLocationException nle){
            this.ioEngine.inform("Program`s location not found.");
            return;
        } catch (NoTargetException nte){
            this.ioEngine.inform("Program not found.");
            return;
        }
        
        if (program.length() > 0){
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder
                    .append(PROGRAMS_LOCATION)
                    .append("/")
                    .append(program);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Desktop.getDesktop().open(new File(pathBuilder.toString()));
                    } catch (IOException e){
                        ioEngine.informAboutException(e, false);
                    } catch (IllegalArgumentException argumentException){
                        ioEngine.informAboutError("Unknown target", false);
                    }
                }
            }).start();
            this.ioEngine.inform("running...");
        }    
    }
    
    @Override
    public boolean ifDirectoryExists(String location){
        File dir = new File(location);
        if (!dir.exists()) {
            this.ioEngine.inform("This path doesn`t exists.");
            return false;
        } else if (!dir.isDirectory()) {
            this.ioEngine.inform("This isn`t a directory.");
            return false;
        } else {
            return true;
        }        
    }
    
    private String checkNameInLocation(String fileOrFolderName, String location) 
            throws NoTargetException, NoLocationException{
        
        File dir = new File(location);
        if (dir.exists() && dir.isDirectory()){
            List<String> matches = new ArrayList<>();
            
            if (fileOrFolderName.contains("-")){                
                List<String> fileNameFragments;
                fileNameFragments = Arrays.asList(fileOrFolderName.split("-"));
                boolean containsAllFragments;
                for(String element : dir.list()){
                    containsAllFragments = true;
                    for(String nameFragment : fileNameFragments){
                        if ( ! element.toLowerCase().contains(nameFragment)){
                            containsAllFragments = false;
                            break;
                        }
                    }
                    if (containsAllFragments) matches.add(element);
                }
            } else {
                for(String element : dir.list()){
                    if (element.toLowerCase().contains(fileOrFolderName)){
                        matches.add(element);
                    }
                }
            }
            
            if (matches.size() == 1){
                return matches.get(0);
            } else if (matches.size() > 1) {                
                int variantNumber = this.ioEngine.resolveVariantsWithExternalIO(
                        "There are several targets:", 
                        matches);
                if (variantNumber > 0){
                    return matches.get(variantNumber-1);
                } else if (variantNumber == -1){
                    return "";
                } else if (variantNumber == -2){
                    this.ioEngine.inform("There are no external IO engine now.");
                    return "";
                } else {
                    return "";
                }               
            } else {
                throw new NoTargetException();
            }
        } else {
            throw new NoLocationException();
        }
    } 
}
