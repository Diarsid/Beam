/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console.snippet;


import static java.util.Arrays.stream;

import static diarsid.beam.core.base.control.io.base.console.ConsoleSigns.SIGN_OF_TOO_LONG;
import static diarsid.beam.core.base.control.io.base.console.snippet.MultipleSnippetMatching.matchesAll;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByContaining;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByNotContainingAny;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByNotEndingWith;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByNotStartingWith;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByNotStartingWithAny;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByNotStartingWithDigit;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByStartingContainingEndingWith;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByStartingEndingWith;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByStartingWith;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByStartingWithAny;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByStartingWithDigitAndContains;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.noMatching;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.noRefining;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningByRemoveAllBefore;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningByRemoveAllBeforeAndAfter;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningByRemoveStart;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningByRemoveStartAndEndIfPresent;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningByRemoveStartingDigitsAnd;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningByTrim;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetReinvocationTextFormat.noFormat;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetReinvocationTextFormat.reinvocationTextFormat;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.Reinvokability.NON_REINVOKABLE;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.Reinvokability.REINVOKABLE;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.TraverseMode.NO_TRAVERSE;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.TraverseMode.TRAVERSE_TO_FIRST_NODE;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.TraverseMode.TRAVERSE_TO_PREVIOUS_NODE;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.TraverseMode.TRAVERSE_TO_ROOT_DIRECTLY;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.TraverseMode.TRAVERSE_TO_ROOT_HIERARCHICALLY;
import static diarsid.beam.core.domain.entities.metadata.EntityProperty.allEntityPropertyNames;

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
            reinvocationTextFormat("browse %s")),
    LISTED_ENTITY (
            REINVOKABLE,
            TRAVERSE_TO_FIRST_NODE,
            matchesAll(
                    matchesByNotStartingWithAny("> ", "- ", "[_] ", "Beam > ", "..."),
                    matchesByNotContainingAny(" -> ", " is one of ", "are you sure"),
                    matchesByNotContainingAny(allEntityPropertyNames()),
                    matchesByNotEndingWith(" ?"),
                    matchesByNotStartingWith("WebPanel > "),
                    matchesByNotStartingWithDigit()),
            refiningByTrim(),
            noFormat()),
    PRINTED_ENTITY_PROPERTY (
            REINVOKABLE,
            TRAVERSE_TO_PREVIOUS_NODE,
            matchesByStartingWithAny(allEntityPropertyNames()),
            refiningByTrim(),
            reinvocationTextFormat("open %s")),
    LISTED_WEBPANEL_DIRECTORY (
            NON_REINVOKABLE,
            NO_TRAVERSE,
            matchesByStartingWith("WebPanel > "),
            noRefining(),
            noFormat()),
    
    SINGLE_VARIANT (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesAll(
                    matchesByStartingEndingWith("> ", " ?"),
                    matchesByNotContainingAny(
                            " is one of ", 
                            "are you sure", 
                            " is ", 
                            " open ", 
                            " call ", 
                            " browse ", 
                            " run ",
                            " (Program) ",
                            " (WebPage) ",
                            " (Location) ",
                            " (Batch) "),
                    matchesByNotStartingWith("> exact match ?")),
            refiningByRemoveAllBeforeAndAfter("> ", " ?"),
            reinvocationTextFormat("open '%s'")),     
    SINGLE_VARIANT_OPEN (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesAll(
                    matchesByStartingEndingWith("> open ", " ?"),
                    matchesByNotContainingAny(
                            " is one of ", 
                            "are you sure", 
                            " is ", 
                            " call ", 
                            " browse ", 
                            " run "),
                    matchesByNotStartingWith("> exact match ?")),
            refiningByRemoveAllBeforeAndAfter("> ", " ?"),
            noFormat()),     
    SINGLE_VARIANT_RUN (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesAll(
                    matchesByStartingEndingWith("> run ", " ?"),
                    matchesByNotContainingAny(
                            " is one of ", 
                            "are you sure", 
                            " is ", 
                            " call ", 
                            " browse ", 
                            " open "),
                    matchesByNotStartingWith("> exact match ?")),
            refiningByRemoveAllBeforeAndAfter("> ", " ?"),
            noFormat()),    
    SINGLE_VARIANT_CALL (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesAll(
                    matchesByStartingEndingWith("> call ", " ?"),
                    matchesByNotContainingAny(
                            " is one of ", 
                            "are you sure", 
                            " is ", 
                            " run ", 
                            " browse ", 
                            " open "),
                    matchesByNotStartingWith("> exact match ?")),
            refiningByRemoveAllBeforeAndAfter("> ", " ?"),
            noFormat()),    
    SINGLE_VARIANT_BROWSE (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesAll(
                    matchesByStartingEndingWith("> browse ", " ?"),
                    matchesByNotContainingAny(
                            " is one of ", 
                            "are you sure", 
                            " is ", 
                            " run ", 
                            " call ", 
                            " open "),
                    matchesByNotStartingWith("> exact match ?")),
            refiningByRemoveAllBeforeAndAfter("> ", " ?"),
            noFormat()),    
    NUMBERED_VARIANT (
            REINVOKABLE, 
            TRAVERSE_TO_ROOT_DIRECTLY, 
            matchesByStartingWithDigitAndContains(" : "),
            refiningByRemoveStartingDigitsAnd(" : "),
            reinvocationTextFormat("open '%s'")),
    ARGUMENT_CLARIFY (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesAll(
                    matchesByStartingContainingEndingWith(
                            "> ", 
                            " is ", 
                            " ?"),
                    matchesByNotContainingAny(
                            " is one of ", 
                            "are you sure")),
            refiningByRemoveAllBeforeAndAfter(" is ", " ?"),
            reinvocationTextFormat("call '%s'")),
    
    TARGET_NOT_FOUND_IN (
            REINVOKABLE,
            NO_TRAVERSE,
            matchesByContaining(" not found in "),
            refiningByRemoveAllBefore(" not found in "),
            reinvocationTextFormat("open %s")),
    
    QUESTION_FOR_ENTITY_WEBPAGE (
            REINVOKABLE,
            NO_TRAVERSE,
            matchesByStartingEndingWith("> ", " (WebPage) ?"),
            refiningByRemoveStartAndEndIfPresent("> ", " (WebPage) ?"),
            reinvocationTextFormat("browse %s")),
    QUESTION_FOR_ENTITY_LOCATION (
            REINVOKABLE,
            NO_TRAVERSE,
            matchesByStartingEndingWith("> ", " (Location) ?"),
            refiningByRemoveStartAndEndIfPresent("> ", " (Location) ?"),
            reinvocationTextFormat("open %s")),
    QUESTION_FOR_ENTITY_BATCH (
            REINVOKABLE,
            NO_TRAVERSE,
            matchesByStartingEndingWith("> ", " (Batch) ?"),
            refiningByRemoveStartAndEndIfPresent("> ", " (Batch) ?"),
            reinvocationTextFormat("call %s")),
    QUESTION_FOR_ENTITY_PROGRAM (
            REINVOKABLE,
            NO_TRAVERSE,
            matchesByStartingEndingWith("> ", " (Program) ?"),
            refiningByRemoveStartAndEndIfPresent("> ", " (Program) ?"),
            reinvocationTextFormat("run %s")),
    
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
    
    static enum TraverseMode {
        NO_TRAVERSE,
        TRAVERSE_TO_FIRST_NODE,
        TRAVERSE_TO_PREVIOUS_NODE,
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
    
    TraverseMode traverseMode() {
        return this.traverseMode;
    }
    
    SnippetReinvocationTextFormat reinvokationTextFormat() {
        return this.reinvokationTextFormat;
    }
    
    String lineToSnippet(String line) {
        return this.refining.applyTo(line);
    }
    
    static SnippetType defineSnippetTypeOf(String line) {        
        return stream(values())
                .filter(type -> type.matching.matches(line))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
