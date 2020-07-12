/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.advanced;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import diarsid.beam.core.application.environment.Catalog;
import diarsid.beam.core.base.analyze.variantsweight.Variant;
import diarsid.beam.core.base.analyze.variantsweight.WeightAnalyzeReal;
import diarsid.beam.core.base.analyze.variantsweight.WeightEstimate;
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

import static diarsid.beam.core.base.analyze.variantsweight.Reindexable.reindex;
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
import static diarsid.beam.core.base.control.io.base.interaction.VariantConversions.namedStringToVariant;
import static diarsid.beam.core.base.control.io.base.interaction.VariantConversions.namedStringsToVariants;
import static diarsid.beam.core.base.os.treewalking.advanced.WalkUtil.addListedFilesTo;
import static diarsid.beam.core.base.os.treewalking.base.FileSearchMode.FILES_AND_FOLDERS;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.findDepthOf;
import static diarsid.beam.core.base.util.PathUtils.joinToPath;
import static diarsid.beam.core.base.util.PathUtils.splitPathFragmentsFrom;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.endsIgnoreCase;
import static diarsid.support.strings.StringUtils.isEmpty;

/**
 *
 * @author Diarsid
 */
class WalkState extends PooledReusable {
    
    private static final int UNVISITED_LEVEL = 0;
            
    private final WeightAnalyzeReal analyze;
    private final WalkStartPlace place;
    private final List<Variant> variants;
    private final List<String> collectedOnCurrentLevelPathsWithoutRoot;
    private final List<String> collectedOnCurrentLevelPathsWithRoot;
    private /* non-final in order to swap levels with ease */ List<File> currentLevel;
    private /* non-final in order to swap levels with ease */ List<File> nextLevel;
    private Initiator initiator;
    private String pattern;
    private List<Variant> predefinedVariants;
    private double predefinedWeightLimit;
    private boolean hasPredefinedWeightLimit;
    private Boolean isPatternPath;
    private FileSearchMode mode;
    private Integer minLevelsToVisit;
    private Integer maxLevelsToVisit;
    private Integer visitedLevel;
    private ValueFlow<String> resultFlow;
    
    WalkState(WeightAnalyzeReal analyze) {
        super();
        this.analyze = analyze;
        this.place = new WalkStartPlace();
        this.variants = new ArrayList<>();
        this.collectedOnCurrentLevelPathsWithoutRoot = new ArrayList<>();
        this.collectedOnCurrentLevelPathsWithRoot = new ArrayList<>();
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
        if ( nonEmpty(this.predefinedVariants) ) {
            this.variants.addAll(this.predefinedVariants);
            
            if ( isNull(this.minLevelsToVisit) ) {
                int maxPredefinedVariantsDepth = this.predefinedVariants
                        .stream()
                        .mapToInt(pathVariant -> findDepthOf(pathVariant.nameOrValue()))
                        .max()
                        .orElse(0);
                this.minLevelsToVisit = maxPredefinedVariantsDepth;
            }            
        }
    }
    
    ValueFlow<String> resultFlow() {        
        return this.resultFlow;
    }

    @Override
    protected void clearForReuse() {
        this.place.clear();
        
        this.variants.clear();
        this.collectedOnCurrentLevelPathsWithoutRoot.clear();
        this.collectedOnCurrentLevelPathsWithRoot.clear();
        this.currentLevel.clear();
        this.nextLevel.clear();
        
        this.visitedLevel = UNVISITED_LEVEL;
        
        this.initiator = null;
        this.pattern = null;
        this.predefinedVariants = null;
        this.isPatternPath = null;
        this.mode = null;
        this.minLevelsToVisit = null;
        this.maxLevelsToVisit = null;
        this.resultFlow = null;
        
        this.predefinedWeightLimit = 0.0;
        this.hasPredefinedWeightLimit = false;
    }
    
    void setHowDeepToGoMin(int minDepth) {
        this.minLevelsToVisit = minDepth;
    }
    
