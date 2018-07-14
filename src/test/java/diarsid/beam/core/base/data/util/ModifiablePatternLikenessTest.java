/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.data.util;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Diarsid
 */
public class ModifiablePatternLikenessTest {
    
    private final ModifiablePatternLikeness likeness;
    
    public ModifiablePatternLikenessTest() {
        likeness = new ModifiablePatternLikeness();
    }
    
    @Before
    public void setUp() {
        likeness.clear();
    }

    @Test
    public void testDecrease_2() {
        likeness.setPatternLength(2);
        
        assertThat(likeness.requiredMatches(), equalTo(2));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(2));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(2));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(2));
    }

    @Test
    public void testDecrease_3() {
        likeness.setPatternLength(3);
        
        assertThat(likeness.requiredMatches(), equalTo(2));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(2));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(2));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(2));
    }

    @Test
    public void testDecrease_4() {
        likeness.setPatternLength(4);
        
        assertThat(likeness.requiredMatches(), equalTo(3));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(3));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(3));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(3));
    }

    @Test
    public void testDecrease_5() {
        likeness.setPatternLength(5);
        
        assertThat(likeness.requiredMatches(), equalTo(4));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(3));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(3));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(3));
    }

    @Test
    public void testDecrease_6() {
        likeness.setPatternLength(6);
        
        assertThat(likeness.requiredMatches(), equalTo(5));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(4)); 
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(4));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(4));
    }
    
    @Test
    public void testDecrease_7() {
        likeness.setPatternLength(7);
        
        assertThat(likeness.requiredMatches(), equalTo(6));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(5));        
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(4));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(4));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(4));
    }
    
    @Test
    public void testDecrease_8() {
        likeness.setPatternLength(8);
        
        assertThat(likeness.requiredMatches(), equalTo(7));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(6));        
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(5));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(5));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(5));
    }
    
    @Test
    public void testDecrease_9() {
        likeness.setPatternLength(9);
        
        assertThat(likeness.requiredMatches(), equalTo(8));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(7));        
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(6));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(5));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(5));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(5));
    }
    
    @Test
    public void testDecrease_10() {
        likeness.setPatternLength(10);
        
        assertThat(likeness.requiredMatches(), equalTo(9));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(8));        
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(7));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(6));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(6));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(6));
    }
    
    @Test
    public void testDecrease_11() {
        likeness.setPatternLength(11);
        
        assertThat(likeness.requiredMatches(), equalTo(9));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(7));   
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(7));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(7));
    }

    @Test
    public void testDecrease_12() {
        likeness.setPatternLength(12);
        
        assertThat(likeness.requiredMatches(), equalTo(10));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(8));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(8));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(8));  
    }

    @Test
    public void testDecrease_13() {
        likeness.setPatternLength(13);
        
        assertThat(likeness.requiredMatches(), equalTo(11));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(9));
                        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(9));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(9));  
    }

    @Test
    public void testDecrease_14() {
        likeness.setPatternLength(14);
        
        assertThat(likeness.requiredMatches(), equalTo(12));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(10));
                
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(8));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(8));
        
        assertThat(likeness.isNextDecreaseMeaningfull(), equalTo(false));
        
        likeness.decrease();
        assertThat(likeness.requiredMatches(), equalTo(8));  
    }
    
}
