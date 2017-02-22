/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static java.time.LocalDateTime.now;

import static diarsid.beam.core.domain.entities.TaskRepeat.DAILY_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.HOURLY_REPEAT;
import static diarsid.beam.core.domain.entities.Tasks.stringifyTimePeriods;

/**
 *
 * @author Diarsid
 */
class TaskReminder extends Task {
    
    private final Set<Integer> allowedHours;
    private final Set<Integer> allowedDays;
    
    TaskReminder(
            int id, 
            LocalDateTime time, 
            Set<Integer> allowedDays, 
            Set<Integer> allowedHours, 
            TaskRepeat type, 
            List<String> text) {
        super(id, time, true, type, text);
        if ( type.isNot(HOURLY_REPEAT, DAILY_REPEAT) ) {
            throw new IllegalArgumentException(
                    "TaskRepeat type should be HOURLY_REPEAT or DAILY_REPEAT.");
        }
        this.allowedDays = allowedDays;
        this.allowedHours = allowedHours;
    }
    
    @Override
    public String hours() {
        return stringifyTimePeriods(this.allowedHours);
    }
    
    @Override
    public String days() {
        return stringifyTimePeriods(this.allowedDays);
    }
    
    @Override
    public final synchronized void switchTime() {
        if ( super.isFuture() ) {
            // no need to switch.
            return;
        }
        typeSwitch: switch (super.type()) {
            case HOURLY_REPEAT : {
                this.hourlyRepeatTimeSwitchingAlgorithm(); 
                break typeSwitch;
            } 
            case DAILY_REPEAT : {
                this.dailyRepeatTimeSwitchingAlgorithm();
                break typeSwitch;
            }
            default: {
                throw new IllegalStateException(
                        "TaskRepeat type should be HOURLY_REPEAT or DAILY_REPEAT.");
            }
        }
    }

    private void dailyRepeatTimeSwitchingAlgorithm() {
        // if task is daily, it can be performed only at
        // days that have been permitted for this
        // particular task during its creation.
        //
        // If new execution time of this task does not
        // contained in list of permitted days it will be
        // postponed to the next day with and verified again.
        LocalDateTime time = super.time();
        LocalDateTime now = now();
        time = time.plusDays(1);
        if ( time.isBefore(now) ) {
            time = now
                    .withHour(time.getHour())
                    .withMinute(time.getMinute());
            if ( time.isBefore(now) ) {
                time = time.plusDays(1);
            }
        }
        boolean dayIsLegal = this.allowedDays.contains(time.getDayOfWeek().getValue());
        
        while ( ! dayIsLegal ) {
            time = time.plusDays(1);
            dayIsLegal = this.allowedDays.contains(time.getDayOfWeek().getValue());
        }
        super.setTime(time);
    }

    private void hourlyRepeatTimeSwitchingAlgorithm() {
        // if task is hourly, it can be performed only at
        // hours and days that have been permitted for this
        // particular task during its creation.
        //
        // If new execution time of this task does not
        // contained in list of permitted days it will be
        // postponed to the next day with 0 hour and verified again.
        //
        // If new execution time of this task does not
        // contained in list of permitted hours it will be
        // postponed to the next hour and verified again.
        LocalDateTime time = super.time();
        LocalDateTime now = now();
        time = time.plusHours(1);
        if ( time.isBefore(now) ) {
            time = now.withMinute(time.getMinute());
            if ( time.isBefore(LocalDateTime.now()) ) {
                time = time.plusHours(1);
            }
        }
        boolean dayIsLegal = this.allowedDays.contains(time.getDayOfWeek().getValue());
        boolean hourIsLegal = this.allowedHours.contains(time.getHour());
        while ( ( ! dayIsLegal) || ( ! hourIsLegal) ) {
            while ( ! dayIsLegal ) {
                time = time.plusDays(1).withHour(0);
                dayIsLegal = this.allowedDays.contains(time.getDayOfWeek().getValue());
            }
            while ( ! hourIsLegal ) {
                time = time.plusHours(1);
                hourIsLegal = this.allowedHours.contains(time.getHour());
            }
        }
        super.setTime(time);
    }
}
