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
import static diarsid.support.log.Logging.logFor;

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
                logFor(Starter.class).info("starting with: " + join(", ", args));                
                Procedure procedure = defineProcedure(args);
                if ( procedure.hasLaunchable() ) {     
                    getLauncher().launch(procedure);
                } else {
                    logFor(Starter.class).info("there is nothing to launch.");
                }                
            } catch (NoClassDefFoundError error) {
                logFor(Starter.class).error("some class is missed: ", error);
                logFor(Starter.class).error("check your classpath statement.");
            } catch (RequirementException | WorkflowBrokenException e) {
                logFor(Starter.class).error(e.getMessage());
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
        prompt.stream().forEach(System.out::println);
    }
}
