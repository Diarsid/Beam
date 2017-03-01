/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.starter;


import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.base.exceptions.RequirementException;
import diarsid.beam.core.base.exceptions.WorkflowBrokenException;

import static java.lang.String.join;

import static diarsid.beam.core.application.starter.Flags.formatToPrintables;
import static diarsid.beam.core.application.starter.Launcher.getLauncher;
import static diarsid.beam.core.application.starter.Procedure.defineProcedure;
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
                log(Starter.class, "starting with: " + join(", ", args));                
                Procedure procedure = defineProcedure(args);
                if ( procedure.hasLaunchable() ) {     
                    getLauncher().launch(procedure);
                } else {
                    log(Starter.class, "there is nothing to launch.");
                }                
            } catch (NoClassDefFoundError error) {
                logError(Starter.class, "some class is missed: ", error);
                logError(Starter.class, "check your classpath statement.");
            } catch (RequirementException | WorkflowBrokenException e) {
                logError(Starter.class, e.getMessage());
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
        prompt.addAll(formatToPrintables(FlagLaunchable.values()));
        prompt.add("");
        prompt.add("  Options specifying how launched part could be configured:");
        prompt.addAll(formatToPrintables(FlagConfigurable.values()));
        prompt.add("");
        prompt.stream().forEach(System.out::println);
    }
}
