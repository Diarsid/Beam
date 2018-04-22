/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console.snippet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;

import static diarsid.beam.core.base.control.io.base.console.ConsoleSigns.SIGN_OF_FILE;
import static diarsid.beam.core.base.control.io.base.console.ConsoleSigns.SIGN_OF_FOLDER;
import static diarsid.beam.core.base.control.io.base.console.ConsoleSigns.SIGN_OF_TOO_LARGE;
import static diarsid.beam.core.base.control.io.base.console.ConsoleSigns.removeFolderSign;
import static diarsid.beam.core.base.control.io.base.console.snippet.ConsoleSnippetFinderState.EMPTY;
import static diarsid.beam.core.base.control.io.base.console.snippet.ConsoleSnippetFinderState.LINE_FOUND;
import static diarsid.beam.core.base.control.io.base.console.snippet.ConsoleSnippetFinderState.LINE_NOT_FOUND;
import static diarsid.beam.core.base.control.io.base.console.snippet.ConsoleSnippetFinderState.LINE_NOT_INFORMATIVE;
import static diarsid.beam.core.base.control.io.base.console.snippet.ConsoleSnippetFinderState.LINE_PROCESSED;
import static diarsid.beam.core.base.control.io.base.console.snippet.ConsoleSnippetFinderState.READY;
import static diarsid.beam.core.base.control.io.base.console.snippet.Snippet.unknownSnippet;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.UNKNOWN;
import static diarsid.beam.core.base.control.io.base.console.snippet.SnippetType.defineSnippetTypeOf;
import static diarsid.beam.core.base.control.io.base.interaction.UserReaction.isYes;
import static diarsid.beam.core.base.util.CollectionsUtils.removeLastFrom;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.extractLocationFromPath;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.indexOfAny;
import static diarsid.beam.core.base.util.StringUtils.nonEmpty;
import static diarsid.beam.core.base.util.StringUtils.splitToLines;
import static diarsid.beam.core.base.util.TextUtil.indexOfFirstNonSpaceIn;
import static diarsid.beam.core.base.util.TextUtil.lineAtCaret;
import static diarsid.beam.core.base.util.PathUtils.joinToPathFrom;
import static diarsid.beam.core.base.util.PathUtils.joinToPathFrom;

/**
 *
 * @author Diarsid
 */
public class ConsoleSnippetFinder {
    
    private final static Map<String, String> ACTIONS_BY_ENTITY_CHAPTER_HEADERS;
    
    static {
        Map<String, String> map  = new HashMap<>();
        map.put("Locations:", "open");
        map.put("Batches:", "call");
        map.put("WebPages:", "browse");
        map.put("Programs:", "run");
        
        ACTIONS_BY_ENTITY_CHAPTER_HEADERS = unmodifiableMap(map);
        
    }
        
    private ConsoleSnippetFinderState state;
    private String text;
    private int caret;
    private String lineAtCaret;
    private SnippetType snippetType;
    private Snippet snippet;

    public ConsoleSnippetFinder() {
    }
    
