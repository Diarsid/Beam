/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.advanced;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import diarsid.beam.core.base.analyze.variantsweight.WeightEstimate;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariant;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.os.treewalking.base.FolderTypeDetector;
import diarsid.beam.core.base.os.treewalking.search.FileSearchMode;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.LocationSubPath;

import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.analyze.similarity.Similarity.isSimilar;
import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightVariantsList;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.GOOD;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.MODERATE;
import static diarsid.beam.core.base.analyze.variantsweight.WeightEstimate.PERFECT;
import static diarsid.beam.core.base.analyze.variantsweight.WeightedVariants.unite;
import static diarsid.beam.core.base.control.flow.FlowResult.FAIL;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowCompleted;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.control.io.base.interaction.Help.asHelp;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.os.treewalking.search.FileSearchMode.ALL;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.PathUtils.extractLastElementFromPath;
import static diarsid.beam.core.base.util.PathUtils.normalizeSeparators;
import static diarsid.beam.core.base.util.PathUtils.removeSeparators;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.haveEqualLength;
import static diarsid.beam.core.base.util.StringUtils.isEmpty;
import static diarsid.beam.core.base.util.StringUtils.lower;


/**
 *
 * @author Diarsid
 */
public class FileWalker {
    
    private static final int UNVISITED_LEVEL = -1;
    
    private final FolderTypeDetector folderTypeDetector;
    private final InnerIoEngine ioEngine;
    private final ThreadLocal<Initiator> localInitiator;
    private final ThreadLocal<String> localPattern;
    private final ThreadLocal<String> localRootString;
    private final ThreadLocal<Location> localLocation;
    private final ThreadLocal<LocationSubPath> localLocationSubPath;
    private final ThreadLocal<FileSearchMode> localMode;
    private final ThreadLocal<Integer> localMaxLevelsToVisit;
    private final ThreadLocal<List<WeightedVariant>> localVariants;
    private final ThreadLocal<AtomicInteger> localVisitedLevel;
    private final ThreadLocal<ValueFlow<String>> localResultFlow;
    
    public FileWalker(InnerIoEngine ioEngine, FolderTypeDetector folderTypeDetector) {
        this.folderTypeDetector = folderTypeDetector;
        this.ioEngine = ioEngine;
        this.localVariants = new ThreadLocal<>();
        this.localVisitedLevel = new ThreadLocal<>();
        this.localInitiator = new ThreadLocal<>();
        this.localPattern = new ThreadLocal<>();
        this.localRootString = new ThreadLocal<>();
        this.localLocation = new ThreadLocal<>();
        this.localLocationSubPath = new ThreadLocal<>();
        this.localMode = new ThreadLocal<>();
        this.localMaxLevelsToVisit = new ThreadLocal<>();
        this.localResultFlow = new ThreadLocal<>();
    }
    
    private WeightEstimate weightEstimateAcceptableForCurrentLevel() {
        switch ( this.localVisitedLevel.get().get() ) {
            case 0:
                return PERFECT;
            case 1:
                return GOOD;
            default:
                return MODERATE; 
        }
    }
    
    public FileWalker in(String where) {
        this.localRootString.set(where);
        return this;
    }
    
    public FileWalker in(Location location) {
        this.localLocation.set(location);
        this.localRootString.set(location.path());
        return this;
    }
    
    public FileWalker in(LocationSubPath locationSubPath) {
        this.localLocationSubPath.set(locationSubPath);
        this.localRootString.set(locationSubPath.fullPath());
        return this;
    }
    
    public FileWalker by(Initiator initiator) {
        this.localInitiator.set(initiator);
        return this;
    }
    
    public FileWalker search(String pattern) {
        this.localPattern.set(pattern);
        return this;
    }
    
    public FileWalker withMaxDepthOf(int maxDepth) {
        this.localMaxLevelsToVisit.set(maxDepth);
        return this;
    }
    
    public FileWalker lookingFor(FileSearchMode mode) {
        this.localMode.set(mode);
        return this;
    }
    
