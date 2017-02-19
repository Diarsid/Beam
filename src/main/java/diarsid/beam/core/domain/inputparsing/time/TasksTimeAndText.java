/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

import java.util.Optional;

import diarsid.beam.core.base.util.Pair;

import static java.util.Objects.nonNull;

/**
 *
 * @author Diarsid
 */
public class TasksTimeAndText extends Pair<TaskTime, String> {
    
    
    TasksTimeAndText(TaskTime time, String text) {
        super(time, text);
    }
    
    public TasksTimeAndText(String text) {
        super(null, text);
    }
    
    public TasksTimeAndText(TaskTime time) {
        super(time, "");
    }
    
    public boolean hasTime() {
        return nonNull(super.first());
    }
    
    public boolean hasText() {
        return ! super.second().isEmpty();
    }
    
    public Optional<TaskTime> getTime() {
        return Optional.ofNullable(super.first());
    }
    
    public String getText() {
        return super.second();
    }
}