    void setHowDeepToGoMax(int maxDepth) {
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
    
    void setPredefinedVariants(List<Variant> predefinedVariants) {
        this.predefinedVariants = predefinedVariants;
        this.predefinedWeightLimit = predefinedVariants
                .stream()
                .mapToDouble(Variant::weight)
                .max()
                .orElse(0.0);
        this.hasPredefinedWeightLimit = true;
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
    
    String root() {
        return this.place.absoluteRoot();
    }
    
    String rootName() {
        return this.place.absoluteRootName();
    }
    
    File absoluteRootAsFile() {
        return this.place.absoluteRootAsFile();
    }
    
    WeightEstimate weightEstimateAcceptableForCurrentLevel() {
        switch ( this.visitedLevel ) {
            case 0:
            case 1:
                return PERFECT;
            case 2:
                return GOOD;
            default:
                return MODERATE; 
        }
    }
    
    String pattern() {
        return this.pattern;
    }
    
    boolean isMaxDepthDefined() {
        return nonNull(this.maxLevelsToVisit);
    }
    
    Integer maxDepth() {
        return this.maxLevelsToVisit;
    }
    
    Initiator initiator() {
        return this.initiator;
    }
    
    String[] patternToPathFragments() {
        return splitPathFragmentsFrom(this.pattern);
    }
    
    int collectedOnCurrentLevelSize() {
        return this.collectedOnCurrentLevelPathsWithoutRoot.size();
    }
    
    String collectedOnCurrentLevelGet(int i) {
        return this.collectedOnCurrentLevelPathsWithoutRoot.get(i);
    }
    
    void collectedOnCurrentLevelAdd(String pathWithoutRoot, String pathWithRoot) {
        this.collectedOnCurrentLevelPathsWithoutRoot.add(pathWithoutRoot);
        this.collectedOnCurrentLevelPathsWithRoot.add(pathWithRoot);
    }
    
    void collectedOnCurrentLevelSet(int i, String path, String pathName) {
        this.collectedOnCurrentLevelPathsWithoutRoot.set(i, path);
        this.collectedOnCurrentLevelPathsWithRoot.set(i, pathName);
    }
    
    void collectedOnCurrentLevelRemove(int i) {
        this.collectedOnCurrentLevelPathsWithoutRoot.remove(i);
        this.collectedOnCurrentLevelPathsWithRoot.remove(i);
    }
    
    void collectedOnCurrentLevelClear() {
        this.collectedOnCurrentLevelPathsWithoutRoot.clear();
        this.collectedOnCurrentLevelPathsWithRoot.clear();
    }
    
    boolean collectedOnCurrentLevelNonEmpty() {
        return this.collectedOnCurrentLevelPathsWithoutRoot.size() > 0;
    }
    
    List<File> currentLevel() {
        return this.currentLevel;
    }
    
    List<File> nextLevel() {
        return this.nextLevel;
    }
    
    List<Variant> variants() {
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
        if ( hasOne(this.collectedOnCurrentLevelPathsWithoutRoot) ) {
            Variant variant = namedStringToVariant(
                    getOne(this.collectedOnCurrentLevelPathsWithoutRoot), 
                    getOne(this.collectedOnCurrentLevelPathsWithRoot));
            this.analyze
                    .weightVariant(this.pattern, variant)
                    .ifPresent(weightedVariant -> this.variants.add(weightedVariant));
        } else {
            List<Variant> pathVariants = namedStringsToVariants(
                    this.collectedOnCurrentLevelPathsWithoutRoot, 
                    this.collectedOnCurrentLevelPathsWithRoot);
            List<Variant> currentLevelVariants = this.analyze
                    .weightVariantsList(this.pattern, pathVariants);
            this.variants.addAll(currentLevelVariants);
        } 
        sort(this.variants);
        reindex(this.variants);
    }
    
    boolean doesFirstVariantMatchPatternExactly() {
        Variant variant = this.variants.get(0);
        String variantText = variant.value();
        return 
                variantText.equalsIgnoreCase(this.pattern) ||
                endsIgnoreCase(variantText, this.pattern);
    }
    
    boolean hasFirstVariantAcceptableWeightEstimate() {
        WeightEstimate acceptableWeightEstimate = this.weightEstimateAcceptableForCurrentLevel();
        return this.variants.get(0).hasEqualOrBetterWeightThan(acceptableWeightEstimate);
    }
    
    List<Variant> extractVariantsAcceptableOnCurrentLevel() {        
        List<Variant> extractedAcceptableVariants = new ArrayList<>();
        
        Predicate<Variant> acceptance;
        
        if ( this.hasPredefinedWeightLimit ) {
            acceptance = variant -> variant.weight() < this.predefinedWeightLimit;
        } else {
            WeightEstimate acceptableWeightEstimate = this.weightEstimateAcceptableForCurrentLevel();
            acceptance = variant -> variant.hasEqualOrBetterWeightThan(acceptableWeightEstimate);
        }
            
        Variant variant;
        acceptableVariantsExtraction: for (int i = 0; i < this.variants.size(); i++) {
            variant = this.variants.get(i);
            if ( acceptance.test(variant) ) {
                extractedAcceptableVariants.add(variant);
                this.variants.remove(i);
                i--;
            } else {
                break acceptableVariantsExtraction;
            }
        }
        
        if ( nonEmpty(extractedAcceptableVariants) ) {
            reindex(this.variants);
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
        return nonNull(this.maxLevelsToVisit) && this.maxLevelsToVisit == 0 ;
    }
    
    boolean ifCanGoDeeper() {
        return isNull(this.maxLevelsToVisit) || this.visitedLevel < this.maxLevelsToVisit;
    }
    
    boolean canStopAtCurrentLevel() {
        if ( isNull(this.minLevelsToVisit) ) {
            return true;
        } else {
            return this.visitedLevel >= this.minLevelsToVisit;
        }
    }
    
    boolean hasMinPredefinedWeight() {
        return this.hasPredefinedWeightLimit;
    }
    
    boolean isFirstCollectedVariantBetterThanMinPredefinedWeight() {
        if ( this.hasPredefinedWeightLimit ) {
            Variant bestPredefined = this.predefinedVariants.get(0);
            Variant bestCollected = this.variants.get(0);
            
            if ( bestCollected.doesNotEqual(bestPredefined) ) {
                return true;
            } else {
                Optional<Variant> betterCollected = this.variants
                        .stream()
                        .sorted()
                        .filter(variant -> ! this.predefinedVariants.contains(variant))
                        .findFirst();
                if ( betterCollected.isPresent() ) {
                    return betterCollected.get().weight() < this.predefinedWeightLimit;
                } else {
                    return false;
                }
            }
        } else {
            return this.variants.size() > 0;
        }
    }
    
    String reportCollectedOnCurrentLevel() {
        StringBuilder s = new StringBuilder();
        s.append("\n    paths collected on ").append(this.visitedLevel).append(" level:");
        for (String relativePath : this.collectedOnCurrentLevelPathsWithoutRoot) {
            s.append("\n").append("    ").append(relativePath);
        }
        return s.toString();
    }
    
    String reportWalkingQuery() {
        StringBuilder s = new StringBuilder();
        
        s.append("\n")
                .append("    FIND ").append(this.mode.name()).append("\n")
                .append("    LIKE PATTERN ").append(this.pattern).append("\n")
                .append("    IN ").append(this.place.absoluteRoot()).append("\n");
        if ( nonNull(this.minLevelsToVisit) ) {
            s.append("    WITH MIN DEPTH ").append(this.minLevelsToVisit).append("\n");
        }
        if ( this.isMaxDepthDefined() ) {
            s.append("    WITH MAX DEPTH ").append(this.maxLevelsToVisit).append("\n");
        }
        if ( nonEmpty(this.predefinedVariants) ) {
            s.append("    WITH PREDEFINED PATHS: ");
            for (Variant variant : this.predefinedVariants) {
                s.append("\n        weight: ")
                        .append(variant.weight())
                        .append(" ")
                        .append(variant.value());
            }
        }
        
        return s.toString();
    }
    
    String reportWalkingQueryOld() {
        String query;
        
        if ( this.isMaxDepthDefined() ) {
            query = format("\n" +
                "    FIND %s\n" +
                "    LIKE PATTERN %s\n" +
                "    IN %s\n" +
                "    WITH DEPTH %s", 
                this.mode, 
                this.pattern, 
                this.place.absoluteRoot(), 
                this.maxDepth());
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
