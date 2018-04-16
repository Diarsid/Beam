/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.advanced;

import java.io.File;
import java.util.List;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariant;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.os.treewalking.base.FileSearchMode;
import diarsid.beam.core.base.os.treewalking.base.FolderTypeDetector;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.LocationSubPath;

import static java.util.Objects.isNull;

import static diarsid.beam.core.base.analyze.similarity.Similarity.isSimilar;
import static diarsid.beam.core.base.analyze.variantsweight.WeightedVariants.unite;
import static diarsid.beam.core.base.control.flow.FlowResult.FAIL;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.objects.Cache.giveBackToCache;
import static diarsid.beam.core.base.objects.Cache.takeFromCache;
import static diarsid.beam.core.base.util.CollectionsUtils.last;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.PathUtils.extractLastElementFromPath;
import static diarsid.beam.core.base.util.PathUtils.normalizeSeparators;
import static diarsid.beam.core.base.util.PathUtils.removeSeparators;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.haveEqualLength;
import static diarsid.beam.core.base.util.StringUtils.lower;


/**
 *
 * @author Diarsid
 */
public class FileTreeWalker {    
    
    private final FolderTypeDetector folderTypeDetector;
    private final InnerIoEngine ioEngine;
    private final ThreadLocal<WalkState> localState;
    
    public FileTreeWalker(InnerIoEngine ioEngine, FolderTypeDetector folderTypeDetector) {
        this.folderTypeDetector = folderTypeDetector;
        this.ioEngine = ioEngine;
        this.localState = new ThreadLocal<>();
    }
    
    private WalkState state() {
        WalkState state = this.localState.get();
        if ( isNull(state) ) {
            state = takeFromCache(WalkState.class);
            this.localState.set(state);
        }
        return state;
    }
    
    public FileTreeWalker in(String where) {
        this.state().setWhereToSearch(where);
        return this;
    }
    
    public FileTreeWalker in(Location location) {
        this.state().setWhereToSearch(location);
        return this;
    }
    
    public FileTreeWalker in(LocationSubPath locationSubPath) {
        this.state().setWhereToSearch(locationSubPath);
        return this;
    }
    
    public FileTreeWalker by(Initiator initiator) {
        this.state().set(initiator);
        return this;
    }
    
    public FileTreeWalker search(String pattern) {
        this.state().setWhatToSearch(pattern);
        return this;
    }
    
    public FileTreeWalker withMaxDepthOf(int maxDepth) {
        this.state().setHowDeepToGo(maxDepth);
        return this;
    }
    
    public FileTreeWalker lookingFor(FileSearchMode mode) {
        this.state().set(mode);
        return this;
    }
    
    public ValueFlow<String> andGetResult() {        
        try {
            WalkState state = this.state();
            VoidFlow checkFlow = state.checkBeforeUseAndInitialize();
            if ( checkFlow.result().is(FAIL) ) {
                return valueFlowFail(checkFlow.message());
            }
            this.walkUsing(state);
            state.processResultFlowAfterSearching();
            return state.resultFlow();            
        } finally {
            this.cleanThreadLocalState();
        }
    }
    
    private void walkUsing(WalkState state) {
        if ( state.patternIsPath() ) {
            this.multipleWalkIterationsThroughPathUsing(state);
        } else {
            this.singleWalkIterationUsing(state);
        }
    }
    
    private void multipleWalkIterationsThroughPathUsing(WalkState state) {
        String[] pathPatterns = state.patternToPathFragments();
        String lastPathPattern = last(pathPatterns);
        walkingThroughPath: for (String pathPattern : pathPatterns) {
            state.muteBeforeWalkingForNextPatternInPath(pathPattern);
            this.singleWalkIterationUsing(state);
            if ( state.isResultFlowCompletedWithValue() ) {
                if ( pathPattern.equals(lastPathPattern) ) {
                    break walkingThroughPath;
                } else {
                    state.muteAfterWalkingForPattern();
                }                
            } else {
                break walkingThroughPath;
            }            
        }
    }
    
