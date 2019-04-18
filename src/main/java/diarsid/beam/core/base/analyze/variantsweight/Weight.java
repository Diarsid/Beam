/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.MIN_VALUE;
import static java.util.Arrays.fill;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.logAnalyze;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.POSITIONS_CLUSTERS;
import static diarsid.beam.core.base.analyze.variantsweight.WeightElement.WeightType.CALCULATED;
import static diarsid.beam.core.base.analyze.variantsweight.WeightElement.WeightType.PREDEFINED;

/**
 *
 * @author Diarsid
 */
class Weight {
    
    private final static int SIZE = 128;
    public final static double WEIGHT_UNINITIALIZED = MIN_VALUE;
    
    private final List<WeightElement> elements;
    private final double[] weights;
    private double weightSum;
    private int nextFreeWeightIndex;

    public Weight() {
        this.elements = new ArrayList<>();
        this.weights = new double[SIZE];
        this.nextFreeWeightIndex = 0;
    }
    
    void add(WeightElement weightElement) {
        weightElement.weightTypeMustBe(PREDEFINED);
        this.elements.add(weightElement);
        this.weights[this.nextFreeWeightIndex] = weightElement.predefinedWeight();
        this.weightSum = this.weightSum + weightElement.predefinedWeight();
        this.nextFreeWeightIndex++;
        logAnalyze(
                POSITIONS_CLUSTERS, 
                "               [weight] %1$+.2f : %2$s", 
                weightElement.predefinedWeight(), weightElement.description());
    }
    
    void add(double calculatedWeight, WeightElement element) {
        element.weightTypeMustBe(CALCULATED);
        this.elements.add(element);
        this.weights[this.nextFreeWeightIndex] = calculatedWeight;
        this.weightSum = this.weightSum + calculatedWeight;
        this.nextFreeWeightIndex++;
        logAnalyze(
                POSITIONS_CLUSTERS, 
                "               [weight] %1$+.2f : %2$s", calculatedWeight, element.description());
    }
    
    boolean contains(WeightElement weightElement) {
        return this.elements.contains(weightElement);
    }
    
    void clear() {
        this.elements.clear();
        fill(this.weights, WEIGHT_UNINITIALIZED);
        this.weightSum = 0;
        this.nextFreeWeightIndex = 0;
    }
    
    double sum() {
        return this.weightSum;
    }
    
    int length() {
        return this.nextFreeWeightIndex;
    }
    
    void observeAll(WeightConsumer weightConsumer) {
        for (int i = 0; i < this.nextFreeWeightIndex; i++) {
            weightConsumer.accept(i, this.weights[i], this.elements.get(i));
        }
    }
    
    void exclude(WeightElement element) {
        int i = this.elements.indexOf(element);
        if ( i > -1 ) {
            excludeByIndex(i);
        }
    }

    private void excludeByIndex(int i) {
        double excludedWeight = this.weights[i];
        this.weights[i] = WEIGHT_UNINITIALIZED;
        this.weightSum = this.weightSum - excludedWeight;
    }
    
    void excludeIfAllPresent(WeightElement element1, WeightElement element2) {
        int i1 = this.elements.indexOf(element1);
        if ( i1 < 0 ) {
            return;
        } 
        
        int i2 = this.elements.indexOf(element2);
        if ( i2 < 0 ) {
            return;
        } 
        
        excludeByIndex(i1);
        excludeByIndex(i2);
    }
}
