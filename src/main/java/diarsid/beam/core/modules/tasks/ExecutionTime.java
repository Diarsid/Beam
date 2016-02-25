/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.tasks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import diarsid.beam.core.modules.tasks.exceptions.TaskTimeInvalidException;

/**
 *
 * @author Diarsid
 */
public class ExecutionTime {
        
    private final Set<Integer> hours;
    private final Set<Integer> days;
            
    public ExecutionTime() {
        this.hours = new HashSet<>();
        this.days = new HashSet<>();
    }    
       
    public ExecutionTime includeHourOfDay(int givenHour) {
        this.verifyHours(givenHour);
        this.hours.add(givenHour);
        return this;
    }
    
    /*
     * 
     */
    public ExecutionTime includeHoursOfDayBetween(int fromHourInclusive, int toHourExclusive) {
        this.verifyHours(fromHourInclusive, toHourExclusive);
        if ( fromHourInclusive == toHourExclusive ) {
            return this;
        } else if ( fromHourInclusive < toHourExclusive ) {
            while ( fromHourInclusive < toHourExclusive ) {
                this.hours.add(fromHourInclusive);
                fromHourInclusive++;
            }
        } else if ( fromHourInclusive > toHourExclusive ) {
            boolean midnightPassed = false;
            while( (fromHourInclusive >= toHourExclusive) ^ midnightPassed ) {
                if ( fromHourInclusive >= 24 ) { 
                    fromHourInclusive = 0;                    
                    midnightPassed = true;
                    if ( fromHourInclusive == toHourExclusive ) {                        
                        continue;
                    }
                }
                this.hours.add(fromHourInclusive);
                fromHourInclusive++;
            }
        }
        return this;
    }
    
    public ExecutionTime includeDayOfWeek(int givenDay) {
        this.verifyDays(givenDay);
        this.days.add(givenDay);
        return this;
    }
    
    public ExecutionTime includeDaysOfWeekBetween(int fromDayInclusive, int toDayInclusive) {
        this.verifyDays(fromDayInclusive, toDayInclusive);
        if ( fromDayInclusive == toDayInclusive ) {
            this.days.add(fromDayInclusive);
        } else if ( fromDayInclusive < toDayInclusive ) {
            while ( fromDayInclusive <= toDayInclusive ) {
                this.days.add(fromDayInclusive);
                fromDayInclusive++;
            }
        } else if ( fromDayInclusive > toDayInclusive ) {
            boolean endOfWeekPassed = false;
            while( (fromDayInclusive > toDayInclusive) ^ endOfWeekPassed ) {
                this.days.add(fromDayInclusive);
                fromDayInclusive++;
                if ( fromDayInclusive == 8 ) { 
                    fromDayInclusive = 1;
                    endOfWeekPassed = true;
                }
            }
        }
        return this;
    }
    
    public Set<Integer> aggregateHours() {
        return this.hours;
    }
    
    public Set<Integer> aggregateDays() {
        return this.days;
    }
    
    private void verifyHours(Integer... givenHours) {
        for (Integer hourNumber : givenHours) {
            if (hourNumber < 0 || hourNumber > 24 ) {
                throw new TaskTimeInvalidException(
                        "Hour cannot be less than 0 or more than 23.");
            }
        }
    }
    
    private void verifyDays(Integer... givenDays) {
        for (Integer dayNumber : givenDays) {
            if (dayNumber < 1 || dayNumber > 7 ) {
                throw new TaskTimeInvalidException(
                        "Day cannot be less than 1 or more than 7.");
            }
        }
    }
}
