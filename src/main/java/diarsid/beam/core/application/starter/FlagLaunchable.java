/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.starter;

import static diarsid.beam.core.application.starter.FlagType.STARTABLE;

/**
 *
 * @author Diarsid
 */
public enum FlagLaunchable implements Flag {
    
    START_ALL (
            1000,
            "-all", 
            "launches both core and sysconsol as separate processes"),
    START_CORE (
            800,
            "-core", 
            "launches only core as a separate process"),
    START_SYSTEM_CONSOLE (
            600,
            "-sysconsole", 
            "launches only sysconsole as a separate process");
    
    private final int priority;
    private final String flag;
    private final FlagType type;
    private final String description;
    
    private FlagLaunchable(int priority, String flag, String description) {
        this.priority = priority;
        this.flag = flag;
        this.description = description;
        this.type = STARTABLE;
    }
    
    @Override
    public String text() {
        return this.flag;
    }
    
    @Override
    public String description() {
        return this.description;
    }

    @Override
    public FlagType type() {
        return this.type;
    }
    
    boolean hasLowerPriorityThan(FlagLaunchable anotherFlag) {
        return ( this.priority < anotherFlag.priority);
    }
    
}
