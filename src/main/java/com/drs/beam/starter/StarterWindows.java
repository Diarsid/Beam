/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.starter;

import java.io.IOException;

import com.drs.beam.external.sysconsole.Console;
import com.drs.beam.core.Beam;
import com.drs.beam.util.config.ConfigParam;
import com.drs.beam.util.config.reader.ConfigReader;

/**
 *
 * @author Diarsid
 */
class StarterWindows implements Starter{
    // Fields =============================================================================
        private final String equalSign = "=";
        private final String space = " ";
        private final String semicolon = ";";
        private final String dot = ".";
        private final ConfigReader configReader = ConfigReader.getReader();
        private boolean loadFromJar;
        
    // Constructors ======================================================================= 
    public StarterWindows() {
    }

    // Methods ============================================================================    
    
    @Override
    public void runBeam(){
        String commandRunBeam = buildCommandForBeam();
        System.out.println(commandRunBeam);
        try{            
            Runtime.getRuntime().exec(commandRunBeam);
        } catch (IOException ioe){
            System.out.println(ioe.getMessage());
        }
    }
    
    @Override
    public void runConsole(){
        String commandRunConsole = buildCommandForConsole();
        System.out.println(commandRunConsole);
        try{
            Runtime.getRuntime().exec(commandRunConsole);
        } catch (IOException ioe){
            System.out.println(ioe.getMessage());
        }
    }

    @Override
    public void takeArgs(String[] args) {
        if (args.length == 0){
            this.loadFromJar = true;
        } else if ("classes".equals(args[0])){
            this.loadFromJar = false;
        }
    }
    
    private String buildCommandForBeam(){
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder
                .append("cmd /c start javaw -cp ")
                // get lib folder + database jar
                .append(this.configReader.getLibrariesLocation().replace("/", "\\"))
                .append(this.configReader.getDbDriverJar())
                .append(semicolon)
                // current folder classpath to get rmi.policy and config.xml
                .append(dot);
        if (loadFromJar){
            commandBuilder
                    .append(semicolon)
                    .append("Beam.jar ");
        } else {
            commandBuilder.append(space);
        }
        commandBuilder
                .append("-Djava.security.policy=config\\rmi.policy ")
                .append("-Djava.rmi.server.hostname=").append(this.configReader.getBeamHost())
                .append(this.configReader.getJvmOptionsForBeam())
                .append(space)
                .append(Beam.class.getCanonicalName())
                .append(space)
                .append(buildArgumentsForBeam());
        
        return commandBuilder.toString();
    }
    
    private String buildArgumentsForBeam(){
        StringBuilder argumentsBuilder = new StringBuilder();
        argumentsBuilder
                .append(ConfigParam.CORE_DB_DRIVER)
                .append(equalSign)
                .append(this.configReader.getCoreDBDriver())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.CORE_DB_NAME)
                .append(equalSign)
                .append(this.configReader.getCoreDBName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.CORE_DB_URL)
                .append(equalSign)
                .append(this.configReader.getCoreDBURL())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.TASK_MANAGER_NAME)
                .append(equalSign)
                .append(this.configReader.getTaskManagerName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.EXECUTOR_NAME)
                .append(equalSign)
                .append(this.configReader.getExecutorName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.ORG_IO_NAME)
                .append(equalSign)
                .append(this.configReader.getAccessName())
                .append(space);       
        argumentsBuilder
                .append(ConfigParam.IMAGES_LOCATION)
                .append(equalSign)
                .append(this.configReader.getImagesLocation())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.LIBRARIES_LOCATION)
                .append(equalSign)
                .append(this.configReader.getLibrariesLocation())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.ORGANIZER_HOST)
                .append(equalSign)
                .append(this.configReader.getBeamHost())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.ORGANIZER_PORT)
                .append(equalSign)
                .append(this.configReader.getBeamPort())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.PROGRAMS_LOCATION)
                .append(equalSign)
                .append(this.configReader.getProgramsLocation())
                .append(space);
        return argumentsBuilder.toString();
    }
    
    private String buildCommandForConsole(){
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder
                .append("cmd /c start java -cp ")
                .append(dot);
        if (loadFromJar){
            commandBuilder
                    .append(semicolon)
                    .append("Beam.jar ");
        } else {
            commandBuilder.append(space);
        }
        commandBuilder
                .append("-Djava.security.policy=config\\rmi.policy ")
                .append("-Djava.rmi.server.hostname=").append(this.configReader.getSystemConsoleHost())
                .append(this.configReader.getJvmOptionsForSystemConsole())
                .append(space)
                .append(Console.class.getCanonicalName())
                .append(space)
                .append(buildArgumentsForConsole()); 
        return commandBuilder.toString();
    }
    
    private String buildArgumentsForConsole(){
        StringBuilder argumentsBuilder = new StringBuilder();
        argumentsBuilder
                .append(ConfigParam.TASK_MANAGER_NAME)
                .append(equalSign)
                .append(this.configReader.getTaskManagerName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.EXECUTOR_NAME)
                .append(equalSign)
                .append(this.configReader.getExecutorName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.ORG_IO_NAME)
                .append(equalSign)
                .append(this.configReader.getAccessName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.ORGANIZER_HOST)
                .append(equalSign)
                .append(this.configReader.getBeamHost())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.ORGANIZER_PORT)
                .append(equalSign)
                .append(this.configReader.getBeamPort())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.CONSOLE_NAME)
                .append(equalSign)
                .append(this.configReader.getSystemConsoleName())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.CONSOLE_HOST)
                .append(equalSign)
                .append(this.configReader.getSystemConsoleHost())
                .append(space);
        argumentsBuilder
                .append(ConfigParam.CONSOLE_PORT)
                .append(equalSign)
                .append(this.configReader.getSystemConsolePort())
                .append(space);
        return argumentsBuilder.toString();
    }
}
