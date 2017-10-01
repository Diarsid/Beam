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

import static diarsid.beam.core.domain.inputparsing.time.TimeType.NEXT_MINUTES;


class ParserForNextMinutePattern implements TimePatternParser {
    
    ParserForNextMinutePattern() {
    }
    
    @Override
    public List<String> timePatterns() {
        return asList(
                "current or next hour minutes:",
                "  35",
                "  35m",
                "  7");
    }

    private int parseMinutes(String timePattern) {
        return parseInt(timePattern.trim().replace("m", ""));
    }

    @Override
    public Optional<Time> parse(String timePattern) {
        if ( timePattern.trim().matches("\\d{1,2}m?") ) {
            int minutes = this.parseMinutes(timePattern);
            if ( minutes >= 0 && minutes < 60 ) {
                LocalDateTime time = now()
                        .withMinute(minutes)
                        .withSecond(0)
                        .withNano(0);
                return Optional.of(new Time(time, NEXT_MINUTES));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        } 
    }
}
