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

import static diarsid.beam.core.domain.inputparsing.time.TimeType.PLUS_MINUTES;


class ParserForPlusMinutesPattern implements TimePatternParser {
    
    ParserForPlusMinutesPattern() {
    }
    
    @Override
    public List<String> timePatterns() {
        return asList(
                "plus minutes:",
                "  +35m",
                "  +01",
                "  +1");
    }
    
    private int parseMinutes(String timePattern) {
        return parseInt(timePattern.trim().replace("m", "").substring(1));
    }

    @Override
    public Optional<Time> parse(String timePattern) {
        if ( timePattern.trim().matches("\\+\\d{1,2}m?") ) {
            int minutes = this.parseMinutes(timePattern);
            if ( minutes > 0 && minutes < 100 ) {
                LocalDateTime time = now()
                        .plusMinutes(this.parseMinutes(timePattern))
                        .withSecond(0)
                        .withNano(0);
                Time schedulableTime = new Time(time, PLUS_MINUTES);
                return Optional.of(schedulableTime);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}
