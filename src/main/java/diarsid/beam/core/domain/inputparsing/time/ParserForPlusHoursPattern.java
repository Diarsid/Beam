/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.now;

import static diarsid.beam.core.domain.inputparsing.time.TasksTimeType.PLUS_HOURS;


class ParserForPlusHoursPattern implements TimePatternParser {
    
    ParserForPlusHoursPattern() {
    }
    
    private int parseHours(String timePattern) {
        return parseInt(timePattern.trim().replace("h", "").substring(1));
    }

    @Override
    public Optional<TaskTime> parse(String timePattern) {
        if ( timePattern.trim().matches("\\+\\d{1,2}h") ) {
            int hours = this.parseHours(timePattern);
            if ( hours > 0 && hours < 100 ) {
                LocalDateTime time = now().plusHours(this.parseHours(timePattern)).withSecond(0).withNano(0);
                return Optional.of(new TaskTime(time, PLUS_HOURS));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}
