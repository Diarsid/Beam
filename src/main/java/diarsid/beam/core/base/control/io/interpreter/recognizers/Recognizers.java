/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.interpreter.recognizers;

import diarsid.beam.core.base.control.io.commands.CommandType;

/**
 *
 * @author Diarsid
 */
public class Recognizers {
    
    private Recognizers() {        
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
    
    public static RelativePathRecognizer relativePath() {
        return new RelativePathRecognizer();
    }
    
    public static DomainWordRecognizer domainWord() {
        return new DomainWordRecognizer();
    }
    
    public static WordRecognizer controlWord(String controlWord) {
        return new WordRecognizer(controlWord);
    }
    
    public static WordsRecognizer controlWords(String... controlWords) {
        return new WordsRecognizer(controlWords);
    }
    
    public static ArgumentsRecognizer argumentsFor(CommandType commandType) {
        return new ArgumentsRecognizer(commandType);
    }
}
