/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter.recognizers;

import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.interpreter.Input;
import diarsid.beam.core.control.interpreter.NodeRecognizer;

import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.control.commands.EmptyCommand.undefinedCommand;
import static diarsid.beam.core.util.StringUtils.lower;


public class PrefixRecognizer extends NodeRecognizer {
    
    private final Set<String> controlPrefixes;
    
    public PrefixRecognizer(String... controlPrefixes) {
        this.controlPrefixes = unmodifiableSet(stream(controlPrefixes)
                .map(prefix -> lower(prefix).trim())
                .collect(toSet()));
    }

    @Override
    public Command assess(Input input) {
        if ( input.hasNotRecognizedArgs() ) {
            Optional<String> foundPrefix = this.controlPrefixes
                    .stream()
                    .filter(prefix -> lower(input.currentArg()).startsWith(prefix))
                    .findFirst();
            if ( foundPrefix.isPresent() ) {
                input.resetCurrentArg(input.currentArg().substring(foundPrefix.get().length()));
                return super.delegateRecognitionForward(input);
            } else {
                return undefinedCommand();
            }
        } else {
            return undefinedCommand();
        }
    }
}
