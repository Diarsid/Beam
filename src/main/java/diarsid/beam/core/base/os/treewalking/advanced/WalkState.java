/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.advanced;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.base.analyze.variantsweight.WeightEstimate;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariant;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.objects.CachedReusable;
import diarsid.beam.core.base.os.treewalking.base.FileSearchMode;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.LocationSubPath;

import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightVariantsList;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.GOOD;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.MODERATE;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.PERFECT;
import static diarsid.beam.core.base.control.flow.FlowResult.COMPLETE;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowCompleted;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.control.io.base.interaction.Help.asHelp;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.os.treewalking.advanced.WalkUtil.addListedFilesTo;
import static diarsid.beam.core.base.os.treewalking.base.FileSearchMode.ALL;
import static diarsid.beam.core.base.util.PathUtils.combineAsPath;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.notExistsInFileSystem;
import static diarsid.beam.core.base.util.PathUtils.splitPathFragmentsFrom;
import static diarsid.beam.core.base.util.StringUtils.isEmpty;

/**
 *
 * @author Diarsid
 */
class WalkState extends CachedReusable {
    
    private static final int UNVISITED_LEVEL = -1;
    
    static {
        CachedReusable.createCacheFor(WalkState.class, () -> new WalkState());
    }
        
    private final List<String> collectedOnCurrentLevel;
    // non final in order to swap levels with ease
    private List<File> currentLevel;
    private List<File> nextLevel;
        
    private final List<WeightedVariant> variants;
    private Initiator initiator;
    private String pattern;
    private Boolean isPatternPath;
    private String absoluteRoot;
    private String relativeRoot;
    private Location location;
    private LocationSubPath locationSubPath;
    private FileSearchMode mode;
    private Integer maxLevelsToVisit;
    private Integer visitedLevel;
    private ValueFlow<String> resultFlow;
    
    private WalkState() {
        super();
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
        if ( isEmpty(this.absoluteRoot) ) {
            return voidFlowFail("root must be specified!");
        } 
        if ( notExistsInFileSystem(this.absoluteRoot) ) {
            return voidFlowFail(format("%s does not exist!", this.absoluteRoot));
        }
                
        this.initialize();
        
        return voidFlowCompleted();
    }

    private void initialize() {
        this.visitedLevel = UNVISITED_LEVEL;
        if ( isNull(this.mode) ) {
            this.mode = ALL;
        }
    }
    
    ValueFlow<String> resultFlow() {        
        return this.resultFlow;
    }

    @Override
    protected void clearForReuse() {
        this.variants.clear();
        this.collectedOnCurrentLevel.clear();
        this.currentLevel.clear();
        this.nextLevel.clear();
        this.visitedLevel = UNVISITED_LEVEL;
        this.initiator = null;
        this.pattern = null;
        this.isPatternPath = null;
        this.absoluteRoot = null;
        this.relativeRoot = null;
        this.location = null;
        this.locationSubPath = null;
        this.mode = null;
        this.maxLevelsToVisit = null;
        this.resultFlow = null;
    }
    
    void setWhereToSearch(String where) {
        this.absoluteRoot = where;
    }
    
    void setWhereToSearch(Location where) {
        this.location = where;
        this.absoluteRoot = where.path();
    }
    
    void setWhereToSearch(LocationSubPath where) {
        this.locationSubPath = where;
        this.absoluteRoot = where.fullPath();
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
    }
    
    boolean patternIsPath() {
        return isPatternPath;
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
    
    Location location() {
        return this.location;
    }
    
    LocationSubPath locationSubPath() {
        return this.locationSubPath;
    }
    
    Initiator initiator() {
        return this.initiator;
    }
    
    String[] patternToPathFragments() {
        return splitPathFragmentsFrom(this.pattern);
    }
    
    File absoluteRootAsFile() {
        return new File(this.absoluteRoot);
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
    
    FileSearchMode mode() {
        return this.mode;
    }
    
    String absoluteRoot() {
        return this.absoluteRoot;
    }
    
    String searchPlace() {
        String where;
        
        if ( this.isPatternPath ) {
            if ( nonNull(this.location) ) {
                if ( nonNull(this.relativeRoot) ) {
                    where = combineAsPath(this.location.name(), this.relativeRoot);
                } else {
                    where = this.location.name();
                }                
            } else {
                if ( nonNull(this.locationSubPath) ) {
                    if ( nonNull(this.relativeRoot) ) {
                        where = combineAsPath(locationSubPath.fullPath(), this.relativeRoot);
                    } else {
                        where = locationSubPath.fullPath();
                    }
                } else {
                    where = this.absoluteRoot;
                }
            }            
        } else {
            if ( nonNull(this.location) ) {
                where = this.location.name();
            } else {
                if ( nonNull(this.locationSubPath) ) {
                    where = locationSubPath.fullPath();
                } else {
                    where = this.absoluteRoot;
                }
            }
        }
        
        return where;
    }
    
    Help composeHelp() {        
        return asHelp(
                format("Choose variant for '%s' in %s", this.pattern, this.searchPlace()), 
                "Use:", 
                "   - number of target to choose it",
                "   - part of target name to choose it",
                "   - 'n' or 'no' to see another variants, if any",
                "   - dot (.) to reject all variants"
        );
    }
    
    void weightCollectedOnCurrentLevelAgainstPatternAndAddToVariants() {
        List<WeightedVariant> currentLevelVariants = 
                weightVariantsList(this.pattern, stringsToVariants(collectedOnCurrentLevel));
        this.variants.addAll(currentLevelVariants);
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
    
    void resultFlowCompletedWithAnswer(Answer answer) {
        this.resultFlow = valueFlowCompletedWith(answer.text());
    }
    
    void resultFlowStopped() {
        this.resultFlow = valueFlowStopped();
    }
    
    void resultFlowFailBecauseAbsoluteRootIsNotDirectory() {
        this.resultFlow = valueFlowFail(format("%s is not a directory!", this.absoluteRoot));
    }
    
    void processResultFlowAfterSearching() {        
        if ( this.isPatternPath && this.isResultFlowCompletedWithValue() ) {
            String lastFoundTarget = this.resultFlow.asComplete().getOrThrow();
            String foundPathTarget = combineAsPath(this.relativeRoot, lastFoundTarget);
            this.resultFlow = valueFlowCompletedWith(foundPathTarget);
        }
    }
    
    void processResultFlowAfterSingleWalkIteration() {
        if ( isNull(this.resultFlow) ) {            
            this.resultFlow = valueFlowCompletedEmpty(format(
                "'%s' not found in %s", this.pattern, this.searchPlace()));
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
        String foundTarget = this.resultFlow.asComplete().getOrThrow();
        if ( isNull(this.relativeRoot) ) {
            this.relativeRoot = foundTarget;
        } else {
            this.relativeRoot = combineAsPath(this.relativeRoot, foundTarget);
        }
        this.absoluteRoot = combineAsPath(this.absoluteRoot, foundTarget);   
        this.resultFlow = null;
    }
    
    boolean isResultFlowCompletedWithValue() {        
        return this.resultFlow.result().is(COMPLETE) && this.resultFlow.asComplete().hasValue();
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
    
}
