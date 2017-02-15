/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.time;

import java.time.LocalDateTime;

import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.now;


public class ParserForNextMinutePattern implements TimePatternParser {
    
    public ParserForNextMinutePattern() {
    }

    private int parseMinutes(String timePattern) {
        return parseInt(timePattern.trim().replace("m", ""));
    }

    @Override
    public LocalDateTime parse(String timePattern) {
        return now().withMinute(this.parseMinutes(timePattern)).withSecond(0).withNano(0);
    }

    @Override
    public boolean isApplicableTo(String timePattern) {
        if ( timePattern.trim().matches("\\d{1,2}m?") ) {
            int minutes = this.parseMinutes(timePattern);
            return ( minutes >= 0 && minutes < 60 );
        } else {
            return false;
        }        
    }
}
