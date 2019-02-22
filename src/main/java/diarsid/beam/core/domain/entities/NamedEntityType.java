/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities;

import static diarsid.support.strings.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public enum NamedEntityType {
    
    LOCATION {
        @Override 
        public String displayName() {
            return "Location";
        }
    },
    WEBPAGE {
        @Override 
        public String displayName() {
            return "WebPage";
        }
    },
    PROGRAM {
        @Override 
        public String displayName() {
            return "Program";
        }
    },
    BATCH {
        @Override 
        public String displayName() {
            return "Batch";
        }
    },
    UNDEFINED_ENTITY {
        @Override
        public String displayName() {
            return "undefined";
        }
    };
    
    public static NamedEntityType fromString(String type) {
        switch ( lower(type) ) {
            case "location" : {
                return LOCATION;
            } 
            case "webpage" : {
                return WEBPAGE;
            }
            case "batch" : {
                return BATCH;
            }
            case "program" : {
                return PROGRAM;
            }
            default : {
                return UNDEFINED_ENTITY;
            }
        }
    }
    
    public abstract String displayName();
    
    public boolean isDefined() {
        return ! this.equals(UNDEFINED_ENTITY);
    }
    
    public boolean isNotDefined() {
        return this.equals(UNDEFINED_ENTITY);
    }
}