    public ValueFlow<String> andGetResult() {        
        try {
            VoidFlow checkFlow = this.checkThreadLocalStateBeforeInitialization();
            if ( checkFlow.result().is(FAIL) ) {
                return valueFlowFail(checkFlow.message());
            }
            this.initializeThreadLocalState();
            this.walk();
            ValueFlow<String> resultFlow = this.localResultFlow.get();
            if ( nonNull(resultFlow) ) {
                return resultFlow;
            } else {
                return valueFlowCompletedEmpty();
            }
        } finally {
            this.cleanThreadLocalState();
        }
    }
    
    private VoidFlow checkThreadLocalStateBeforeInitialization() {
        if ( isNull(this.localInitiator.get()) ) {
            return voidFlowFail("initiator must be specified!");
        }
        if ( isEmpty(this.localPattern.get()) ) {
            return voidFlowFail("pattern must be specified!");
        }
        if ( isEmpty(this.localRootString.get()) ) {
            return voidFlowFail("root must be specified!");
        }   
        return voidFlowCompleted();
    }
    
//    public static void main(String[] args) {
//        FileWalker walker = new FileWalker(getFolderTypeDetector());
//        walker
//                .in("D:/WORK")
//                .search("jorihin")
//                .lookingFor(FOLDERS_ONLY)
////                .withMaxDepthOf(3)
//                .andGetResult();
//        
//        walker.localVariants.get()
//                .stream()
//                .sorted()
//                .map(variant -> format("%s : %s", variant.weight(), variant.text()))
//                .forEach(s -> System.out.println(s));
//    }
    
    private void walk() {
        final File root = new File(this.localRootString.get());
        this.localRootString.set(root.getAbsolutePath());
        
        if ( ! root.isDirectory() ) {
            this.localResultFlow.set(valueFlowFail(
                    format("%s is not a directory!", this.localRootString.get())));
            return;
        }
        
        List<String> collectedOnCurrentLevel = new ArrayList<>();
        List<File> currentLevel = new ArrayList<>();
        List<File> nextLevel = new ArrayList<>();
        boolean needToGoDeeper;
        
        addListedFilesTo(currentLevel, root.listFiles());        
        this.walkThroughCurrentLevel(currentLevel, collectedOnCurrentLevel, nextLevel);
        needToGoDeeper = this.consumeAndDefineIfGoDeeper(collectedOnCurrentLevel);
        
        Integer maxLevelsToVisit = this.localMaxLevelsToVisit.get();
        
        if ( nextLevel.isEmpty() || ( nonNull(maxLevelsToVisit) && maxLevelsToVisit == 0 ) ) {
            return;
        }
        
        List<File> levelsSwap;
        boolean canContinue = true;        
        AtomicInteger visitedLevel = this.localVisitedLevel.get();
        while ( needToGoDeeper && nonEmpty(nextLevel) && canContinue ) {            
            currentLevel.clear();
            levelsSwap = currentLevel;
            currentLevel = nextLevel;
            nextLevel = levelsSwap;
            
            this.walkThroughCurrentLevel(currentLevel, collectedOnCurrentLevel, nextLevel);            
            
            needToGoDeeper = this.consumeAndDefineIfGoDeeper(collectedOnCurrentLevel);
            if ( nonNull(maxLevelsToVisit) ) {
                canContinue = visitedLevel.get() < maxLevelsToVisit;
            }                    
        }
    }

    private void initializeThreadLocalState() {
        this.localVariants.set(new ArrayList<>());
        this.localVisitedLevel.set(new AtomicInteger(UNVISITED_LEVEL));
        if ( isNull(this.localMode.get()) ) {
            this.localMode.set(ALL);
        }
    }
    
    private void cleanThreadLocalState() {
        this.localVariants.remove();
        this.localVisitedLevel.remove();
        this.localInitiator.remove();
        this.localPattern.remove();
        this.localRootString.remove();
        this.localLocation.remove();
        this.localLocationSubPath.remove();
        this.localMode.remove();
        this.localMaxLevelsToVisit.remove();
        this.localResultFlow.remove();
    }
    
