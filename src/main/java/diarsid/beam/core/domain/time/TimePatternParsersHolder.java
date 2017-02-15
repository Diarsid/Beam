/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.time;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author Diarsid
 */
public class TimePatternParsersHolder {
    
    private final Set<TimePatternParser> parsers;
    
    TimePatternParsersHolder(Set<TimePatternParser> parsers) {
        this.parsers = parsers;
    }
    
    public Optional<LocalDateTime> parseTimeFrom(String timePattern) {
        return this.parsers
                .stream()
                .filter(parser -> parser.isApplicableTo(timePattern))
                .map(parser -> parser.parse(timePattern))
                .findFirst();
    }
}
