/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.control.commands;

import static java.util.Arrays.stream;

import static diarsid.beam.core.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public enum EditableTarget {
    
    TARGET_COMMANDS,
    TARGET_NAME,
    TARGET_ORDER,
    TARGET_PLACE,
    TARGET_URL,
    TARGET_SHORTCUTS,
    TARGET_PATH,
    
    TARGET_UNDEFINED;

    public boolean isDefined() {
        return this != TARGET_UNDEFINED;
    }
    
    public boolean isNotDefined() {
        return this == TARGET_UNDEFINED;
    }
    
    public static EditableTarget targetOf(String name) {
        return stream(values())
                .filter(value -> lower(value.name()).equals(lower(name)))
                .findFirst()
                .orElse(TARGET_UNDEFINED);
    }
    
    public static EditableTarget argToTarget(String arg) {
        switch ( lower(arg) ) {
            case "name" : {
                return TARGET_NAME;
            }
            case "comm" :
            case "commands" :
            case "operations" : {
                return TARGET_COMMANDS;
            }   
            case "order" : {
                return TARGET_ORDER;
            }
            case "place" :
            case "placement" : {
                return TARGET_PLACE;
            }    
            case "short" :
            case "shortcuts" :
            case "shortcut" : {
                return TARGET_SHORTCUTS;
            }   
            case "url" :
            case "address" :
            case "link" : {
                return TARGET_URL;
            }   
            case "path" : {
                return TARGET_PATH;
            }
            
            default : {
                return TARGET_UNDEFINED;
            }
        }    
    }
}
