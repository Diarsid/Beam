/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.time;

import java.util.Set;

/**
 *
 * @author Diarsid
 */
public class TimePatternDectectorsHolder {
    
    private final Set<? extends TimePatternDetector> detectors;
    
    TimePatternDectectorsHolder(Set<? extends TimePatternDetector> detectors) {
        this.detectors = detectors;
    }
    
    public boolean isPatternDetectable(String timePattern) {
        return this.detectors
                .stream()
                .anyMatch(detector -> detector.isApplicableTo(timePattern));
    }
}
