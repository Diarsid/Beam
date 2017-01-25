/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.starter;

import static diarsid.beam.core.starter.FlagType.STARTABLE;

/**
 *
 * @author Diarsid
 */
public enum FlagStartable implements Flag {
    
    START_ALL (
            1000,
            "-all", 
            "launches both core and sysconsol as separate processes"),
    START_CORE (
            800,
            "-core", 
            "launches only core as a separate process"),
    START_CORE_INLINE (
            800,
            "-core-inline", 
            "launches only core in current terminal process"),
    START_SYSTEM_CONSOLE (
            600,
            "-sysconsole", 
            "launches only sysconsole as a separate process"),
    START_SYSTEM_CONSOLE_INLINE (
            600,
            "-sysconsole-inline", 
            "launches only sysconsole in current terminal process");
    
    private final int priority;
    private final String flag;
    private final FlagType type;
    private final String description;
    
    private FlagStartable(int priority, String flag, String description) {
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
    
    boolean hasLowerPriorityThan(FlagStartable anotherFlag) {
        return ( this.priority < anotherFlag.priority);
    }
    
}
