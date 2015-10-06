/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.starter;

import java.util.List;

/**
 *
 * @author Diarsid
 */
public class ModulesLoader {
    
    public static void main(String[] args) {
        RemoteLocator locator = new RemoteLocator();
        List<String> modulesToStart = locator.defineModulesToStart();        
        if (modulesToStart.size() > 0){            
            Starter starter = Starter.getStarter();
            starter.takeArgs(args);
            if (modulesToStart.contains("beam")){
                starter.runBeam();
                if (modulesToStart.contains("console")){
                    while (!locator.isBeamWorking()){
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ie) {
                            System.out.println(ie.getMessage());
                        }
                    }                    
                    starter.runConsole();
                }
            } else if (modulesToStart.contains("console")){
                starter.runConsole();
            }
        }        
    }
}
