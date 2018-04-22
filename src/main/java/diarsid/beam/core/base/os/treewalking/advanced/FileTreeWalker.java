/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.advanced;

import java.io.File;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.application.environment.Catalog;
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
import diarsid.beam.core.modules.data.DaoPatternChoices;

import static java.util.Objects.isNull;

import static diarsid.beam.core.base.analyze.similarity.Similarity.isSimilar;
import static diarsid.beam.core.base.analyze.variantsweight.WeightedVariants.unite;
import static diarsid.beam.core.base.control.flow.FlowResult.FAIL;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.objects.Cache.giveBackToCache;
import static diarsid.beam.core.base.objects.Cache.takeFromCache;
import static diarsid.beam.core.base.util.CollectionsUtils.last;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.OptionalUtil.isNotPresent;
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
class FileTreeWalker implements Walker, WalkingInPlace, WalkingByInitiator, WalkingToFind {    
    
    private final FolderTypeDetector folderTypeDetector;
    private final InnerIoEngine ioEngine;
    private final Optional<DaoPatternChoices> daoPatternChoices;
    private final ThreadLocal<WalkState> localState;
    
    FileTreeWalker(
            InnerIoEngine ioEngine, 
            FolderTypeDetector folderTypeDetector) {
        this.folderTypeDetector = folderTypeDetector;
        this.ioEngine = ioEngine;
        this.daoPatternChoices = Optional.empty();
        this.localState = new ThreadLocal<>();
    }
    
    FileTreeWalker(
            InnerIoEngine ioEngine, 
            DaoPatternChoices daoPatternChoices, 
            FolderTypeDetector folderTypeDetector) {
        this.folderTypeDetector = folderTypeDetector;
        this.ioEngine = ioEngine;
        this.daoPatternChoices = Optional.of(daoPatternChoices);
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
    
    @Override
    public WalkingInPlace in(String where) {
        this.state().setWhereToSearch(normalizeSeparators(where));
        return this;
    }

    @Override
    public WalkingInPlace in(Catalog catalog) {
        this.state().setWhereToSearch(normalizeSeparators(
                catalog.path().toAbsolutePath().toString()));
        return this;
    }
    
    @Override
    public WalkingInPlace in(Location location) {
        this.state().setWhereToSearch(location);
        return this;
    }
    
    @Override
    public WalkingInPlace in(LocationSubPath locationSubPath) {
        this.state().setWhereToSearch(locationSubPath);
        return this;
    }
    
    @Override
    public WalkingByInitiator by(Initiator initiator) {
        this.state().set(initiator);
        return this;
    }
    
    @Override
    public WalkingToFind withMaxDepthOf(int maxDepth) {
        this.state().setHowDeepToGo(maxDepth);
        return this;
    }
    
    @Override
    public WalkingToFind walkToFind(String pattern) {
        this.state().setWhatToSearch(pattern);
        return this;
    }
    
    @Override
    public Walker lookingFor(FileSearchMode mode) {
        this.state().set(mode);
        return this;
    }
    
    @Override
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
            WeightedVariants weightedVariants = unite(variantsFoundOnCurrentLevel);
                        
            boolean ifGoDeeper = true;
            
            Answer userAnswer;
            if ( this.isChoiceMadeForPatternWithBestFrom(weightedVariants) ) {
                state.resultFlowCompletedWith(weightedVariants.best().text());
                ifGoDeeper = false;
            } else {
                userAnswer = this.askUserAboutFoundVariants(state, weightedVariants);
                if ( userAnswer.isGiven() ) {
                    state.resultFlowCompletedWith(userAnswer.text());
                    this.asyncTryToSaveChoiceFrom(pattern, userAnswer.text(), weightedVariants);
                    ifGoDeeper = false;
                } else if ( userAnswer.isRejection() ) {
                    state.resultFlowStopped();
                    ifGoDeeper = false;
                }
            }            
                         
            return ifGoDeeper;
        } 
        
        return true;
    }
    
    private boolean isChoiceMadeForPatternWithBestFrom(WeightedVariants weightedVariants) {
        if ( isNotPresent(this.daoPatternChoices) ) {
            return false;
        }
        
        String bestVariant = weightedVariants.best().text();
        return this.daoPatternChoices
                .get()
                .isChoiceMatchTo(this.state().pattern(), bestVariant, weightedVariants);
    }
    
    private void asyncTryToSaveChoiceFrom(
            String pattern, String choice, WeightedVariants weightedVariants) {
        if ( this.daoPatternChoices.isPresent() ) {
            asyncDo(() -> {
                this.daoPatternChoices.get().save(pattern, choice, weightedVariants);
            });
        }
    }

    private Answer askUserAboutFoundVariants(WalkState state, WeightedVariants weightedVariants) {        
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