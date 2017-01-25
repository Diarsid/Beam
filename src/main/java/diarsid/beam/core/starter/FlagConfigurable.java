/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.starter;

import static diarsid.beam.core.starter.FlagType.CONFIGURABLE;

/**
 *
 * @author Diarsid
 */
enum FlagConfigurable implements Flag {
    
    NO_DEBUG (
            "-no-debug", 
            "disables all debugger output"),
    NO_FILE_DEBUG (
            "-no-file-debug", 
            "disables only file debugger output"),
    NO_CONSOLE_DEBUG (
            "-no-console-debug", 
            "disables only console debugger output"),
    NO_CONSOLE_LOG (
            "-no-console-log", 
            "disables all console logging");
    
    private final String flag;
    private final FlagType type;
    private final String description;
    
    private FlagConfigurable(String flag, String description) {
        this.flag = flag;
        this.description = description;
        this.type = CONFIGURABLE;
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
