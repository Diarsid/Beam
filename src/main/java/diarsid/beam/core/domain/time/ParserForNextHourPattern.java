/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.time;

import java.time.LocalDateTime;

import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.now;


class ParserForNextHourPattern implements TimePatternParser {
    
    ParserForNextHourPattern() {
    }

    private int parseHours(String timePattern) {
        return parseInt(timePattern.trim().replace("h", ""));
    }

    @Override
    public LocalDateTime parse(String timePattern) {
        return now().withHour(this.parseHours(timePattern)).withSecond(0).withNano(0);
    }

    @Override
    public boolean isApplicableTo(String timePattern) {
        if ( timePattern.trim().matches("\\d{1,2}h") ) {
            int hours = this.parseHours(timePattern);
            return ( hours >= 0 && hours < 24 );
        } else {
            return false;
        }        
    }
}