    private boolean consumeAndDefineIfGoDeeper(List<String> collectedOnCurrentLevel) {
        try {
            String processedFile;
            int rootLength = this.localRootString.get().length() + 1; // +1 to cut separator after root
            for (int i = 0; i < collectedOnCurrentLevel.size(); i++) {
                processedFile = collectedOnCurrentLevel
                        .get(i)
                        .substring(rootLength); 
                collectedOnCurrentLevel.set(i, processedFile);
            }  
            return this.consumeRefinedAndDefineIfGoDeeper(collectedOnCurrentLevel);
        } finally {
            collectedOnCurrentLevel.clear();
        }
    }
    
    private boolean consumeRefinedAndDefineIfGoDeeper(List<String> collectedOnCurrentLevel) {
//        collectedOnCurrentLevel
//                .stream()
//                .map(file -> level + ": " + file)
//                .forEach(file -> System.out.println(file));
        
        String pattern = this.localPattern.get();
        String processedFile;
        for (int i = 0; i < collectedOnCurrentLevel.size(); i++) {
            processedFile = collectedOnCurrentLevel.get(i);
            if ( ! fileIsSimilarToPattern(processedFile, pattern) ) {
                collectedOnCurrentLevel.remove(i);
                i--;
            }
        }
        
        if ( collectedOnCurrentLevel.isEmpty() ) {
            return true;
        }
        
        List<WeightedVariant> variants = this.localVariants.get();
        variants.addAll(weightVariantsList(pattern, stringsToVariants(collectedOnCurrentLevel)));
        
        if ( variants.isEmpty() ) {
            return true;
        }
        
        sort(variants);
        
        WeightEstimate acceptableWeightEstimate = this.weightEstimateAcceptableForCurrentLevel();
        if ( variants.get(0).hasEqualOrBetterWeightThan(acceptableWeightEstimate) ) {
            List<WeightedVariant> chosenVariants = new ArrayList<>();
            WeightedVariant variant;
            acceptableVariantsExtraction: for (int i = 0; i < variants.size(); i++) {
                variant = variants.get(i);
                if ( variant.hasEqualOrBetterWeightThan(acceptableWeightEstimate) ) {
                    chosenVariants.add(variant);
                    variants.remove(i);
                    i--;
                } else {
                    break acceptableVariantsExtraction;
                }
            }
            WeightedVariants weightedVariants = unite(chosenVariants);
            Help help = this.composeLocalHelp();
            Answer answer = this.ioEngine
                    .chooseInWeightedVariants(this.localInitiator.get(), weightedVariants, help);
            
            boolean ifGoDeeper = true;
            
            if ( answer.isGiven() ) {
                this.localResultFlow.set(valueFlowCompletedWith(answer.text()));
                ifGoDeeper = false;
            } else if ( answer.isRejection() ) {
                this.localResultFlow.set(valueFlowStopped());
                ifGoDeeper = false;
            }             
            return ifGoDeeper;
        } 
        
        return true;
    }
    
    private Help composeLocalHelp() {
        String where;
        Location location = this.localLocation.get();
        if ( nonNull(location) ) {
            where = location.name();
        } else {
            LocationSubPath locationSubPath = this.localLocationSubPath.get();
            if ( nonNull(locationSubPath) ) {
                where = locationSubPath.fullPath();
            } else {
                where = this.localRootString.get();
            }
        }
        return asHelp(
                format("Choose variant for '%s' in %s", this.localPattern.get(), where), 
                "Use:", 
                "   - number of target to choose it",
                "   - part of target name to choose it",
                "   - 'n' or 'no' to see another variants, if any",
                "   - dot (.) to reject all variants"
        );
    }

    private void walkThroughCurrentLevel(
            List<File> currentLevel, List<String> collectedOnCurrentLevel, List<File> nextLevel) {
        FileSearchMode mode = this.localMode.get();
        for (File file : currentLevel) {
            if ( mode.correspondsTo(file) ) {
                collectedOnCurrentLevel.add(normalizeSeparators(file.getAbsolutePath()));
            }
            if ( this.canEnterIn(file) ) {
                addListedFilesTo(nextLevel, file.listFiles());
            }
        }
        this.localVisitedLevel.get().incrementAndGet();
    }
    
    static void addListedFilesTo(List<File> whereToAdd, File[] filesToAdd) {
        if ( filesToAdd.length == 0 ) {
            return;
        }
        for (File file : filesToAdd) {
            whereToAdd.add(file);
        }
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
