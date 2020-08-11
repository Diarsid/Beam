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
import diarsid.beam.core.base.analyze.similarity.Similarity;
import diarsid.beam.core.base.analyze.variantsweight.Variant;
import diarsid.beam.core.base.analyze.variantsweight.Variants;
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
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoPatternChoices;
import diarsid.support.objects.Pool;

import static java.util.Objects.isNull;

import static diarsid.beam.core.base.analyze.variantsweight.Variants.findVariantEqualToPattern;
import static diarsid.beam.core.base.analyze.variantsweight.Variants.unite;
import static diarsid.beam.core.base.control.flow.FlowResult.FAIL;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.util.CollectionsUtils.last;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.OptionalUtil.isNotPresent;
import static diarsid.beam.core.base.util.PathUtils.extractLastElementFromPath;
import static diarsid.beam.core.base.util.PathUtils.joinToPath;
import static diarsid.beam.core.base.util.PathUtils.normalizeSeparators;
import static diarsid.beam.core.base.util.PathUtils.removeSeparators;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.support.log.Logging.logFor;
import static diarsid.support.strings.StringUtils.haveEqualLength;
import static diarsid.support.strings.StringUtils.lower;


/**
 *
 * @author Diarsid
 */
class FileTreeWalker implements Walker, WalkingInPlace, WalkingByInitiator, WalkingToFind {    
    
    private final FolderTypeDetector folderTypeDetector;
    private final InnerIoEngine ioEngine;
    private final Similarity similarity;
    private final Pool<WalkState> statePool;
    private final Optional<ResponsiveDaoPatternChoices> daoPatternChoices;
    private final ThreadLocal<WalkState> localState;
    
    FileTreeWalker(
            InnerIoEngine ioEngine, 
            Similarity similarity,
            FolderTypeDetector folderTypeDetector,
            Pool<WalkState> statePool) {
        this.folderTypeDetector = folderTypeDetector;
        this.ioEngine = ioEngine;
        this.similarity = similarity;
        this.statePool = statePool;
        this.daoPatternChoices = Optional.empty();
        this.localState = new ThreadLocal<>();
    }
    
    FileTreeWalker(
            InnerIoEngine ioEngine, 
            Similarity similarity,
            ResponsiveDaoPatternChoices daoPatternChoices, 
            FolderTypeDetector folderTypeDetector,
            Pool<WalkState> statePool) {
        this.folderTypeDetector = folderTypeDetector;
        this.ioEngine = ioEngine;
        this.similarity = similarity;
        this.statePool = statePool;
        this.daoPatternChoices = Optional.of(daoPatternChoices);
        this.localState = new ThreadLocal<>();
    }
    
    private WalkState state() {
        WalkState state = this.localState.get();
        if ( isNull(state) ) {
            state = this.statePool.give();
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
        if ( location.hasSubPath() ) {
            this.state().setWhereToSearch((LocationSubPath) location);
        } else {
            this.state().setWhereToSearch(location);
        }
        return this;
    }
    
    @Override
    public WalkingByInitiator by(Initiator initiator) {
        this.state().set(initiator);
        return this;
    }
    
    @Override
    public WalkingToFind withMinDepthOf(int minDepth) {
        this.state().setHowDeepToGoMin(minDepth);
        return this;
    }
    
    @Override
    public WalkingToFind withMaxDepthOf(int maxDepth) {
        this.state().setHowDeepToGoMax(maxDepth);
        return this;
    }
    
    @Override
    public WalkingToFind walkToFind(String pattern) {
        this.state().setWhatToSearch(pattern);
        return this;
    }

    @Override
    public WalkingToFind withPredefined(List<Variant> predefinedVariants) {
        this.state().setPredefinedVariants(predefinedVariants);
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
            this.processWalkingResultsIn(state);
            return state.resultFlow();            
        } finally {
            this.cleanThreadLocalState();
        }
    }
    
    private void walkUsing(WalkState state) {
        logFor(this).info(this.state().reportWalkingQuery());
        if ( state.patternIsPath() ) {
            this.multipleWalkIterationsThroughPathUsing(state);
        } else {
            this.singleWalkIterationUsing(state);
        }
    }
    
