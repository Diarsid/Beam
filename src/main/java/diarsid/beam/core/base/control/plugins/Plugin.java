/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.plugins;

/**
 *
 * @author Diarsid
 */
public abstract class Plugin {
    
    private final String prefix;
    private final String name;
    
    public Plugin(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }
    
    public final String prefix() {
        return this.prefix;
    }
    
    public final boolean equalsByPrefix(Plugin other) {
        return this.prefix.equals(other.prefix);
    }
    
    public final String name() {
        return this.name;
    }
}
