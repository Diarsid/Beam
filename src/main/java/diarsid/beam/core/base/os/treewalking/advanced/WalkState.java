/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.advanced;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.application.environment.Catalog;
import diarsid.beam.core.base.analyze.variantsweight.Analyze;
import diarsid.beam.core.base.analyze.variantsweight.WeightEstimate;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariant;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.os.treewalking.base.FileSearchMode;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.support.objects.PooledReusable;

import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.GOOD;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.MODERATE;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.PERFECT;
import static diarsid.beam.core.base.control.flow.FlowResult.DONE;
import static diarsid.beam.core.base.control.flow.FlowResult.FAIL;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowDoneEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowDoneWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowDone;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.control.io.base.interaction.Help.asHelp;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringToVariant;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.os.treewalking.advanced.WalkUtil.addListedFilesTo;
import static diarsid.beam.core.base.os.treewalking.base.FileSearchMode.FILES_AND_FOLDERS;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.joinToPath;
import static diarsid.beam.core.base.util.PathUtils.splitPathFragmentsFrom;
import static diarsid.beam.core.base.util.StringUtils.isEmpty;

/**
 *
 * @author Diarsid
 */
class WalkState extends PooledReusable {
    
    private static final int UNVISITED_LEVEL = -1;
            
    private final Analyze analyze;
    private final WalkStartPlace place;
    private final List<WeightedVariant> variants;
    private final List<String> collectedOnCurrentLevel;
    private /* non-final in order to swap levels with ease */ List<File> currentLevel;
    private /* non-final in order to swap levels with ease */ List<File> nextLevel;
    private Initiator initiator;
    private String pattern;
    private Boolean isPatternPath;
    private FileSearchMode mode;
    private Integer maxLevelsToVisit;
    private Integer visitedLevel;
    private ValueFlow<String> resultFlow;
    
    WalkState(Analyze analyze) {
        super();
        this.analyze = analyze;
        this.place = new WalkStartPlace();
        this.variants = new ArrayList<>();
        this.collectedOnCurrentLevel = new ArrayList<>();
        this.currentLevel = new ArrayList<>();
        this.nextLevel = new ArrayList<>();
    }
    
    VoidFlow checkBeforeUseAndInitialize() {
        if ( isNull(this.initiator) ) {
            return voidFlowFail("initiator must be specified!");
        }
        if ( isEmpty(this.pattern) ) {
            return voidFlowFail("pattern must be specified!");
        }
        VoidFlow placeCheck = this.place.check();
        if ( placeCheck.result().is(FAIL) ) {
            return placeCheck;
        }
                
        this.initialize();
        
        return voidFlowDone();
    }

    private void initialize() {
        this.visitedLevel = UNVISITED_LEVEL;
        if ( isNull(this.mode) ) {
            this.mode = FILES_AND_FOLDERS;
        }
    }
    
    ValueFlow<String> resultFlow() {        
        return this.resultFlow;
    }

    @Override
    protected void clearForReuse() {
        this.place.clear();
        
        this.variants.clear();
        this.collectedOnCurrentLevel.clear();
        this.currentLevel.clear();
        this.nextLevel.clear();
        
        this.visitedLevel = UNVISITED_LEVEL;
        
        this.initiator = null;
        this.pattern = null;
        this.isPatternPath = null;
        this.mode = null;
        this.maxLevelsToVisit = null;
        this.resultFlow = null;
    }
    
    void setHowDeepToGo(int maxDepth) {
        this.maxLevelsToVisit = maxDepth;
    }
    
    void set(Initiator initiator) {
        this.initiator = initiator;
    }
    
    void set(FileSearchMode mode) {
        this.mode = mode;
    }
    
    void setWhatToSearch(String pattern) {
        this.pattern = pattern;
        this.isPatternPath = containsPathSeparator(pattern);
        this.place.setIsPatternPath(this.isPatternPath);
    }
    
    void setWhereToSearch(Catalog where) {
        this.place.setWhereToSearch(where);
    }
    
    void setWhereToSearch(String where) {
        this.place.setWhereToSearch(where);
    }
    
    void setWhereToSearch(Location where) {
        this.place.setWhereToSearch(where);
    }
    
    void setWhereToSearch(LocationSubPath where) {
        this.place.setWhereToSearch(where);
    }
    
    boolean patternIsPath() {
        return this.isPatternPath;
    }
    
    String absoluteRoot() {
        return this.place.absoluteRoot();
    }
    
    File absoluteRootAsFile() {
        return this.place.absoluteRootAsFile();
    }
    
    WeightEstimate weightEstimateAcceptableForCurrentLevel() {
        switch ( this.visitedLevel ) {
            case 0:
                return PERFECT;
            case 1:
                return GOOD;
            default:
                return MODERATE; 
        }
    }
    
    String pattern() {
        return this.pattern;
    }
    
    boolean isDepthDefined() {
        return nonNull(this.maxLevelsToVisit);
    }
    
    Integer depth() {
        return this.maxLevelsToVisit;
    }
    
    Initiator initiator() {
        return this.initiator;
    }
    
    String[] patternToPathFragments() {
        return splitPathFragmentsFrom(this.pattern);
    }
    
    List<String> collectedOnCurrentLevel() {
        return this.collectedOnCurrentLevel;
    }
    
    List<File> currentLevel() {
        return this.currentLevel;
    }
    
    List<File> nextLevel() {
        return this.nextLevel;
    }
    
    List<WeightedVariant> variants() {
        return this.variants;
    }
    
