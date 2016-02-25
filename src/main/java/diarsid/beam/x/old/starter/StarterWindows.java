/*
 * project: Beam
 * author: Diarsid
 */

package diarsid.beam.x.old.starter;

import java.io.IOException;

import diarsid.beam.core.Beam;

import diarsid.beam.shared.modules.config.Config;
//import com.drs.beam.starter.Starter;

import diarsid.beam.x.old.util.config.jaxpdom.reader.ConfigReader;

/**
 *
 * @author Diarsid
 */
//class StarterWindows implements Starter{
class StarterWindows {
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
    
    //@Override
    public void runBeam(){
        String commandRunBeam = buildCommandForBeam();
        System.out.println(commandRunBeam);
        try{            
            Runtime.getRuntime().exec(commandRunBeam);
        } catch (IOException ioe){
            System.out.println(ioe.getMessage());
        }
    }
    
    //@Override
    public void runConsole(){
        String commandRunConsole = buildCommandForConsole();
        System.out.println(commandRunConsole);
        try{
            Runtime.getRuntime().exec(commandRunConsole);
        } catch (IOException ioe){
            System.out.println(ioe.getMessage());
        }
    }

    //@Override
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
                .append(dot)
                .append(semicolon)
                .append(this.configReader.getLibrariesLocation().replace("/", "\\"))
                .append("gem-injector-1.0.jar");
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
                .append(Config.CORE_JDBC_DRIVER)
                .append(equalSign)
                .append(this.configReader.getCoreDBDriver())
                .append(space);
        argumentsBuilder
                .append(Config.CORE_DB_NAME)
                .append(equalSign)
                .append(this.configReader.getCoreDBName())
                .append(space);
        argumentsBuilder
                .append(Config.CORE_JDBC_URL)
                .append(equalSign)
                .append(this.configReader.getCoreDBURL())
                .append(space);
        argumentsBuilder
                .append(Config.TASK_MANAGER_NAME)
                .append(equalSign)
                .append(this.configReader.getTaskManagerName())
                .append(space);
        argumentsBuilder
                .append(Config.EXECUTOR_NAME)
                .append(equalSign)
                .append(this.configReader.getExecutorName())
                .append(space);
        argumentsBuilder
                .append(Config.BEAM_ACCESS_NAME)
                .append(equalSign)
                .append(this.configReader.getAccessName())
                .append(space);  
        argumentsBuilder
                .append(Config.LOCATIONS_HANDLER_NAME)
                .append(equalSign)
                .append(this.configReader.getLocationsHandlerName())
                .append(space);
        argumentsBuilder
                .append(Config.WEB_PAGES_HANDLER_NAME)
                .append(equalSign)
                .append(this.configReader.getWebPagesHandlerName())
                .append(space);
        argumentsBuilder
                .append(Config.IMAGES_LOCATION)
                .append(equalSign)
                .append(this.configReader.getImagesLocation())
                .append(space);
        argumentsBuilder
                .append(Config.LIBRARIES_LOCATION)
                .append(equalSign)
                .append(this.configReader.getLibrariesLocation())
                .append(space);
        argumentsBuilder
                .append(Config.CORE_HOST)
                .append(equalSign)
                .append(this.configReader.getBeamHost())
                .append(space);
        argumentsBuilder
                .append(Config.CORE_PORT)
                .append(equalSign)
                .append(this.configReader.getBeamPort())
                .append(space);
        argumentsBuilder
                .append(Config.PROGRAMS_LOCATION)
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
                //.append(Console.class.getCanonicalName())
                .append(space)
                .append(buildArgumentsForConsole()); 
        return commandBuilder.toString();
    }
    
    private String buildArgumentsForConsole(){
        StringBuilder argumentsBuilder = new StringBuilder();
        argumentsBuilder
                .append(Config.TASK_MANAGER_NAME)
                .append(equalSign)
                .append(this.configReader.getTaskManagerName())
                .append(space);
        argumentsBuilder
                .append(Config.EXECUTOR_NAME)
                .append(equalSign)
                .append(this.configReader.getExecutorName())
                .append(space);
        argumentsBuilder
                .append(Config.BEAM_ACCESS_NAME)
                .append(equalSign)
                .append(this.configReader.getAccessName())
                .append(space);
        argumentsBuilder
                .append(Config.LOCATIONS_HANDLER_NAME)
                .append(equalSign)
                .append(this.configReader.getLocationsHandlerName())
                .append(space);
        argumentsBuilder
                .append(Config.WEB_PAGES_HANDLER_NAME)
                .append(equalSign)
                .append(this.configReader.getWebPagesHandlerName())
                .append(space);
        argumentsBuilder
                .append(Config.CORE_HOST)
                .append(equalSign)
                .append(this.configReader.getBeamHost())
                .append(space);
        argumentsBuilder
                .append(Config.CORE_PORT)
                .append(equalSign)
                .append(this.configReader.getBeamPort())
                .append(space);
        argumentsBuilder
                .append(Config.SYS_CONSOLE_NAME)
                .append(equalSign)
                .append(this.configReader.getSystemConsoleName())
                .append(space);
        argumentsBuilder
                .append(Config.SYS_CONSOLE_HOST)
                .append(equalSign)
                .append(this.configReader.getSystemConsoleHost())
                .append(space);
        argumentsBuilder
                .append(Config.SYS_CONSOLE_PORT)
                .append(equalSign)
                .append(this.configReader.getSystemConsolePort())
                .append(space);
        return argumentsBuilder.toString();
    }
}
