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

import static diarsid.beam.core.domain.inputparsing.time.TasksTimeType.NEXT_MINUTES;


class ParserForNextMinutePattern implements TimePatternParser {
    
    ParserForNextMinutePattern() {
    }

    private int parseMinutes(String timePattern) {
        return parseInt(timePattern.trim().replace("m", ""));
    }

    @Override
    public Optional<TasksTime> parse(String timePattern) {
        if ( timePattern.trim().matches("\\d{1,2}m?") ) {
            int minutes = this.parseMinutes(timePattern);
            if ( minutes >= 0 && minutes < 60 ) {
                LocalDateTime time = now()
                        .withMinute(minutes)
                        .withSecond(0)
                        .withNano(0);
                return Optional.of(new TasksTime(time, NEXT_MINUTES));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        } 
    }
}
