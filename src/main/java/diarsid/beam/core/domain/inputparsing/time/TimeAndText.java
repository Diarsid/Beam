/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

import java.util.Optional;

import diarsid.support.objects.Pair;

import static java.util.Objects.nonNull;

/**
 *
 * @author Diarsid
 */
public class TimeAndText extends Pair<Time, String> {    
    
    TimeAndText(Time time, String text) {
        super(time, text);
    }
    
    public TimeAndText(String text) {
        super(null, text);
    }
    
    public TimeAndText(Time time) {
        super(time, "");
    }
    
    public boolean hasTime() {
        return nonNull(super.first());
    }
    
    public boolean hasText() {
        return ! super.second().isEmpty();
    }
    
    public Optional<Time> getTime() {
        return Optional.ofNullable(super.first());
    }
    
    public String getText() {
        return super.second();
    }
}
