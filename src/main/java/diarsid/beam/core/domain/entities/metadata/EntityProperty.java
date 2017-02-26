/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities.metadata;

import static java.util.Arrays.stream;

import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public enum EntityProperty {
    
    COMMANDS,
    NAME,
    TEXT,
    ORDER,
    WEB_PLACE,
    WEB_DIRECTORY,
    WEB_URL,
    SHORTCUTS,
    FILE_URL,
    
    UNDEFINED_PROPERTY;

    public boolean isDefined() {
        return this != UNDEFINED_PROPERTY;
    }
    
    public boolean isUndefined() {
        return this == UNDEFINED_PROPERTY;
    }
    
    public static EntityProperty propertyOf(String property) {
        return stream(values())
                .filter(value -> lower(value.name()).equals(lower(property)))
                .findFirst()
                .orElse(UNDEFINED_PROPERTY);
    }
    
    public boolean isOneOf(EntityProperty... possibleProperties) {
        return stream(possibleProperties)
                .anyMatch(possibleProperty -> this.equals(possibleProperty));
    }
    
    public boolean isNotOneOf(EntityProperty... possibleProperties) {
        return stream(possibleProperties)
                .noneMatch(possibleProperty -> this.equals(possibleProperty));
    }
    
    public static EntityProperty argToProperty(String arg) {
        switch ( lower(arg) ) {
            case "name" : {
                return NAME;
            }
            case "txt" : 
            case "tex" : 
            case "text" : {
                return TEXT;
            }
            case "comm" :
            case "commands" :
            case "operations" : {
                return COMMANDS;
            }   
            case "order" : {
                return ORDER;
            }
            case "place" :
            case "placement" : {
                return WEB_PLACE;
            }
            case "short" :
            case "shortcuts" :
            case "shortcut" : {
                return SHORTCUTS;
            }   
            case "dir" :
            case "webdir" : 
            case "directory" : 
            case "webdirectory" : {
                return WEB_DIRECTORY;
            }    
            case "url" :
            case "address" :
            case "link" : {
                return WEB_URL;
            }   
            case "path" : {
                return FILE_URL;
            }
            
            default : {
                return UNDEFINED_PROPERTY;
            }
        }    
    }
}
