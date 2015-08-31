/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.starter;

import com.drs.beam.Beam;
import com.drs.beam.external.console.Console;
import com.drs.beam.util.config.ConfigParam;
import com.drs.beam.util.config.ConfigReader;
import java.io.IOException;

/**
 *
 * @author Diarsid
 */
public class StarterWindows implements Starter{
    // Fields =============================================================================
        private final String equalSign = "=";
        private final String space = " ";
        private final String semicolon = ";";
        private final String dot = ".";
        private final ConfigReader reader = ConfigReader.getReader();
        private final boolean loadFromJar = "jar".equals(reader.getLoadingType());
        
    // Constructors ======================================================================= 
    public StarterWindows() {
    }

    // Methods ============================================================================    
    
    @Override
    public void runBeam(){
        StringBuilder argumentsBuilder = new StringBuilder();
        argumentsBuilder
                .append(ConfigParam.CORE_DB_DRIVER)
                .append(equalSign)
                .append(reader.getCoreDBDriver())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.CORE_DB_NAME)
                .append(equalSign)
                .append(reader.getCoreDBName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.CORE_DB_URL)
                .append(equalSign)
                .append(reader.getCoreDBURL())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.TASK_MANAGER_NAME)
                .append(equalSign)
                .append(reader.getTaskManagerName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.EXECUTOR_NAME)
                .append(equalSign)
                .append(reader.getOSExecutorName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.ORG_IO_NAME)
                .append(equalSign)
                .append(reader.getOrgIOName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.GUI_PLATFORM)
                .append(equalSign)
                .append(reader.getGuiPlatform())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.IMAGES_LOCATION)
                .append(equalSign)
                .append(reader.getImagesLocation())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.LIBRARIES_LOCATION)
                .append(equalSign)
                .append(reader.getLibrariesLocation())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.ORGANIZER_HOST)
                .append(equalSign)
                .append(reader.getOrganizerHost())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.ORGANIZER_PORT)
                .append(equalSign)
                .append(reader.getOrganizerPort())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.PROGRAMS_LOCATION)
                .append(equalSign)
                .append(reader.getProgramsLocation())
                .append(space);        
        
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder
                .append("cmd /c start javaw -XX:+OptimizeStringConcat -cp ")
                // get lib folder + database jar
                .append(reader.getLibrariesLocation().replace("/", "\\"))
                .append(reader.getDbDriverJar())
                .append(semicolon)
                // current folder classpath to get rmi.policy and config.xml
                .append(dot);
        if (loadFromJar){
            commandBuilder.append(semicolon).append("Beam.jar ");
        } else {
            commandBuilder.append(space);
        }
        commandBuilder
                .append("-Djava.security.policy=config\\rmi.policy ")
                .append("-Djava.rmi.server.hostname=").append(reader.getOrganizerHost())
                .append(space)
                .append(Beam.class.getCanonicalName())
                .append(space)
                .append(argumentsBuilder.toString());        
        try{
            Runtime.getRuntime().exec(commandBuilder.toString());
        } catch (IOException ioe){
            System.out.println(ioe.getMessage());
        }
        System.out.println(commandBuilder.toString());
    }
    
    @Override
    public void runConsole(){        
        StringBuilder argumentsBuilder = new StringBuilder();
        argumentsBuilder
                .append(ConfigParam.TASK_MANAGER_NAME)
                .append(equalSign)
                .append(reader.getTaskManagerName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.EXECUTOR_NAME)
                .append(equalSign)
                .append(reader.getOSExecutorName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.ORG_IO_NAME)
                .append(equalSign)
                .append(reader.getOrgIOName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.ORGANIZER_HOST)
                .append(equalSign)
                .append(reader.getOrganizerHost())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.ORGANIZER_PORT)
                .append(equalSign)
                .append(reader.getOrganizerPort())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.CONSOLE_NAME)
                .append(equalSign)
                .append(reader.getConsoleName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.CONSOLE_HOST)
                .append(equalSign)
                .append(reader.getConsoleHost())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.CONSOLE_PORT)
                .append(equalSign)
                .append(reader.getConsolePort())
                .append(space);
        
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append("cmd /c start java ");
        if (loadFromJar){
            commandBuilder.append("-cp Beam.jar ");
        }
        commandBuilder
                .append("-Djava.security.policy=config\\rmi.policy ")
                .append("-Djava.rmi.server.hostname=").append(reader.getConsoleHost())
                .append(space)
                .append(Console.class.getCanonicalName())
                .append(space)
                .append(argumentsBuilder.toString()); 
        System.out.println(commandBuilder.toString());
        try{
            Runtime.getRuntime().exec(commandBuilder.toString());
        } catch (IOException ioe){
            System.out.println(ioe.getMessage());
        }
    }
}