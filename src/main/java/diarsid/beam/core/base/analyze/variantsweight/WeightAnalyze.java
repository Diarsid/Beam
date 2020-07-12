/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Diarsid
 */
public interface WeightAnalyze {

    boolean isSatisfiable(String pattern, String name);

    boolean isSatisfiable(String pattern, Variant variant);

    Variants weightStrings(String pattern, List<String> variants);

    Variants weightVariants(String pattern, List<Variant> variants);

    Variants weightStrings(String pattern, String noWorseThan, List<String> variants);

    Variants weightVariants(String pattern, String noWorseThan, List<Variant> variants);

    List<Variant> weightStringsList(String pattern, List<String> strings);

    List<Variant> weightStringsList(String pattern, String noWorseThan, List<String> strings);

    List<Variant> weightVariantsList(String pattern, List<Variant> variants);

    List<Variant> weightVariantsList(String pattern, String noWorseThan, List<Variant> variants);

    Optional<Variant> weightVariant(String pattern, Variant variant);
    
}
