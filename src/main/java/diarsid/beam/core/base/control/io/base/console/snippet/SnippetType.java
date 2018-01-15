/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console.snippet;

import static java.util.Arrays.stream;

import static diarsid.beam.core.base.control.io.base.console.ConsoleSigns.SIGN_OF_TOO_LONG;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByContaining;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByStartingContainingEndingWith;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByStartingEndingWith;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByStartingWith;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByStartingWithDigitAndContains;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.noMatching;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.notContaining;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.noRefining;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningByRemoveAllBefore;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningByRemoveAllBeforeAndAfter;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningByRemoveStart;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningByRemoveStartAndEndIfPresent;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningByRemoveStartingDigitsAnd;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetReinvocationTextFormat.noFormat;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetReinvocationTextFormat.reinvocationTextFormat;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.Reinvokability.NON_REINVOKABLE;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.Reinvokability.REINVOKABLE;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.TraverseMode.NO_TRAVERSE;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.TraverseMode.TRAVERSE_TO_ROOT_DIRECTLY;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.TraverseMode.TRAVERSE_TO_ROOT_HIERARCHICALLY;

/**
 *
 * @author Diarsid
 */
public enum SnippetType {
    
    COMMAND (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("Beam > "),
            refiningByRemoveStart("Beam > "),
            reinvocationTextFormat("call '%s'")),
    LISTED_COMMAND (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByContaining(" -> "), 
            refiningByRemoveAllBefore(" -> "),
            reinvocationTextFormat("call '%s'")),
    
    OPENING (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("> ...opening "),
            refiningByRemoveStart("> ...opening "),
            reinvocationTextFormat("open %s")),
    RUNNING (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("> ...running "),
            refiningByRemoveStart("> ...running "),
            reinvocationTextFormat("run %s")),
    EXECUTING (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("> ...executing "),
            refiningByRemoveStart("> ...executing "),
            reinvocationTextFormat("execute %s")),
    BROWSING (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("> ...browsing "),
            refiningByRemoveStart("> ...browsing "),
            reinvocationTextFormat("browse %s")),
    
    LISTED_FILE (
            REINVOKABLE, 
            TRAVERSE_TO_ROOT_HIERARCHICALLY, 
            matchesByStartingWith("-  "),
            refiningByRemoveStartAndEndIfPresent("-  ", SIGN_OF_TOO_LONG),
            reinvocationTextFormat("open %s")),
    LISTED_FOLDER (
            REINVOKABLE, 
            TRAVERSE_TO_ROOT_HIERARCHICALLY, 
            matchesByStartingWith("[_] "),
            refiningByRemoveStartAndEndIfPresent("[_] ", SIGN_OF_TOO_LONG),
            reinvocationTextFormat("open %s")),
    LISTED_WEBPAGE (
            REINVOKABLE,
            NO_TRAVERSE,
            matchesByStartingWithDigitAndContains(") "),
            refiningByRemoveStartingDigitsAnd(") "),
            reinvocationTextFormat("browse %s")
            ),
    SINGLE_VARIANT (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingEndingWith("> ", " ?")
                    .and(notContaining(" is one of ", "are you sure")),
            refiningByRemoveAllBeforeAndAfter("> ", " ?"),
            reinvocationTextFormat("open '%s'")), 
    NUMBERED_VARIANT (
            REINVOKABLE, 
            TRAVERSE_TO_ROOT_DIRECTLY, 
            matchesByStartingWithDigitAndContains(" : "),
            refiningByRemoveStartingDigitsAnd(" : "),
            reinvocationTextFormat("open '%s'")),
    ARGUMENT_CLARIFY (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingContainingEndingWith("> ", " is ", " ?")
                    .and(notContaining(" is one of ", "are you sure")),
            refiningByRemoveAllBeforeAndAfter(" is ", " ?"),
            reinvocationTextFormat("call '%s'")),
    
    TARGET_NOT_FOUND_IN (
            REINVOKABLE,
            NO_TRAVERSE,
            matchesByContaining(" not found in "),
            refiningByRemoveAllBefore(" not found in "),
            reinvocationTextFormat("open %s")),
    
    QUESTION_FOR_YES_OR_NO (
            NON_REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("> yes/no :")),
    QUESTION_FOR_VARIANTS (
            NON_REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("> is one of ?")),
    QUESTION_FOR_VARIANTS_CHOICE (
            NON_REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("> choose :")),
    QUESTION_FOR_SURE(
            NON_REINVOKABLE,
            NO_TRAVERSE,
            matchesByContaining("are you sure")),
    UNKNOWN (
            NON_REINVOKABLE, 
            NO_TRAVERSE, 
            noMatching());
    
    static enum Reinvokability {
        REINVOKABLE,
        NON_REINVOKABLE
    }
    
    public static enum TraverseMode {
        NO_TRAVERSE,
        TRAVERSE_TO_ROOT_DIRECTLY,
        TRAVERSE_TO_ROOT_HIERARCHICALLY
    }
    
    private final Reinvokability reinvokability;
    private final TraverseMode traverseMode;
    private final SnippetMatching matching;
    private final SnippetRefining refining;
    private final SnippetReinvocationTextFormat reinvokationTextFormat;
    
    private SnippetType(
            Reinvokability reinvokability, 
            TraverseMode traverseMode,
            SnippetMatching matching,
            SnippetRefining refining,
            SnippetReinvocationTextFormat reinvokationTextFormat) {
        this.reinvokability = reinvokability;
        this.traverseMode = traverseMode;
        this.matching = matching;
        this.refining = refining;
        this.reinvokationTextFormat = reinvokationTextFormat;
    }
    
    private SnippetType(
            Reinvokability reinvokability, 
            TraverseMode traverseMode,
            SnippetMatching matching) {
        this.reinvokability = reinvokability;
        this.traverseMode = traverseMode;
        this.matching = matching;
        this.refining = noRefining();
        this.reinvokationTextFormat = noFormat();
    }
    
    public boolean isReinvokable() {
        return this.reinvokability.equals(REINVOKABLE);
    }
    
    public boolean isNotReinvokable() {
        return this.reinvokability.equals(NON_REINVOKABLE);
    }
    
    public TraverseMode traverseMode() {
        return this.traverseMode;
    }
    
    public SnippetReinvocationTextFormat reinvokationTextFormat() {
        return this.reinvokationTextFormat;
    }
    
    public String lineToSnippet(String line) {
        return this.refining.applyTo(line);
    }
    
    public static SnippetType defineSnippetTypeOf(String line) {        
        return stream(values())
                .filter(type -> type.matching.matches(line))
                .findFirst()
                .orElse(UNKNOWN);
    }
}