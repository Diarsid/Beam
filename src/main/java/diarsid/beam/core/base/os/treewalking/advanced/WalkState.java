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
import diarsid.beam.core.base.analyze.variantsweight.WeightEstimate;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariant;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.objects.CachedReusable;
import diarsid.beam.core.base.os.treewalking.base.FileSearchMode;
import diarsid.beam.core.base.util.Possible;
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
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.joinToPath;
import static diarsid.beam.core.base.util.PathUtils.notExistsInFileSystem;
import static diarsid.beam.core.base.util.PathUtils.splitPathFragmentsFrom;
import static diarsid.beam.core.base.util.Possible.possible;
import static diarsid.beam.core.base.util.StringUtils.isEmpty;
import static diarsid.beam.core.base.util.StringUtils.nonEmpty;

/**
 *
 * @author Diarsid
 */
class WalkState extends CachedReusable {
    
    private static final int UNVISITED_LEVEL = -1;
    
    static {
        CachedReusable.createCacheFor(WalkState.class, () -> new WalkState());
    }
            
    private final Possible<String> absoluteRoot;
    private final Possible<Location> location;
    private final Possible<LocationSubPath> locationSubPath;
    private final Possible<Catalog> catalog;      
    private final Possible<String> relativeRoot;
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
    
    private WalkState() {
        super();
        this.absoluteRoot = possible();
        this.location = possible();
        this.locationSubPath = possible();
        this.catalog = possible();
        this.relativeRoot = possible();
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
        if ( this.absoluteRoot.notMatch(root -> nonEmpty(root)) ) {
            return voidFlowFail("root must be specified!");
        } 
        if ( notExistsInFileSystem(this.absoluteRoot.orThrow()) ) {
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
        
        this.absoluteRoot.nullify();
        this.location.nullify();
        this.locationSubPath.nullify();
        this.catalog.nullify();
        this.relativeRoot.nullify();
        
        this.visitedLevel = UNVISITED_LEVEL;
        
        this.initiator = null;
        this.pattern = null;
        this.isPatternPath = null;
        this.mode = null;
        this.maxLevelsToVisit = null;
        this.resultFlow = null;
    }
    
    void setWhereToSearch(Catalog where) {
        this.catalog.resetTo(where);
    }
    
    void setWhereToSearch(String where) {
        this.absoluteRoot.resetTo(where);
    }
    
    void setWhereToSearch(Location where) {
        this.location.resetTo(where);
        this.absoluteRoot.resetTo(where.path());
    }
    
    void setWhereToSearch(LocationSubPath where) {
        this.locationSubPath.resetTo(where);
        this.absoluteRoot.resetTo(where.fullPath());
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
    
    Initiator initiator() {
        return this.initiator;
    }
    
    String[] patternToPathFragments() {
        return splitPathFragmentsFrom(this.pattern);
    }
    
    File absoluteRootAsFile() {
        return new File(this.absoluteRoot.orThrow());
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
        return this.absoluteRoot.orThrow();
    }
    
    String searchPlace() {
        String where;
        
        if ( this.isPatternPath ) {
            if ( this.location.isPresent() ) {
                where = this.location.orThrow().name();
                if ( this.relativeRoot.isPresent() ) {
                    where = joinToPath(where, this.relativeRoot.orThrow());
                }                
            } else if ( this.locationSubPath.isPresent() ) {
                where = locationSubPath.orThrow().fullPath();
                if ( this.relativeRoot.isPresent() ) {
                    where = joinToPath(where, this.relativeRoot.orThrow());
                }
            } else if ( this.catalog.isPresent() ) { 
                where = this.catalog.orThrow().name();
                if ( this.relativeRoot.isPresent() ) {
                    where = joinToPath(where, this.relativeRoot.orThrow());
                }
            } else {
                where = this.absoluteRoot.orThrow();
            }      
        } else {
            if ( this.location.isPresent() ) {
                where = this.location.orThrow().name();
            } else if ( this.locationSubPath.isPresent() ) {
                where = this.locationSubPath.orThrow().fullPath();
            } else if ( this.catalog.isPresent() ) { 
                where = this.catalog.orThrow().name();
            } else {
                where = this.absoluteRoot.orThrow();
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
    
    void resultFlowCompletedWith(String result) {
        this.resultFlow = valueFlowCompletedWith(result);
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
            String foundPathTarget = joinToPath(this.relativeRoot.orThrow(), lastFoundTarget);
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
        if ( this.relativeRoot.isPresent() ) {
            this.relativeRoot.resetTo(joinToPath(this.relativeRoot.orThrow(), foundTarget));
        } else {
            this.relativeRoot.resetTo(foundTarget);
        }
        this.absoluteRoot.resetTo(joinToPath(this.absoluteRoot.orThrow(), foundTarget));   
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
