/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;

import static diarsid.beam.core.domain.inputparsing.time.TimeType.NEXT_HOURS;


class ParserForNextHourPattern implements TimePatternParser {
    
    ParserForNextHourPattern() {
    }
    
    @Override
    public List<String> timePatterns() {
        return asList(
                "current or next day hour with current minutes:",
                "  06h",
                "  22h",
                "  7h");
    }

    private int parseHours(String timePattern) {
        return parseInt(timePattern.trim().replace("h", ""));
    }

    @Override
    public Optional<Time> parse(String timePattern) {
        if ( timePattern.trim().matches("\\d{1,2}h") ) {
            int hours = this.parseHours(timePattern);
            if ( hours >= 0 && hours < 24 ) {
                LocalDateTime time = now()
                        .withHour(hours)
                        .withSecond(0)
                        .withNano(0);
                return Optional.of(new Time(time, NEXT_HOURS));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }            
    }
}
