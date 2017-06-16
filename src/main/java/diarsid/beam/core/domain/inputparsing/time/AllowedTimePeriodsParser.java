/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

import diarsid.beam.core.domain.entities.TimePeriod;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;

import static diarsid.beam.core.base.util.StringNumberUtils.isNumeric;
import static diarsid.beam.core.base.util.StringNumberUtils.isNumericRange;
import static diarsid.beam.core.base.util.StringUtils.nonEmpty;
import static diarsid.beam.core.base.util.StringUtils.normalizeSpaces;
import static diarsid.beam.core.domain.entities.TimePeriod.DAYS;
import static diarsid.beam.core.domain.entities.TimePeriod.HOURS;
import static diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriod.emptyTime;

/**
 *
 * @author Diarsid
 */
public class AllowedTimePeriodsParser {
    
    AllowedTimePeriodsParser() {
    }

    private String[] normalizePatternAndSplitByComma(String timePattern) {
        timePattern = normalizeSpaces(timePattern.trim());
        timePattern = timePattern.replaceAll("\\s+-\\s+", "-");
        timePattern = timePattern.replaceAll(",\\s?", ",");
        String[] numbers = timePattern.split(",");
        return numbers;
    }

    private AllowedTimePeriod parseAllowedTimeWithUnit(String timePattern, TimePeriod period) {
        String[] numbers = this.normalizePatternAndSplitByComma(timePattern);
        AllowedTimePeriod time = emptyTime();
        stream(numbers)
                .filter(numberString -> nonEmpty(numberString))
                .filter(numberString -> isNumeric(numberString) || isNumericRange(numberString))
                .forEach(numberString -> {
                    this.parseTimeAndCollectInAllowedTimePeriod(numberString, period, time);
                });
        return time;
    }
    
    private void parseTimeAndCollectInAllowedTimePeriod(
            String numberString, TimePeriod period, AllowedTimePeriod time) {
        if ( isNumeric(numberString) ) {
            if ( period.is(HOURS) ) {
                time.includeHourOfDay(parseInt(numberString));
            } else if ( period.is(DAYS) ) {
                time.includeDayOfWeek(parseInt(numberString));
            }                        
        } else if ( isNumericRange(numberString) ) {
            int dashIndex = numberString.indexOf("-");
            int from = parseInt(numberString.substring(0, dashIndex));
            int to = parseInt(numberString.substring(dashIndex + 1, numberString.length()));
            if ( period.is(HOURS) ) {
                time.includeHoursOfDayBetween(from, to);
            } else if ( period.is(DAYS) ) {
                time.includeDaysOfWeekBetween(from, to);
            }
        }
    }
    
    public AllowedTimePeriod parseAllowedHours(String timePattern) {
        return parseAllowedTimeWithUnit(timePattern, HOURS);
    }
    
    public AllowedTimePeriod parseAllowedDays(String timePattern) {
        return parseAllowedTimeWithUnit(timePattern, DAYS);
    }
}
