/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

/**
 *
 * @author Diarsid
 */
public interface WeightConsumer {
    
    void accept(int index, double weight, WeightElement weightElement);
}
