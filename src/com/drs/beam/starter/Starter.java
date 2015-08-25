/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.starter;


/**
 *
 * @author Diarsid
 */
public interface Starter {
    public void runBeam();
    public void runConsole();   
    
    public static Starter getStarter(){
        String systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("win")){
            return new StarterWindows();
        } else if (systemName.contains("x")){
            return null;
        } else {
            System.out.println("Unknown OS.");
            System.exit(1);
            return null;
        }
    }
}
