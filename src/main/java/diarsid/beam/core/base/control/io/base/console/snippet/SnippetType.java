/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console.snippet;

import static java.util.Arrays.stream;

import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByContaining;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByStartingContainingEndingWith;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByStartingEndingWith;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByStartingWith;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.matchesByStartingWithDigitAndColon;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetMatching.noMatching;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.noRefining;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningBeRemoveAllBefore;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningBeRemoveAllBeforeAndAfter;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningBeRemoveStartingDigitsAndColon;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningByRemoveStart;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetRefining.refiningByRemoveStartAndEndIfPresent;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.Reinvokability.NON_REINVOKABLE;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.Reinvokability.REINVOKABLE;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.TraverseMode.NO_TRAVERSE;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.TraverseMode.TRAVERSE_TO_ROOT_DIRECTLY;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.TraverseMode.TRAVERSE_TO_ROOT_HIERARCHICALLY;
import static diarsid.beam.core.base.control.io.base.console.ConsoleSigns.SIGN_OF_TOO_LONG;

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
            "call"),
    LISTED_COMMAND (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByContaining(" -> "), 
            refiningBeRemoveAllBefore(" -> "),
            "call"),
    
    OPENING (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("> ...opening "),
            refiningByRemoveStart("> ...opening "),
            "open"),
    RUNNING (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("> ...running "),
            refiningByRemoveStart("> ...running "),
            "run"),
    EXECUTING (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("> ...executing "),
            refiningByRemoveStart("> ...executing "),
            "execute"),
    BROWSING (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("> ...browsing "),
            refiningByRemoveStart("> ...browsing "),
            "browse"),
    
    LISTED_FILE (
            REINVOKABLE, 
            TRAVERSE_TO_ROOT_HIERARCHICALLY, 
            matchesByStartingWith("-  "),
            refiningByRemoveStartAndEndIfPresent("-  ", SIGN_OF_TOO_LONG),
            "open"),
    LISTED_FOLDER (
            REINVOKABLE, 
            TRAVERSE_TO_ROOT_HIERARCHICALLY, 
            matchesByStartingWith("[_] "),
            refiningByRemoveStartAndEndIfPresent("[_] ", SIGN_OF_TOO_LONG),
            "open"),
    SINGLE_VARIANT (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingEndingWith("> ", " ?")
                    .andNotContaining(" is one of ", "are you sure"),
            refiningBeRemoveAllBeforeAndAfter("> ", " ?"),
            "open"), 
    NUMBERED_VARIANT (
            REINVOKABLE, 
            TRAVERSE_TO_ROOT_DIRECTLY, 
            matchesByStartingWithDigitAndColon(),
            refiningBeRemoveStartingDigitsAndColon(),
            "open"),
    ARGUMENT_CLARIFY (
            REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingContainingEndingWith("> ", " is ", " ?")
                    .andNotContaining(" is one of ", "are you sure"),
            refiningBeRemoveAllBeforeAndAfter(" is ", " ?"),
            "call"),
    
    TARGET_NOT_FOUND_IN (
            REINVOKABLE,
            NO_TRAVERSE,
            matchesByContaining(" not found in "),
            refiningBeRemoveAllBefore(" not found in "),
            ""),
    
    QUESTION_FOR_YES_OR_NO (
            NON_REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("> yes/no :"),
            noRefining()),
    QUESTION_FOR_VARIANTS (
            NON_REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("> is one of ?"),
            noRefining()),
    QUESTION_FOR_VARIANTS_CHOICE (
            NON_REINVOKABLE, 
            NO_TRAVERSE, 
            matchesByStartingWith("> choose :"),
            noRefining()),
    QUESTION_FOR_SURE(
            NON_REINVOKABLE,
            NO_TRAVERSE,
            matchesByContaining("are you sure"),
            noRefining()),
    UNKNOWN (
            NON_REINVOKABLE, 
            NO_TRAVERSE, 
            noMatching(),
            noRefining());
    
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
    private final String reinvokationMark;
    
    private SnippetType(
            Reinvokability reinvokability, 
            TraverseMode traverseMode,
            SnippetMatching matching,
            SnippetRefining refining,
            String reinvokationMark) {
        this.reinvokability = reinvokability;
        this.traverseMode = traverseMode;
        this.matching = matching;
        this.refining = refining;
        this.reinvokationMark = reinvokationMark;
    }
    
    private SnippetType(
            Reinvokability reinvokability, 
            TraverseMode traverseMode,
            SnippetMatching matching,
            SnippetRefining refining) {
        this.reinvokability = reinvokability;
        this.traverseMode = traverseMode;
        this.matching = matching;
        this.refining = refining;
        this.reinvokationMark = "";
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
    
    public String reinvokationMark() {
        return this.reinvokationMark;
    }
    
    public String lineToSnippet(String line) {
        return this.refining.refineFromLine(line);
    }
    
    public static SnippetType defineSnippetTypeOf(String line) {        
        return stream(values())
                .filter(type -> type.matching.matches(line))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
