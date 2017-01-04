/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import diarsid.beam.core.control.io.commands.ArgumentedCommand;
import diarsid.beam.core.control.io.commands.CommandType;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.concurrent.TimeUnit.valueOf;

import static org.slf4j.LoggerFactory.getLogger;

import static diarsid.beam.core.control.io.commands.CommandType.BATCH_PAUSE;

/**
 *
 * @author Diarsid
 */
public class ExecutorPauseCommand implements ArgumentedCommand {
    
    private static final ExecutorPauseCommand NO_PAUSE;
    static {
        NO_PAUSE = new ExecutorPauseCommand(0, SECONDS);
    }
    
    private final int pauseDuration;
    private final TimeUnit unit;
    
    public ExecutorPauseCommand(int pauseDuration, TimeUnit unit) {
        this.pauseDuration = pauseDuration;
        this.unit = unit;
    }
    
    public static ExecutorPauseCommand parsePauseCommandFrom(String stringifiedPauseCommand) {
        String[] pauseData = stringifiedPauseCommand.trim().split(" ");
        if ( pauseData.length != 2 ) {
            return NO_PAUSE;
        } 
        try {
            return new ExecutorPauseCommand(
                    parseInt(pauseData[0]), 
                    valueOf(pauseData[1]));
        } catch (Exception e) {
            getLogger(ExecutorPauseCommand.class).error(
                    format("String '%s' is not proper pause format.", stringifiedPauseCommand));
            return NO_PAUSE;
        }
    }
    
    private String stringifyPause() {
        return format("%d %s", this.pauseDuration, this.unit.name());
    }

    @Override
    public String stringifyOriginal() {
        return this.stringifyPause();
    }

    @Override
    public String stringifyExtended() {
        return this.stringifyPause();
    }

    @Override
    public CommandType type() {
        return BATCH_PAUSE;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + this.pauseDuration;
        hash = 11 * hash + Objects.hashCode(this.unit);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final ExecutorPauseCommand other = ( ExecutorPauseCommand ) obj;
        if ( this.pauseDuration != other.pauseDuration ) {
            return false;
        }
        if ( this.unit != other.unit ) {
            return false;
        }
        return true;
    }
}
