/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.util.Objects;

import diarsid.beam.core.control.io.commands.ArgumentedCommand;
import diarsid.beam.core.control.io.commands.CommandType;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import static org.slf4j.LoggerFactory.getLogger;

import static diarsid.beam.core.control.io.commands.CommandType.BATCH_PAUSE;
import static diarsid.beam.core.domain.entities.TimePeriod.SECONDS;

/**
 *
 * @author Diarsid
 */
public class BatchPauseCommand implements ArgumentedCommand {
    
    private static final BatchPauseCommand NO_PAUSE;
    static {
        NO_PAUSE = new BatchPauseCommand(0, SECONDS);
    }
    
    private final int pauseDuration;
    private final TimePeriod getTimePeriod;
    
    public BatchPauseCommand(int pauseDuration, TimePeriod unit) {
        this.pauseDuration = pauseDuration;
        this.getTimePeriod = unit;
    }
    
    public static BatchPauseCommand parsePauseCommandFrom(String stringifiedPauseCommand) {
        String[] pauseData = stringifiedPauseCommand.trim().split(" ");
        if ( pauseData.length != 2 ) {
            return NO_PAUSE;
        } 
        try {
            return new BatchPauseCommand(
                    parseInt(pauseData[0]), 
                    TimePeriod.valueOf(pauseData[1]));
        } catch (Exception e) {
            getLogger(BatchPauseCommand.class).error(
                    format("String '%s' is not proper pause format.", stringifiedPauseCommand));
            return NO_PAUSE;
        }
    }
    
    private String stringifyPause() {
        return format("%d %s", this.pauseDuration, this.getTimePeriod.name());
    }

    @Override
    public String stringifyOriginal() {
        return this.stringifyPause();
    }

    @Override
    public String stringifyExtended() {
        return this.stringifyPause();
    }

    public int getPauseDuration() {
        return this.pauseDuration;
    }

    public TimePeriod getTimePeriod() {
        return this.getTimePeriod;
    }     
    
    public boolean isValid() {
        return this.pauseDuration != 0;
    }

    @Override
    public CommandType type() {
        return BATCH_PAUSE;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + this.pauseDuration;
        hash = 11 * hash + Objects.hashCode(this.getTimePeriod);
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
        final BatchPauseCommand other = ( BatchPauseCommand ) obj;
        if ( this.pauseDuration != other.pauseDuration ) {
            return false;
        }
        if ( this.getTimePeriod != other.getTimePeriod ) {
            return false;
        }
        return true;
    }
}
