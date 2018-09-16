/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;



import diarsid.support.configuration.Configuration;

import static java.util.Arrays.stream;

import static diarsid.beam.core.base.util.CollectionsUtils.last;

/**
 *
 * @author Diarsid
 */
enum DataAccessVersion {
    
    V1 (1), 
    V2 (2);
    
    private int number;
    
    private DataAccessVersion(int version) {
        this.number = version;
    }
    
    static DataAccessVersion getDataAccessVersion(Configuration configuration) {
        int configuredVersionNumber = configuration.asInt("data.access.version");
        return stream(values())
                .filter(version -> version.number == configuredVersionNumber)
                .findFirst()
                .orElse(last(values()));
    }
    
    int versionNumber() {
        return this.number;
    }
}
