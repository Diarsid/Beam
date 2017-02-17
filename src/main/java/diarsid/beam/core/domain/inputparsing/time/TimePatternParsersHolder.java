/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

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
    
    public Optional<TasksTime> parse(String timePattern) {
        Optional<TasksTime> time;
        for (TimePatternParser parser : this.parsers) {
            time = parser.parse(timePattern);
            if ( time.isPresent() ) {
                return time;
            }
        }
        return Optional.empty();
    }
}