    private static Optional<String> getActionByHeader(String entityChapterHeader) {
        return ACTIONS_BY_ENTITY_CHAPTER_HEADERS
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().contains(entityChapterHeader))
                .findFirst()
                .map(entry -> entry.getValue());
    }
    
    public ConsoleSnippetFinder in(String text) {
        this.text = text;
        this.state = READY;
        return this;
    }
    
    public ConsoleSnippetFinder goToLineAt(int caretPosition) {
        this.caret = caretPosition;
        this.lineAtCaret = lineAtCaret(this.text, this.caret);
        if ( this.lineAtCaret.isEmpty() ) {
            this.state = LINE_NOT_FOUND;
        } else if ( this.isLineAtCaretNotInformative() ) {
            this.state = LINE_NOT_INFORMATIVE;
        } else {
            this.state = LINE_FOUND;
        }
        
        return this;
    }
    
    private boolean isLineAtCaretNotInformative() {
        String trimmedLine = this.lineAtCaret.trim();
        
        return trimmedLine.isEmpty() || trimmedLine.equalsIgnoreCase("Beam >");
    }
    
    public ConsoleSnippetFinder defineLineSnippetType() {
        if ( this.state.equals(LINE_NOT_FOUND) || this.state.equals(LINE_NOT_INFORMATIVE) ) {
            this.snippetType = UNKNOWN;
        } else {
            this.snippetType = defineSnippetTypeOf(this.lineAtCaret);
        }
        this.state = LINE_PROCESSED;
        return this;
    }
    
    public ConsoleSnippetFinder composeSnippet() {
        if ( this.snippetType.isNotReinvokable() ) {
            this.composeSnippetSimply();
            return this;
        }
        
        switch ( this.snippetType.traverseMode() ) {
            case NO_TRAVERSE : {
                this.composeSnippetWithoutTraversing();
                break;
            }    
            case TRAVERSE_TO_FIRST_NODE : {
                this.composeSnippetWithTraversingToFirstNode();
                break;
            }
            case TRAVERSE_TO_ROOT_DIRECTLY : {
                this.composeSnippetWithTraversingDirectly();
                break;
            }    
            case TRAVERSE_TO_ROOT_HIERARCHICALLY : {
                this.composeSnippetWithTraversingHierarchically();
                break;
            }    
            default : {
                this.snippet = unknownSnippet();
            }         
        }
        return this;
    }
    
    private void composeSnippetWithoutTraversing() {
        this.composeSnippetSimply();
    }

    private void composeSnippetSimply() {
        this.snippet = new Snippet(
                this.snippetType, this.snippetType.lineToSnippet(this.lineAtCaret));
    }
    
    private void composeSnippetWithTraversingToFirstNode() {
        int commandStart = this.text.lastIndexOf("Beam > ", this.caret) + "Beam > ".length();
        int commandEnd = this.text.indexOf('\n', commandStart);
        
        String snippetLine = this.snippetType.lineToSnippet(this.lineAtCaret); 
        
        String commandSpan = this.text.substring(commandEnd, this.caret);
        String entityChapterHeader = "";
        int initialIndentLevel = indexOfFirstNonSpaceIn(this.lineAtCaret);
        ListIterator<String> linesIterator = this.linesIteratorFor(commandSpan);
        
        String line;
        int lineIndentLevel;
        
        listedEntitiesWalking: while ( linesIterator.hasPrevious() ) {            
            line = linesIterator.previous();
            
            lineIndentLevel = indexOfFirstNonSpaceIn(line);
            if ( lineIndentLevel == -1 ) {
                this.snippetType = UNKNOWN;
                break listedEntitiesWalking;
            } 
            
            if ( lineIndentLevel < initialIndentLevel ) {
                entityChapterHeader = line.trim();
                break listedEntitiesWalking;
            }
        }    
        
        if ( nonEmpty(entityChapterHeader) ) {
            Optional<String> action = getActionByHeader(entityChapterHeader);
            if ( action.isPresent() ) {
                snippetLine = format("%s %s", action.get(), snippetLine);
            }
        }
        
        this.snippet = new Snippet(this.snippetType, snippetLine);
    }
    
    private void composeSnippetWithTraversingDirectly() {
        int commandStart = this.text.lastIndexOf("Beam > ", this.caret) + "Beam > ".length();
        int commandEnd = this.text.indexOf('\n', commandStart);
        
        String snippetLine = this.snippetType.lineToSnippet(this.lineAtCaret);
        
        String subpath = this.leadingSubpathIfPresentInInitialCommand(commandStart, commandEnd);
        if ( nonEmpty(subpath) ) {
            snippetLine = joinToPathFrom(subpath, snippetLine);
        }
        
        this.snippet = new Snippet(this.snippetType, snippetLine);
    }
    
    private String leadingSubpathIfPresentInInitialCommand(
            int commandStart, int commandEnd) {
        String lastCommand = this.text.substring(commandStart, commandEnd);
        if ( containsPathSeparator(lastCommand) ) {
            String location = extractLocationFromPath(lastCommand);
            String subpathQuestion = format("'%s' is ", location);
            String commandSpan = this.text.substring(commandEnd, this.caret);
            
            if ( containsIgnoreCase(commandSpan, subpathQuestion) ) {
                int supbathBegin = commandSpan.indexOf(subpathQuestion) + subpathQuestion.length();
                int subpathEnds = commandSpan.indexOf(" ?\n", supbathBegin);
                String subpath = commandSpan.substring(supbathBegin, subpathEnds).trim();

                int yesOrNoBegin = commandSpan.indexOf("> yes/no : ", subpathEnds) + 
                        "> yes/no : ".length();
                int yesOrNoEnd = commandSpan.indexOf('\n', yesOrNoBegin);
                String yesOrNo = commandSpan.substring(yesOrNoBegin, yesOrNoEnd);

                if ( isYes(yesOrNo) ) {
                    location = subpath;
                }
            }     
            return location;
        } else {
            return "";
        }
    }
    
    private void composeSnippetWithTraversingHierarchically() {
        switch ( this.snippetType ) {
            case LISTED_FILE : {
                this.composeSnippetByDetecting(SIGN_OF_FILE);
                break;
            }
            case LISTED_FOLDER : {
                this.composeSnippetByDetecting(SIGN_OF_FOLDER);
                break;
            }
            default : {
                this.composeSnippetSimply();
            }
        }
    }
    
    private void composeSnippetByDetecting(String sign) {
        int initialIndentLevel = this.lineAtCaret.indexOf(sign);
        String snippetLine = this.combineSnippetLineFromTreeBy(initialIndentLevel);
        
        this.snippet = new Snippet(this.snippetType, snippetLine);
    }
    
    private ListIterator<String> linesIteratorFor(String commandSpan) {
        List<String> lines = splitToLines(commandSpan);
        removeLastFrom(lines);
        return lines.listIterator(lines.size());
    }
    
    private String combineSnippetLineFromTreeBy(int initialIndentLevel) {
        int commandStart = this.text.lastIndexOf("Beam > ", this.caret) + "Beam > ".length();
        int commandEnd = this.text.indexOf('\n', commandStart);
        
        String snippetLine = this.snippetType.lineToSnippet(this.lineAtCaret); 
        
        String commandSpan = this.text.substring(commandEnd, this.caret);
        
        List<String> treePath = new ArrayList<>();
        
        ListIterator<String> linesIterator = this.linesIteratorFor(commandSpan);
        
        String line;
        String contentPath = "";
        int lineIndentLevel;
        
        printedFileTreeWalking: while ( linesIterator.hasPrevious() ) {            
            line = linesIterator.previous();
            
            lineIndentLevel = line.indexOf(SIGN_OF_FOLDER);
            if ( lineIndentLevel == -1 ) {
                lineIndentLevel = indexOfAny(line, SIGN_OF_FILE, SIGN_OF_TOO_LARGE);
                if ( lineIndentLevel == -1 ) {
                    contentPath = line.trim();
                    break printedFileTreeWalking;
                }
            } else {
                if ( lineIndentLevel >= initialIndentLevel ) {
                    continue printedFileTreeWalking;
                } else {
                    initialIndentLevel = lineIndentLevel;
                    treePath.add(0, removeFolderSign(line));
                }
            }            
        }        
        
        int indexOfContentSign = contentPath.indexOf(" content:");
        if ( indexOfContentSign > -1 ) {
            contentPath = contentPath.substring(0, indexOfContentSign);
        }        
        
        snippetLine = joinToPathFrom(contentPath, joinToPathFrom(treePath), snippetLine);
        return snippetLine;
    }
    
    public Snippet getSnippetAndReset() {
        this.text = "";
        this.caret = 0;
        this.lineAtCaret = "";
        this.state = EMPTY; 
        this.snippetType = UNKNOWN;
        
        Snippet foundSnippet = this.snippet;
        this.snippet = null;
        return foundSnippet;
    }
}
