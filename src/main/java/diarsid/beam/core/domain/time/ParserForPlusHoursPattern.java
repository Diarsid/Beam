/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.time;

import java.time.LocalDateTime;

import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.now;


public class ParserForPlusHoursPattern implements TimePatternParser {
    
    public ParserForPlusHoursPattern() {
    }
    
    private int parseHours(String timePattern) {
        return parseInt(timePattern.trim().replace("h", "").substring(1));
    }

    @Override
    public LocalDateTime parse(String timePattern) {
        return now().plusHours(this.parseHours(timePattern)).withSecond(0).withNano(0);
    }

    @Override
    public boolean isApplicableTo(String timePattern) {
        if ( timePattern.trim().matches("\\+\\d{1,2}h") ) {
            int hours = this.parseHours(timePattern);
            return ( hours > 0 && hours < 100 );
        } else {
            return false;
        }        
    }
}