    private void singleWalkIterationUsing(WalkState state) {
        try {
            File root = state.absoluteRootAsFile();
        
            if ( ! root.isDirectory() ) {
                state.resultFlowFailBecauseAbsoluteRootIsNotDirectory();
                return;
            }

            boolean needToGoDeeper;

            state.addListedFilesToCurrentLevel(root.listFiles());
            this.walkThroughCurrentLevel(state);
            needToGoDeeper = this.consumeAndDefineIfGoDeeper(state);

            if ( state.nextLevel().isEmpty() || state.ifCannotGoDeeper() ) {
                return;
            }

            boolean canGoDeeper = true;
            while ( needToGoDeeper && canGoDeeper && nonEmpty(state.nextLevel()) ) {   
                state.swapNextLevelToCurrentLevel();            
                this.walkThroughCurrentLevel(state);
                needToGoDeeper = this.consumeAndDefineIfGoDeeper(state);
                canGoDeeper = state.ifCanGoDeeper();
            }
        } finally {
            state.processResultFlowAfterSingleWalkIteration();
        }        
    }
    
    private void cleanThreadLocalState() {
        WalkState state = this.localState.get();
        this.localState.remove();
        giveBackToCache(state);
    }
    
    private boolean consumeAndDefineIfGoDeeper(WalkState state) {
        try {
            String processedFile;
            int rootLength = state.absoluteRoot().length() + 1; // +1 to cut separator after root
            for (int i = 0; i < state.collectedOnCurrentLevel().size(); i++) {
                processedFile = state
                        .collectedOnCurrentLevel()
                        .get(i)
                        .substring(rootLength); 
                state.collectedOnCurrentLevel().set(i, processedFile);
            }  
            return this.consumeRefinedAndDefineIfGoDeeper(state);
        } finally {
            state.collectedOnCurrentLevel().clear();
        }
    }
    
    private boolean consumeRefinedAndDefineIfGoDeeper(WalkState state) {
        
        String pattern = state.pattern();
        String processedFile;
        for (int i = 0; i < state.collectedOnCurrentLevel().size(); i++) {
            processedFile = state.collectedOnCurrentLevel().get(i);
            if ( ! fileIsSimilarToPattern(processedFile, pattern) ) {
                state.collectedOnCurrentLevel().remove(i);
                i--;
            }
        }
        
        if ( state.collectedOnCurrentLevel().isEmpty() ) {
            return true;
        }
        
        state.weightCollectedOnCurrentLevelAgainstPatternAndAddToVariants();
        
        if ( state.variants().isEmpty() ) {
            return true;
        }
        
        state.sortVariants();
                
        if ( state.hasFirstVariantAcceptableWeightEstimate() ) {
            List<WeightedVariant> variantsFoundOnCurrentLevel = 
                    state.extractVariantsAcceptableOnCurrentLevel();
            Answer answer = this.askAboutFoundVariants(state, variantsFoundOnCurrentLevel);
            
            boolean ifGoDeeper = true;
            
            if ( answer.isGiven() ) {
                state.resultFlowCompletedWithAnswer(answer);
                ifGoDeeper = false;
            } else if ( answer.isRejection() ) {
                state.resultFlowStopped();
                ifGoDeeper = false;
            }             
            return ifGoDeeper;
        } 
        
        return true;
    }

    private Answer askAboutFoundVariants(WalkState state, List<WeightedVariant> chosenVariants) {
        WeightedVariants weightedVariants = unite(chosenVariants);
        Help help = state.composeHelp();
        Answer answer = this.ioEngine
                .chooseInWeightedVariants(state.initiator(), weightedVariants, help);
        return answer;
    }

    private void walkThroughCurrentLevel(WalkState state) {
        for (File file : state.currentLevel()) {
            if ( state.mode().correspondsTo(file) ) {
                state.collectedOnCurrentLevel().add(normalizeSeparators(file.getAbsolutePath()));
            }
            if ( this.canEnterIn(file) ) {
                state.addListedFilesToNextLevel(file.listFiles());
            }
        }
        state.incrementVisitedLevel();
    }
    
    private boolean canEnterIn(File file) {
        return file.isDirectory() 
                && ! file.isHidden() 
                && this.folderTypeDetector.safeExamineTypeOf(file).isNotRestricted();
    }
    
    static boolean fileIsSimilarToPattern(String file, String pattern) {
        if ( pattern.isEmpty() ) {
            return true;
        }
        
        file = lower(file);
        String fileName = extractLastElementFromPath(file);
        
        if ( containsIgnoreCase(fileName, pattern) ) {
            return true;
        } else {            
            if ( haveEqualLength(file, fileName) ) {
                return isSimilar(file, pattern);
            } else {
                if ( isSimilar(fileName, pattern) ) {
                    return true;
                } else {
                    String filePathWithoutSeparators = removeSeparators(file);
                    if ( containsIgnoreCase(filePathWithoutSeparators, pattern) ) {
                        return true;
                    } else {
                        return isSimilar(filePathWithoutSeparators, pattern);
                    }
                }
            }
        }
    }
}
