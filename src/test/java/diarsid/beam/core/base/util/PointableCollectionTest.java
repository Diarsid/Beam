/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Diarsid
 */
public class PointableCollectionTest {
    
    private String defaultValue;
    private int capacity;
    private PointableCollection<String> pointable;
    
    @Before
    public void setUp() {
        defaultValue = "";
        capacity = 3;
        pointable = new PointableCollection<>(capacity, defaultValue);
    }

    @Test
    public void whenEmpty_alwaysReturnsDefault() {
        assertThat(pointable.toFirstAndGet(), equalTo(defaultValue));
        assertThat(pointable.toFirstAndGet(), equalTo(defaultValue));
        assertThat(pointable.toLastAndGet(), equalTo(defaultValue));
        assertThat(pointable.toLastAndGet(), equalTo(defaultValue));
    }
    
    @Test
    public void whenNotEmpty_traversingBetweenFirstAndLast() {
        pointable.add("1");
        pointable.add("2");
        pointable.add("3");
        
        assertThat(pointable.toFirstAndGet(), equalTo("3"));
        assertThat(pointable.toFirstAndGet(), equalTo("3"));
        assertThat(pointable.toFirstAndGet(), equalTo("3"));
        
        assertThat(pointable.toLastAndGet(), equalTo("2"));
        assertThat(pointable.toLastAndGet(), equalTo("1"));
        assertThat(pointable.toLastAndGet(), equalTo("1"));
        
        assertThat(pointable.toFirstAndGet(), equalTo("2"));
        assertThat(pointable.toFirstAndGet(), equalTo("3"));
        assertThat(pointable.toFirstAndGet(), equalTo("3"));
    }

    @Test
    public void whenOverflow_retainsCapacityWhileRemovingOlderValues() {
        pointable.add("0");
        pointable.add("1");
        pointable.add("2");
        pointable.add("3");
        pointable.add("4");
        
        assertThat(pointable.size(), equalTo(capacity));
        
        assertThat(pointable.toLastAndGet(), equalTo("4"));
        assertThat(pointable.toLastAndGet(), equalTo("3"));
        assertThat(pointable.toLastAndGet(), equalTo("2"));
        assertThat(pointable.toLastAndGet(), equalTo("2"));
        assertThat(pointable.toLastAndGet(), equalTo("2"));
        
        assertThat(pointable.toFirstAndGet(), equalTo("3"));
        assertThat(pointable.toFirstAndGet(), equalTo("4"));
    }

    @Test
    public void whenAppended_refreshPointerToNewestValue() {
        pointable.add("1");
        pointable.add("2");
        pointable.add("3");
        
        assertThat(pointable.toLastAndGet(), equalTo("3"));
        assertThat(pointable.toLastAndGet(), equalTo("2"));
        assertThat(pointable.toLastAndGet(), equalTo("1"));
        
        pointable.add("4");
        
        assertThat(pointable.toLastAndGet(), equalTo("4"));
    }
    
     @Test
    public void whenAppendedAlreadyExistingValue_becamesFirst_refreshPointerToNewestValue() {
        pointable.add("1");
        pointable.add("2");
        pointable.add("3");
        pointable.add("2");
        
        assertThat(pointable.toLastAndGet(), equalTo("2"));
        assertThat(pointable.toLastAndGet(), equalTo("3"));
        assertThat(pointable.toLastAndGet(), equalTo("1"));
    }    
    
}
