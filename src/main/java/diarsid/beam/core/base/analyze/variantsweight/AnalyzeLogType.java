/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import static diarsid.beam.core.application.environment.BeamEnvironment.configuration;

/**
 *
 * @author Diarsid
 */
enum AnalyzeLogType {
    
    BASE (
            configuration().asBoolean("analyze.weight.base.log")),
    POSITIONS_SEARCH (
            BASE.isEnabled && configuration().asBoolean("analyze.weight.positions.search.log")),
    POSITIONS_CLUSTERS (
            BASE.isEnabled && configuration().asBoolean("analyze.weight.positions.clusters.log"));
    
    private final boolean isEnabled;
    
    private AnalyzeLogType(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    
    boolean isEnabled() {
        return this.isEnabled;
    }
    
    boolean isDisabled() {
        return ! this.isEnabled;
    }
}
