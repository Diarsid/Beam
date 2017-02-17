/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

import java.util.HashSet;
import java.util.Set;

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
    
    public void merge(AllowedExecutionTime other) {
        this.hours.addAll(other.hours);
        this.days.addAll(other.days);
    }
       
    public boolean includeHourOfDay(int givenHour) {
        if ( this.verifyHours(givenHour) ) {
            this.hours.add(givenHour);
            return true;
        } else { 
            return false;
        }
    }
    
    public boolean includeHoursOfDayBetween(int fromHourInclusive, int toHourExclusive) {
        if ( this.verifyHours(fromHourInclusive, toHourExclusive) ) {
            if ( fromHourInclusive == toHourExclusive ) {
                return true;
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
            return true;
        } else {
            return false;
        }
    }
    
    public boolean includeDayOfWeek(int givenDay) {
        if ( this.verifyDays(givenDay) ) {
            this.days.add(givenDay);
            return true;
        } else { 
            return false;
        }
    }
    
    public boolean includeDaysOfWeekBetween(int fromDayInclusive, int toDayInclusive) {
        if ( this.verifyDays(fromDayInclusive, toDayInclusive) ) {
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
            return true;
        } else {
            return false;
        }
    }
    
    private boolean verifyHours(Integer... givenHours) {
        for (Integer hourNumber : givenHours) {
            if (hourNumber < 0 || hourNumber > 24 ) {
                return false;
            }
        }
        return true;
    }
    
    private boolean verifyDays(Integer... givenDays) {
        for (Integer dayNumber : givenDays) {
            if (dayNumber < 1 || dayNumber > 7 ) {
                return false;
            }
        }
        return true;
    }
}