    private void processWalkingResultsIn(WalkState state) {
        if ( state.variants().isEmpty() ) {
            state.processResultFlowAfterSearching();
        } else {
            Variants variants = unite(state.variants());
            Answer userAnswer = this.askUserAboutFoundVariants(state, variants);
            if ( userAnswer.isGiven() ) {
                state.resultFlowDoneWith(userAnswer.text());
                state.variants().clear();
                this.asyncTryToSaveChoiceFrom(state.pattern(), userAnswer.text(), variants);
            } else if ( userAnswer.variantsAreNotSatisfactory() ) {
                state.variants().clear();
            } else if ( userAnswer.isRejection() ) {
                state.resultFlowStopped();
            }
        }                     
    }
    
    private void multipleWalkIterationsThroughPathUsing(WalkState state) {
        String[] pathPatterns = state.patternToPathFragments();
        String lastPathPattern = last(pathPatterns);
        walkingThroughPath: for (String pathPattern : pathPatterns) {
            state.muteBeforeWalkingForNextPatternInPath(pathPattern);
            this.singleWalkIterationUsing(state);
            if ( state.isResultFlowDoneWithValue() ) {
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
            needToGoDeeper = this.consumeRefinedWalkResultsAndDefineIfGoDeeper(state);
            state.collectedOnCurrentLevelClear();

            if ( state.nextLevel().isEmpty() || state.ifCannotGoDeeper() ) {
                return;
            }

            boolean canGoDeeper = true;
            while ( needToGoDeeper && canGoDeeper && nonEmpty(state.nextLevel()) ) {   
                state.swapNextLevelToCurrentLevel();            
                this.walkThroughCurrentLevel(state);
                needToGoDeeper = this.consumeRefinedWalkResultsAndDefineIfGoDeeper(state);
                state.collectedOnCurrentLevelClear();
                canGoDeeper = state.ifCanGoDeeper();
            }
        } finally {
            state.processResultFlowAfterSingleWalkIteration();
        }        
    }
    
    private void cleanThreadLocalState() {
        WalkState state = this.localState.get();
        this.localState.remove();
        this.statePool.takeBack(state);
    }
    
    private boolean consumeRefinedWalkResultsAndDefineIfGoDeeper(WalkState state) {
        logFor(this).info(state.reportCollectedOnCurrentLevel());
        
        String pattern = state.pattern();
        String processedFile;
        for (int i = 0; i < state.collectedOnCurrentLevelSize(); i++) {
            processedFile = state.collectedOnCurrentLevelGet(i);
            if ( ! this.fileIsSimilarToPattern(processedFile, pattern) ) {
                state.collectedOnCurrentLevelRemove(i);
                i--;
            }
        }
        
        if ( state.collectedOnCurrentLevelNonEmpty() ) {
            state.weightCollectedOnCurrentLevelAgainstPatternAndAddToVariants();
        }
        
        if ( state.variants().isEmpty() ) {
            return true;
        }
        
        if ( state.doesFirstVariantMatchPatternExactly() ) {
            state.resultFlowDoneWith(state.variants().get(0).value());
            state.variants().clear();
            return false;
        }
        
        boolean minimumLevelReachedAndAcceptableVariantExist = 
                state.hasFirstVariantAcceptableWeightEstimate() &&
                state.canStopAtCurrentLevel();
        
        boolean bestVariantExceedBestPredefinedVariant = 
                state.hasMinPredefinedWeight() && 
                state.isFirstCollectedVariantBetterThanMinPredefinedWeight();
        
        boolean canStopAtCurrentLevel = 
                minimumLevelReachedAndAcceptableVariantExist ||
                bestVariantExceedBestPredefinedVariant;
                
        if ( canStopAtCurrentLevel ) {
            List<Variant> variantsFoundOnCurrentLevel = 
                    state.extractVariantsAcceptableOnCurrentLevel();
            
            boolean needGoDeeper = true;
            
            Optional<Variant> variantEqualToPattern = 
                    findVariantEqualToPattern(variantsFoundOnCurrentLevel);
            if ( variantEqualToPattern.isPresent() ) {
                state.resultFlowDoneWith(variantEqualToPattern.get().value());
                state.variants().clear();
                needGoDeeper = false;
                return needGoDeeper;
            }
            
            Variants weightedVariants = unite(variantsFoundOnCurrentLevel);
            
            Answer userAnswer;
            if ( this.isChoiceMadeForPatternWithBestFrom(weightedVariants) ) {
                state.resultFlowDoneWith(weightedVariants.best().value());
                state.variants().clear();
                needGoDeeper = false;
            } else {
                userAnswer = this.askUserAboutFoundVariants(state, weightedVariants);
                if ( userAnswer.isGiven() ) {
                    state.resultFlowDoneWith(userAnswer.text());
                    state.variants().clear();
                    this.asyncTryToSaveChoiceFrom(pattern, userAnswer.text(), weightedVariants);
                    needGoDeeper = false;
                } else if ( userAnswer.variantsAreNotSatisfactory() ) {
                    state.variants().clear();
                    needGoDeeper = true;
                } else if ( userAnswer.isRejection() ) {
                    state.resultFlowStopped();
                    state.variants().clear();
                    needGoDeeper = false;
                }
            }            
                         
            return needGoDeeper;
        } 
        
        return true;
    }
    
    private boolean isChoiceMadeForPatternWithBestFrom(Variants weightedVariants) {
        if ( isNotPresent(this.daoPatternChoices) ) {
            return false;
        }
        
        String bestVariant = weightedVariants.best().value();
        WalkState state = this.state();
        return this.daoPatternChoices
                .get()
                .hasMatchOf(state.initiator(), state.pattern(), bestVariant, weightedVariants);
    }
    
    private void asyncTryToSaveChoiceFrom(
            String pattern, String choice, Variants weightedVariants) {
        if ( this.daoPatternChoices.isPresent() ) {
            Initiator initiator = this.state().initiator();
            asyncDo(() -> {
                this.daoPatternChoices.get().save(initiator, pattern, choice, weightedVariants);
            });
        }
    }

    private Answer askUserAboutFoundVariants(WalkState state, Variants weightedVariants) {        
        Help help = state.composeHelp();
        Answer answer = this.ioEngine
                .ask(state.initiator(), weightedVariants, help);
        return answer;
    }

    private void walkThroughCurrentLevel(WalkState state) {
        state.nextLevel().clear();
        
        String absolutePath;
        String relativePathWithRoot;
        String relativePathWithoutRoot;
        
        int rootLengthWithName = state.root().length() + 1; // +1 to cut separator after root
        
        for (File file : state.currentLevel()) {            
            if ( state.searchMode().correspondsTo(file) ) {
                absolutePath = normalizeSeparators(file.getAbsolutePath());
                relativePathWithoutRoot = absolutePath.substring(rootLengthWithName);
                relativePathWithRoot = joinToPath(state.rootName(), relativePathWithoutRoot);
                state.collectedOnCurrentLevelAdd(relativePathWithoutRoot, relativePathWithRoot);
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
    
    private boolean fileIsSimilarToPattern(String file, String pattern) {
        if ( pattern.isEmpty() ) {
            return true;
        }
        
        file = lower(file);
        String fileName = extractLastElementFromPath(file);
        
        if ( containsIgnoreCase(fileName, pattern) ) {
            return true;
        } else {            
            if ( haveEqualLength(file, fileName) ) {
                return this.similarity.isSimilar(file, pattern);
            } else {
                if ( this.similarity.isSimilar(fileName, pattern) ) {
                    return true;
                } else {
                    String filePathWithoutSeparators = removeSeparators(file);
                    if ( containsIgnoreCase(filePathWithoutSeparators, pattern) ) {
                        return true;
                    } else {
                        return this.similarity.isSimilar(filePathWithoutSeparators, pattern);
                    }
                }
            }
        }
    }
}
