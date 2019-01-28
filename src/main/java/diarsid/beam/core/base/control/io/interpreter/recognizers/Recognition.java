/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.interpreter.recognizers;

import diarsid.beam.core.base.control.io.commands.CommandType;

import static diarsid.beam.core.application.environment.BeamEnvironment.similarity;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.ArgsExpectation.EXPECTS_MORE_ARGS;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.ArgsExpectation.NOT_EXPECTS_MORE_ARGS;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.EmptyArgsTolerance.NOT_TOLERATE_EMPTY_ARGS;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.EmptyArgsTolerance.TOLERATE_EMPTY_ARGS;

/**
 *
 * @author Diarsid
 */
public class Recognition {
    
    private static final ControlWordsContext CONTEXT;
    
    static {
        CONTEXT = new ControlWordsContext();
    }
    
    private Recognition() {        
    }
    
    public static ControlWordsContext controlWordsContext() {
        return CONTEXT;
    }
    
    public static String independentWord(String word) {
        CONTEXT.add(word);
        CONTEXT.addToIndependent(word);
        return word;
    }
    
    public static String[] independentWords(String... words) {
        CONTEXT.add(words);
        CONTEXT.addToIndependent(words);
        return words;
    }
    
    public static InputCorrectnessRecognizer correctInput() {
        return new InputCorrectnessRecognizer();
    }
    
    public static OneArgRecognizer singleArg() {
        return new OneArgRecognizer();
    }
    
    public static MultipleArgsRecognizer multipleArgs() {
        return new MultipleArgsRecognizer();
    }
    
    public static PauseRecognizer pause() {
        return new PauseRecognizer();
    }
    
    public static EmptyCommandRecognizer only(CommandType commandType) {
        return new EmptyCommandRecognizer(commandType);
    }
    
    public static PrefixesRecognizer prefixes(String... controlPrefixes) {
        return new PrefixesRecognizer(controlPrefixes);
    }
    
    public static PluginsRecognizer plugins() {
        return new PluginsRecognizer();
    }
    
    public static RelativePathRecognizer relativePath() {
        return new RelativePathRecognizer();
    }
    
    public static DomainWordRecognizer domainWord() {
        return new DomainWordRecognizer();
    }
    
    public static WordRecognizer mediatoryControlWord(String controlWord) {
        CONTEXT.add(controlWord);
        return new WordRecognizer(
                NOT_TOLERATE_EMPTY_ARGS, EXPECTS_MORE_ARGS, controlWord, similarity());
    }
    
    public static WordRecognizer controlWord(String controlWord) {        
        CONTEXT.add(controlWord);
        return new WordRecognizer(
                TOLERATE_EMPTY_ARGS, EXPECTS_MORE_ARGS, controlWord, similarity());
    }
    
    public static WordRecognizer justControlWord(String controlWord) {        
        CONTEXT.add(controlWord);
        return new WordRecognizer(
                NOT_TOLERATE_EMPTY_ARGS, NOT_EXPECTS_MORE_ARGS, controlWord, similarity());
    }
    
    public static WordsRecognizer mediatoryControlWords(String... controlWords) {
        CONTEXT.add(controlWords);
        return new WordsRecognizer(
                NOT_TOLERATE_EMPTY_ARGS, EXPECTS_MORE_ARGS, similarity(), controlWords);
    }
    
    public static WordsRecognizer controlWords(String... controlWords) {
        CONTEXT.add(controlWords);
        return new WordsRecognizer(
                TOLERATE_EMPTY_ARGS, EXPECTS_MORE_ARGS, similarity(), controlWords);
    }
    
    public static WordsRecognizer justControlWords(String... controlWords) {
        CONTEXT.add(controlWords);
        return new WordsRecognizer(
                NOT_TOLERATE_EMPTY_ARGS, NOT_EXPECTS_MORE_ARGS, similarity(), controlWords);
    }
    
    public static ArgumentsRecognizer argumentsFor(CommandType commandType) {
        return new ArgumentsRecognizer(commandType);
    }
    
    public static ExecutorRecognizer executable(CommandType commandType) {
        return new ExecutorRecognizer(commandType);
    }
    
    public static ExecutorRecognizer executableWith(CommandType commandType, String additional) {
        return new ExecutorRecognizer(commandType, additional);
    }
}
