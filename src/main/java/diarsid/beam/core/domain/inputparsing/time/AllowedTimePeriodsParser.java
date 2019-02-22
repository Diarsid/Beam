/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

import java.util.List;

import diarsid.beam.core.domain.entities.TimePeriod;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

import static diarsid.beam.core.base.util.StringNumberUtils.isNumeric;
import static diarsid.beam.core.base.util.StringNumberUtils.isNumericRange;
import static diarsid.support.strings.StringUtils.nonEmpty;
import static diarsid.support.strings.StringUtils.normalizeSpaces;
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
    
    public List<String> allowedHoursFormats() {
        return asList(
                "Specify hours when task can be repeated by numbers as comma",
                "separated values e.g. 7, 13, 15, etc. or ranges e.g 7 - 16,",
                "19 - 22, etc. They can also be mixed together e.g. 12, ",
                "16 - 19, 22.",
                "24 can be used to denote the hour between 23 and 00. For",
                "example, 21-24 means a task can be repeated at 21:xx,",
                "22:xx and 23:xx.",
                "Space between numbers can be ommited.");
    }
    
    public List<String> allowedDaysFormats() {
        return asList(
                "Specify days when task can be repeated by numbers (Monday - 1)",
                "as comma separated values e.g. 1, 2, 4, etc. or ranges e.g.",
                "1 - 3, 2 - 6, etc. They can also be mixed together e.g. 1, ",
                "3-5, 7.",
                "Space between numbers can be ommited.");
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
