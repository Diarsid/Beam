/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities;

import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public enum NamedEntityType {
    LOCATION,
    WEBPAGE,
    PROGRAM,
    BATCH;
    
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
            default : {
                return null;
            }
        }
    }
}
