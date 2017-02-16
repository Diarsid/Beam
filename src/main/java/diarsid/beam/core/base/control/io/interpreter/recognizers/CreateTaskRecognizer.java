/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter.recognizers;

import java.util.List;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.creation.CreateTaskCommand;
import diarsid.beam.core.base.control.io.interpreter.Input;
import diarsid.beam.core.base.control.io.interpreter.Recognizer;
import diarsid.beam.core.domain.time.TimePatternDectectorsHolder;

import static java.lang.String.join;

import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.domain.time.TimeParsing.dectectorsHolder;


public class CreateTaskRecognizer implements Recognizer {
    
    private final TimePatternDectectorsHolder timePatternDectectors;
    
    public CreateTaskRecognizer() {
        this.timePatternDectectors = dectectorsHolder();
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            List<String> args = input.allRemainingArgs();
            if ( hasOne(args) ) {
                if ( this.timePatternDectectors.isPatternDetectable(getOne(args)) ) {
                    return new CreateTaskCommand(getOne(args), "");
                } else {
                    return new CreateTaskCommand("", getOne(args));
                }
            } else {
                String oneArgTime = getOne(args);
                String twoArgTime = join(" ", args.get(0), args.get(1));                
                if ( this.timePatternDectectors.isPatternDetectable(twoArgTime) ) {
                    return new CreateTaskCommand(twoArgTime, join(" ", args.subList(2, args.size())));
                } else if ( this.timePatternDectectors.isPatternDetectable(oneArgTime) ) {
                    return new CreateTaskCommand(oneArgTime, join(" ", args.subList(1, args.size())));                    
                } else {
                    return new CreateTaskCommand("", join(" ", args));
                }
            } 
        } else {
            return new CreateTaskCommand("", "");
        }
    }
}
