/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.util.Objects;

import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.ExtendableCommand;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import static org.slf4j.LoggerFactory.getLogger;

import static diarsid.beam.core.base.control.io.commands.CommandType.BATCH_PAUSE;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.domain.entities.TimePeriod.SECONDS;

/**
 *
 * @author Diarsid
 */
public class BatchPauseCommand implements ExtendableCommand {
    
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

    @Override
    public String stringify() {
        return this.stringifyPause();
    }

    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant("pause " + this.stringifyPause(), variantIndex);
    }
    
    private String stringifyPause() {
        return format("%d %s", this.pauseDuration, lower(this.getTimePeriod.name()));
    }

    @Override
    public String originalArgument() {
        return this.stringifyPause();
    }

    @Override
    public String extendedArgument() {
        return this.stringifyPause();
    }

    public int duration() {
        return this.pauseDuration;
    }

    public TimePeriod timePeriod() {
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

    @Override
    public void setNew() {
        // do nothing. batch command is never regarded as 'new' as it is system command.
    }

    @Override
    public void setStored() {
        // do nothing. batch command is never regarded as 'new' as it is system command.
    }

    @Override
    public boolean wasNotUsedBefore() {
        return false;
    }

    @Override
    public boolean wasUsedBeforeAndStored() {
        return true;
    }

    @Override
    public void setTargetFound() {
        // stub, do nothing.
    }

    @Override
    public void setTargetNotFound() {
        // stub, do nothing.
    }

    @Override
    public boolean isTargetFound() {
        return true;
    }

    @Override
    public boolean isTargetNotFound() {
        return false;
    }
}
