/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter.recognizers;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.Recognizer;
import diarsid.beam.core.domain.inputparsing.common.ArgumentsInterceptor;
import diarsid.beam.core.domain.entities.BatchPauseCommand;
import diarsid.beam.core.domain.entities.TimePeriod;

import static diarsid.beam.core.base.control.io.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.domain.inputparsing.common.ArgumentType.NUMBER;
import static diarsid.beam.core.domain.inputparsing.common.ArgumentType.TIME_PERIOD;
import static diarsid.beam.core.domain.entities.TimePeriod.parseTimePeriodFrom;
import static diarsid.beam.core.base.util.StringNumberUtils.parseNumberElseZero;

/**
 *
 * @author Diarsid
 */
public class PauseRecognizer implements Recognizer {
    
    public PauseRecognizer() {
    }

    @Override
    public Command assess(Input input) {
        if ( this.hasTwoArguments(input) ) {
            ArgumentsInterceptor args = new ArgumentsInterceptor();
            input.allRemainingArgs()
                    .stream()
                    .filter(arg -> args.interceptArgumentOfType(arg, NUMBER).ifContinue())
                    .filter(arg -> args.interceptArgumentOfType(arg, TIME_PERIOD).ifContinue())
                    .count();
            
            int pauseDuration = parseNumberElseZero(args.argOfType(NUMBER));
            TimePeriod timePeriod = parseTimePeriodFrom(args.argOfType(TIME_PERIOD));
            if ( pauseDuration > 0 && timePeriod.isDefined() ) {
                return new BatchPauseCommand(pauseDuration, timePeriod);
            } else {
                return undefinedCommand();
            }           
        } else {
            return undefinedCommand();
        }
    }

    private boolean hasTwoArguments(Input input) {
        return 
                input.hasNotRecognizedArgs() && 
                input.remainingArgsQty() == 2;
    }
}
