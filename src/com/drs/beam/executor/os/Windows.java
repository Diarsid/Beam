/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.executor.os;

import com.drs.beam.io.BeamIO;
import com.drs.beam.io.InnerIOIF;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.StringJoiner;
        
/**
 *
 * @author Diarsid
 */
public class Windows implements OS{    
    // Fields ==================================================================
    private final String PROGRAMS_LOCATION;
    private final String CMD_START = "cmd /c start ";
    private final String NATIVE_SEPARATOR = "/";
    private final String WIN_SEPARATOR = "\\";
    private final String SPACE = " ";
    
    private final Desktop desktop;
    private final InnerIOIF ioEngine;
    
    // Constructors ============================================================
    public Windows(String programs) {
        this.PROGRAMS_LOCATION = programs;
        this.desktop = Desktop.getDesktop();
        this.ioEngine = BeamIO.getInnerIO();
    }
    
    // Methods =================================================================
    @Override
    public void openLocation(String location){
        try{
            desktop.open(new File(location));
        } catch (IOException e){
            ioEngine.informAboutException(e, false);
        }        
    }
    
    @Override
    public void openFileInLocation(String file, String location){
        // targetName pattern: myPr / myFil
        // corrected target name: myProject / myFile.ext or "" if not exists
        file = checkNameInLocation(file, location);
        if (file.length() > 0){
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder
                    .append(location)
                    .append(NATIVE_SEPARATOR)
                    .append(file);
            try{
                desktop.open(new File(pathBuilder.toString()));
            } catch (IOException e){
                ioEngine.informAboutException(e, false);
            }
        }        
    }
    
    @Override
    public void openFileInLocationWithProgram(String file, String location, String program){
        file = checkNameInLocation(file, location);
        program = checkNameInLocation(program, PROGRAMS_LOCATION);
        if (file.length()>0 && program.length()>0){
            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder
                    .append(CMD_START)
                    .append(PROGRAMS_LOCATION.replace(NATIVE_SEPARATOR, WIN_SEPARATOR))
                    .append(WIN_SEPARATOR)
                    .append(program)
                    .append(SPACE)
                    .append(location.replace(NATIVE_SEPARATOR, WIN_SEPARATOR))
                    .append(WIN_SEPARATOR)
                    .append(file);
            try{
                Runtime.getRuntime().exec(commandBuilder.toString());
            }catch (IOException e){
                ioEngine.informAboutException(e, false);
            }            
        }
    }
    
    @Override
    public void runProgram(String program){
        // program pattern: notep, NetBe
        // corrected program names: notepad.exe, NetBeans.lnk or "" if not exists
        program = checkNameInLocation(program, PROGRAMS_LOCATION);
        if (program.length() > 0){
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder
                    .append(PROGRAMS_LOCATION)
                    .append(NATIVE_SEPARATOR)
                    .append(program);
            try{
                desktop.open(new File(pathBuilder.toString()));
            } catch (IOException e){
                ioEngine.informAboutException(e, false);
            }
        }    
    }
    
    @Override
    public boolean ifDirectoryExists(String location){
        File dir = new File(location);
        if (!dir.exists()) {
            ioEngine.inform("this path doesn`t exists.");
            return false;
        } else if (!dir.isDirectory()) {
            ioEngine.inform("this isn`t a directory.");
            return false;
        } else {
            return true;
        }        
    }
    
    private String checkNameInLocation(String fileOrFolderName, String location){
        File dir = new File(location);
        if (dir.exists() && dir.isDirectory()){
            String[] content = dir.list();
            int matchesQty = 0;
            List<String> matches = new ArrayList<String>();
            for(String element : content){
                if (element.contains(fileOrFolderName)){
                    fileOrFolderName = element;
                    matchesQty++;
                    matches.add(element);
                }
            }
            if (matchesQty == 1){
                return fileOrFolderName;
            } else if (matchesQty > 1) {
                StringJoiner sj = new StringJoiner(", ");
                for(String s : matches){
                    sj.add(s);
                }
                ioEngine.inform("There are several targets with such names: "+sj.toString());
                return "";
            } else {
                ioEngine.informAboutError("Such file/folder was not found in this location", false);
                return "";
            }
        } else {
            ioEngine.informAboutError("Such location doesn`t exists in the system!", false);
            return "";
        }
    }    
    
}
