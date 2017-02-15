/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.time;

import java.time.LocalDateTime;

/**
 *
 * @author Diarsid
 */
public interface TimePatternParser extends TimePatternDetector {
    
    LocalDateTime parse(String timePattern);
}
