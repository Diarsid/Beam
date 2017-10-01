/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import static diarsid.beam.core.base.analyze.similarity.Similarity.hasStrictSimilar;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsWordInIgnoreCase;

/**
 *
 * @author Diarsid
 */
public class EnumUtils {
    
    private EnumUtils() {        
    }
    
    public static boolean argMatchesEnum(String arg, ParseableEnum parseableEnum) {
        return 
                arg.equalsIgnoreCase(parseableEnum.name()) || 
                containsWordInIgnoreCase(parseableEnum.keyWords(), arg) || 
                hasStrictSimilar(parseableEnum.keyWords(), arg);
    }
}
