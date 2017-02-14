/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.starter;


import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.application.catalogs.ScriptsCatalog;
import diarsid.beam.core.application.configuration.Configuration;

import static java.lang.String.join;
import static java.util.Arrays.stream;

import static diarsid.beam.core.application.catalogs.ApplicationCatalogs.getScriptsCatalog;
import static diarsid.beam.core.application.configuration.ApplicationConfiguration.getConfiguration;
import static diarsid.beam.core.starter.Flags.flagOf;
import static diarsid.beam.core.starter.Flags.formatToPrintables;
import static diarsid.beam.core.base.util.ArraysUtil.isEmpty;
import static diarsid.beam.core.base.util.Logs.log;
import static diarsid.beam.core.base.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
public class Starter {
    
    private Starter() {
    }
    
    public static void main(String[] args) {
        welcome();
        if ( isEmpty(args) ) {
            printPromptAndExit();
        } else {
            try {
                log(Starter.class, "launched with: " + join(", ", args));
                Configuration configuration = getConfiguration();
                ScriptsCatalog scriptsCatalog = getScriptsCatalog();
                Procedure procedure = readFlags(args);
                StartRunner environment = new StartRunner(configuration, scriptsCatalog);                
                environment.process(procedure);
            } catch (NoClassDefFoundError error) {
                logError(Starter.class, "some class is missed:", error);
                logError(Starter.class, "check your classpath statement.");
            }
        }
    }
    
    private static void welcome() {
        System.out.println();
        System.out.println("  ==== Beam.core Starter ==== ");
        System.out.println();
    }
    
    private static void printPromptAndExit() {
        List<String> prompt = new ArrayList();
        prompt.add("  No options specified.");
        prompt.add("");
        prompt.add("  Options specifying which part should be launched:");
        prompt.addAll(formatToPrintables(FlagStartable.values()));
        prompt.add("");
        prompt.add("  Options specifying how launched part could be configured:");
        prompt.addAll(formatToPrintables(FlagConfigurable.values()));
        prompt.add("");
        prompt.add("  Options specifying additional utility operations:");
        prompt.addAll(formatToPrintables(FlagExecutable.values()));
        prompt.stream().forEach(System.out::println);
    }
    
    private static Procedure readFlags(String[] flags) {
        Procedure procedure = new Procedure();
        stream(flags)
                .map(flagString -> flagOf(flagString))
                .filter(optionalFlag -> optionalFlag.isPresent())      
                .map(optionalFlag -> optionalFlag.get())
                .forEach(flag -> procedure.acceptFlag(flag));
        procedure.flagsAccepted();
        return procedure;
    }
}
