/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.time;

import java.time.LocalDateTime;

import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.now;
import static java.time.Year.isLeap;


class ParserForNextDaysHoursMinutesPattern implements TimePatternParser {
    
    ParserForNextDaysHoursMinutesPattern() {
    }

    @Override
    public LocalDateTime parse(String timePattern) {
        timePattern = timePattern.replace("d", "").replace("h", "").replace("m", "");
        int spaceIndex = timePattern.indexOf(" ");
        int semicolonIndex = timePattern.indexOf(":");
        int days = parseInt(timePattern.substring(0, spaceIndex));
        int hours = parseInt(timePattern.substring(spaceIndex + 1, semicolonIndex));
        int minutes = parseInt(timePattern.substring(semicolonIndex + 1, timePattern.length()));
        return now()
                .withDayOfMonth(days)
                .withHour(hours)
                .withMinute(minutes)
                .withSecond(0)
                .withNano(0);
    }

    @Override
    public boolean isApplicableTo(String timePattern) {
        if ( timePattern.trim().matches("\\d{1,2}d? \\d{1,2}h?:\\d{1,2}m?") ) {
            timePattern = timePattern.replace("d", "").replace("h", "").replace("m", "");
            int spaceIndex = timePattern.indexOf(" ");
            int semicolonIndex = timePattern.indexOf(":");
            int days = parseInt(timePattern.substring(0, spaceIndex));
            int hours = parseInt(timePattern.substring(spaceIndex + 1, semicolonIndex));
            int minutes = parseInt(timePattern.substring(semicolonIndex + 1, timePattern.length()));
            LocalDateTime now = now();
            int thisMonthLength = now.getMonth().length(isLeap(now.getYear()));
            return ( 
                    days > 0 && days <= thisMonthLength && 
                    hours >= 0 && hours < 24 && 
                    minutes >= 0 && minutes < 60 );
        } else {
            return false;
        }
    }
}
