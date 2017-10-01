/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 *
 * @author Diarsid
 */
public enum TaskRepeat {
    
    NO_REPEAT (
            "no repeat", 
            "Task will never be repeated again."),
    HOURLY_REPEAT (
            "hourly repeat", 
            "Will be repeated every allowed hour and day.",
            "For example, from Tuesday till Friday from 10:00 to 18:00."),
    DAILY_REPEAT (
            "every day of week", 
            "Will be repeated at the same time every allowed day of week.", 
            "For example, from Monday till Thursday at 19:00."),
    MONTHLY_REPEAT (
            "every month", 
            "Will be repeated every month at the same day of ",
            "month (0-31) and the same time"),
    YEARLY_REPEAT (
            "every year", 
            "Will be repeated every year at the same month, day and time."),
    
    UNDEFINED_REPEAT ("undefined"); 
    
    private final String displayName;
    private final List<String> description;
    
    private TaskRepeat(String displayName, String... description) {
        this.displayName = displayName;
        this.description = asList(description);
    }
    
    public List<String> description() {
        return this.description;
    }
    
    public boolean isDefined() {
        return ! this.equals(UNDEFINED_REPEAT);
    }
    
    public boolean isUndefined() {
        return this.equals(UNDEFINED_REPEAT);
    }
    
    public boolean isOneOf(TaskRepeat... others) {
        return stream(others)
                .anyMatch(other -> this.equals(other));
    }
    
    public boolean isNot(TaskRepeat other) {
        return ! this.equals(other);
    }
    
    public boolean isNot(TaskRepeat... others) {
        return stream(others)
                .noneMatch(other -> this.equals(other));
    }
    
    public static List<String> repeatNames() {
        return stream(values())
                .filter(repeat -> repeat.isDefined())
                .map(repeat -> repeat.displayName)
                .collect(toList());
    }
    
    public static TaskRepeat repeatByItsName(String displayName) {
        return stream(values())
                .filter(repeat -> displayName.equals(repeat.displayName))
                .findFirst()
                .orElseGet(() -> UNDEFINED_REPEAT);
    }
    
    public static List<String> repeatsDescription() {
        List<String> repeatInfo = new ArrayList<>();
        for (TaskRepeat repeat : values()) {
            if ( repeat.isUndefined() ) {
                continue;
            }
            repeatInfo.add(repeat.displayName);
            repeat.description.forEach((description) -> {
                repeatInfo.add("  " + description);
            });
        }
        return repeatInfo;
    }
}
