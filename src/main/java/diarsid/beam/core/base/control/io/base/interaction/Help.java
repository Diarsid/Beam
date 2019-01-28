/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;

import java.util.List;

import static java.util.Arrays.asList;

import static diarsid.beam.core.application.environment.BeamEnvironment.similarity;

/**
 *
 * @author Diarsid
 */
public interface Help {
    
    public static boolean isHelpRequest(String s) {
        return s.equalsIgnoreCase("?") || 
               s.equalsIgnoreCase("h") || 
               similarity().isStrictSimilar("help", s);
    }
    
    public static Help asHelp(String... info) {
        return new HelpInfo(asList(info));
    }
    
    public static Help asHelp(List<String> info) {
        return new HelpInfo(info);
    }
    
    boolean isInfo();
    
    boolean isKey();
    
    default HelpKey asKey() {
        return (HelpKey) this;
    }
    
    default HelpInfo asInfo() {
        return (HelpInfo) this;
    }
}
