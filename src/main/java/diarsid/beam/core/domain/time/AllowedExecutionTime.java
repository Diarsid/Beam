/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.time;

import java.util.HashSet;
import java.util.Set;

import diarsid.beam.core.domain.entities.exceptions.TaskTimeInvalidException;

import static java.util.Arrays.stream;

/**
 *
 * @author Diarsid
 */
public class AllowedExecutionTime {
        
    private final Set<Integer> hours;
    private final Set<Integer> days;
            
    AllowedExecutionTime() {
        this.hours = new HashSet<>();
        this.days = new HashSet<>();
    }    
    
    public static AllowedExecutionTime emptyTime() {
        return new AllowedExecutionTime();
    }
    
    public Set<Integer> getHours() {
        return this.hours;
    }
    
    public Set<Integer> getDays() {
        return this.days;
    }
    
    public boolean isEmpty() {
        return ( this.hours.isEmpty() && this.days.isEmpty() );
    }
    
    public boolean hasHours() {
        return ! this.hours.isEmpty();
    }
    
    public boolean hasDays() {
        return ! this.days.isEmpty();
    }
    
    public boolean hasNotHours() {
        return this.hours.isEmpty();
    }
    
    public boolean hasNotDays() {
        return this.days.isEmpty();
    }
    
    public boolean containsDays(int... days) {
        return stream(days).allMatch(day -> this.days.contains(day));
    }
    
    public boolean containsHours(int... hours) {
        return stream(hours).allMatch(hour -> this.hours.contains(hour));
    }
    
    public AllowedExecutionTime merge(AllowedExecutionTime other) {
        this.hours.addAll(other.hours);
        this.days.addAll(other.days);
        return this;
    }
       
    public AllowedExecutionTime includeHourOfDay(int givenHour) {
        this.verifyHours(givenHour);
        this.hours.add(givenHour);
        return this;
    }
    
    public AllowedExecutionTime includeHoursOfDayBetween(int fromHourInclusive, int toHourExclusive) {
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
    
    public AllowedExecutionTime includeDayOfWeek(int givenDay) {
        this.verifyDays(givenDay);
        this.days.add(givenDay);
        return this;
    }
    
    public AllowedExecutionTime includeDaysOfWeekBetween(int fromDayInclusive, int toDayInclusive) {
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