    FileSearchMode searchMode() {
        return this.mode;
    }
    
    Help composeHelp() {        
        return asHelp(
                format("Choose variant for '%s' in %s", this.pattern, this.place.name()), 
                "Use:", 
                "   - number of target to choose it",
                "   - part of target name to choose it",
                "   - 'n' or 'no' to see another variants, if any",
                "   - dot (.) to reject all variants"
        );
    }
    
    void weightCollectedOnCurrentLevelAgainstPatternAndAddToVariants() {
        if ( hasOne(this.collectedOnCurrentLevel) ) {
            this.analyze.weightVariant(
                    this.pattern, stringToVariant(getOne(this.collectedOnCurrentLevel)))
                    .ifPresent(weightedVariant -> this.variants.add(weightedVariant));
        } else {
            List<WeightedVariant> currentLevelVariants = this.analyze.weightVariantsList(
                    this.pattern, stringsToVariants(this.collectedOnCurrentLevel));
            this.variants.addAll(currentLevelVariants);
        }        
    }
    
    void sortVariants() {
        sort(this.variants);
    }
    
    boolean hasFirstVariantAcceptableWeightEstimate() {
        WeightEstimate acceptableWeightEstimate = this.weightEstimateAcceptableForCurrentLevel();
        return this.variants.get(0).hasEqualOrBetterWeightThan(acceptableWeightEstimate);
    }
    
    List<WeightedVariant> extractVariantsAcceptableOnCurrentLevel() {
        WeightEstimate acceptableWeightEstimate = this.weightEstimateAcceptableForCurrentLevel();
        List<WeightedVariant> extractedAcceptableVariants = new ArrayList<>();
        
        WeightedVariant variant;
        acceptableVariantsExtraction: for (int i = 0; i < this.variants.size(); i++) {
            variant = this.variants.get(i);
            if ( variant.hasEqualOrBetterWeightThan(acceptableWeightEstimate) ) {
                extractedAcceptableVariants.add(variant);
                this.variants.remove(i);
                i--;
            } else {
                break acceptableVariantsExtraction;
            }
        }
        
        return extractedAcceptableVariants;
    }
    
    void addListedFilesToCurrentLevel(File[] listedFiles) {
        this.currentLevel.clear();
        addListedFilesTo(this.currentLevel, listedFiles);
    }
    
    void addListedFilesToNextLevel(File[] listedFiles) {
        addListedFilesTo(this.nextLevel, listedFiles);
    }
    
    void swapNextLevelToCurrentLevel() {
        List<File> levelsSwap;
        
        this.currentLevel.clear();
        levelsSwap = this.currentLevel;
        this.currentLevel = this.nextLevel;
        this.nextLevel = levelsSwap;
    }
    
    void resultFlowDoneWith(String result) {
        this.resultFlow = valueFlowDoneWith(result);
    }
        
    void resultFlowStopped() {
        this.resultFlow = valueFlowStopped();
    }
    
    void resultFlowFailBecauseAbsoluteRootIsNotDirectory() {
        this.resultFlow = valueFlowFail(format(
                "%s is not a directory!", this.place.absoluteRoot()));
    }
    
    void processResultFlowAfterSearching() {        
        if ( this.isPatternPath && this.isResultFlowDoneWithValue() ) {
            String lastFoundTarget = this.resultFlow.asDone().orThrow();
            String foundPathTarget = joinToPath(this.place.relativeRoot(), lastFoundTarget);
            this.resultFlow = valueFlowDoneWith(foundPathTarget);
        }
    }
    
    void processResultFlowAfterSingleWalkIteration() {
        if ( isNull(this.resultFlow) ) {            
            this.resultFlow = valueFlowDoneEmpty(format(
                "'%s' not found in %s", this.pattern, this.place.name()));
        }
    }
    
    void muteBeforeWalkingForNextPatternInPath(String pathPattern) {
        this.pattern = pathPattern;
        if ( nonNull(this.maxLevelsToVisit) && this.maxLevelsToVisit > 0 ) {
            this.maxLevelsToVisit--;
        }
    }
    
    void muteAfterWalkingForPattern() {
        this.variants.clear();
        String foundTarget = this.resultFlow.asDone().orThrow();
        this.place.muteUsing(foundTarget); 
        this.resultFlow = null;
    }
    
    boolean isResultFlowDoneWithValue() {        
        return this.resultFlow.result().is(DONE) && this.resultFlow.asDone().hasValue();
    }
    
    void incrementVisitedLevel() {
        this.visitedLevel++;
    }
    
    boolean ifCannotGoDeeper() {
        return ( nonNull(this.maxLevelsToVisit) && this.maxLevelsToVisit == 0 );
    }
    
    boolean ifCanGoDeeper() {
        return isNull(this.maxLevelsToVisit) || this.visitedLevel < this.maxLevelsToVisit;
    }
    
    String walkingQuery() {
        String query;
        
        if ( this.isDepthDefined() ) {
            query = format(
                "\n" +
                "    FIND %s\n" +
                "    LIKE PATTERN %s\n" +
                "    IN %s\n" +
                "    WITH DEPTH %s", 
                this.mode, 
                this.pattern, 
                this.place.absoluteRoot(), 
                this.depth());
        } else {
            query = format(
                "\n" +
                "    FIND %s\n" +
                "    LIKE PATTERN %s\n" +
                "    IN %s", 
                this.mode, 
                this.pattern, 
                this.place.absoluteRoot());
        }
        
        return query;
    }
    
}
