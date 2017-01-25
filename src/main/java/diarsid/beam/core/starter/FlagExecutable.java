/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.starter;

import static diarsid.beam.core.starter.FlagType.EXECUTABLE;

/**
 *
 * @author Diarsid
 */
public enum FlagExecutable implements Flag {

    REWRITE_SCRIPTS (
            "-scripts", 
            "reads configuration and rewrites scripts");

    private final String flag;
    private final FlagType type;
    private final String description;
    
    private FlagExecutable(String flag, String description) {
        this.flag = flag;
        this.description = description;
        this.type = EXECUTABLE;
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
    
}
