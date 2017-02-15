/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.time;

import java.time.LocalDateTime;

import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.now;


public class ParserForPlusHourMinutePattern implements TimePatternParser {
    
    public ParserForPlusHourMinutePattern() {
    }

    @Override
    public LocalDateTime parse(String timePattern) {
        // remove 'h', 'm' and '+';
        timePattern = timePattern.trim().replace("h", "").replace("m", "").substring(1);
        int semicolonIndex = timePattern.indexOf(":");
        int minutes = parseInt(timePattern.substring(semicolonIndex + 1, timePattern.length()));
        int hours = parseInt(timePattern.substring(0, semicolonIndex));
        return now()
                .plusHours(hours)
                .plusMinutes(minutes)
                .withSecond(0)
                .withNano(0);        
    }

    @Override
    public boolean isApplicableTo(String timePattern) {
        if ( timePattern.trim().matches("\\+\\d{1,2}h?:\\d{1,2}m?") ) {
            timePattern = timePattern.trim().replace("h", "").replace("m", "").substring(1);
            int semicolonIndex = timePattern.indexOf(":");
            int minutes = parseInt(timePattern.substring(semicolonIndex + 1, timePattern.length()));
            int hours = parseInt(timePattern.substring(0, semicolonIndex));
            return ( 
                    minutes >= 0 && minutes < 59 && 
                    hours > 0 && hours < 100 );
        } else {
            return false;
        }
    }
}
