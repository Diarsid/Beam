/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static java.util.Arrays.asList;

import static org.junit.Assert.*;

import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsAnySnippetsInAnyWordsIgnoreCase;

/**
 *
 * @author Diarsid
 */
public class StringIgnoreCaseUtilTest {

    public StringIgnoreCaseUtilTest() {
    }

    /**
     * Test of containsWordInIgnoreCase method, of class StringIgnoreCaseUtil.
     */
    
    @Test
    public void testContainsAllPartsIgnoreCase_String_String_contains() {        
        String searched = "aA-cD-Bb";
        String whereToSearch = "aaaAAAbbbBBBcccCCCdddDDD";
        
        boolean expResult = true;
        boolean result = StringIgnoreCaseUtil.containsAllPartsIgnoreCase(whereToSearch, searched);
        
        assertEquals(expResult, result);
    }
    
    @Test
    public void testContainsIgnoreCase_String_String_contains() {        
        String searched = "fraGMent";
        String whereToSearch = "another string-FRagMeNT-contained";
        
        boolean expResult = true;
        boolean result = StringIgnoreCaseUtil.containsIgnoreCase(whereToSearch, searched);
        
        assertEquals(expResult, result);
    }
    
    @Test
    public void testContainsIgnoreCase_String_String_not_contains() {        
        String searched = "fraGMent";
        String whereToSearch = "another string FRag";
        
        boolean expResult = false;
        boolean result = StringIgnoreCaseUtil.containsIgnoreCase(whereToSearch, searched);
        
        assertEquals(expResult, result);
    }
    
    @Test
    public void testContainsIgnoreCase_Null_String_not_contains() {        
        String searched = null;
        String whereToSearch = "another string FRag";
        
        boolean expResult = false;
        boolean result = StringIgnoreCaseUtil.containsIgnoreCase(whereToSearch, searched);
        
        assertEquals(expResult, result);
    }
    
    @Test
    public void testContainsIgnoreCase_String_null_not_contains() {        
        String searched = "fraGMent";
        String whereToSearch = null;
        
        boolean expResult = false;
        boolean result = StringIgnoreCaseUtil.containsIgnoreCase(whereToSearch, searched);
        
        assertEquals(expResult, result);
    }

    /**
     * Test of containsWordInIgnoreCase method, of class StringIgnoreCaseUtil.
     */
    @Test
    public void testContainsIgnoreCase_List_String() {
        List<String> whereToSearch = new ArrayList<>();
        whereToSearch.add("string");
        whereToSearch.add("anotherString");
        whereToSearch.add("seARchED");
        whereToSearch.add("one moreString");
        
        String searched = "searched";
        
        boolean expected = true;
        boolean result = StringIgnoreCaseUtil.containsWordInIgnoreCase(whereToSearch, searched);
        
        assertEquals(expected, result);
    }
    
    @Test
    public void testContainsIgnoreCase_List_String_not_contained() {
        List<String> whereToSearch = new ArrayList<>();
        whereToSearch.add("string");
        whereToSearch.add("anotherString");
        whereToSearch.add("one moreString");
        
        String searched = "searched";
        
        boolean expected = false;
        boolean result = StringIgnoreCaseUtil.containsWordInIgnoreCase(whereToSearch, searched);
        
        assertEquals(expected, result);
    }
    
    @Test
    public void testContainsIgnoreCase_List_String_emptyList() {
        List<String> whereToSearch = new ArrayList<>();
        
        String searched = "searched";
        
        boolean expected = false;
        boolean result = StringIgnoreCaseUtil.containsWordInIgnoreCase(whereToSearch, searched);
        
        assertEquals(expected, result);
    }
    
    @Test
    public void testContainsIgnoreCase_List_String_nullList() {
        List<String> whereToSearch = null;
        
        String searched = "searched";
        
        boolean expected = false;
        boolean result = StringIgnoreCaseUtil.containsWordInIgnoreCase(whereToSearch, searched);
        
        assertEquals(expected, result);
    }
    
    @Test
    public void testContainsIgnoreCase_List_String_nullSearched() {
        List<String> whereToSearch = new ArrayList<>();
        whereToSearch.add("string");
        whereToSearch.add("anotherString");
        whereToSearch.add("one moreString");
        
        String searched = null;
        
        boolean expected = false;
        boolean result = StringIgnoreCaseUtil.containsWordInIgnoreCase(whereToSearch, searched);
        
        assertEquals(expected, result);
    }
    
    /**
     * Test of containsSnippetIgnoreCase method, of class StringIgnoreCaseUtil.
     */
    @Test 
    public void testContainsSnippetIgnoreCase_contains() {
        List<String> whereToSearch = new ArrayList<>();
        whereToSearch.add("string");
        whereToSearch.add("anotherString");
        whereToSearch.add("one moreString");
        
        String searchedSnippet = "oth";
        
        boolean expected = true;
        boolean result = StringIgnoreCaseUtil.containsSnippetIgnoreCase(whereToSearch, searchedSnippet);
        assertEquals(expected, result);
    }
    
    @Test 
    public void testContainsSnippetIgnoreCase_does_not_contain() {
        List<String> whereToSearch = new ArrayList<>();
        whereToSearch.add("string");
        whereToSearch.add("anotherString");
        whereToSearch.add("one moreString");
        
        String searchedSnippet = "XXX";
        
        boolean expected = false;
        boolean result = StringIgnoreCaseUtil.containsSnippetIgnoreCase(whereToSearch, searchedSnippet);
        assertEquals(expected, result);
    }
    
    /**
     * Test of containsIgnoreCaseAnyFragment method, of class StringIgnoreCaseUtil.
     */
    
    @Test 
    public void testContainsIgnoreCaseAnyFragment_contains() {
        String whereToSearch = "long-specific-name";
        
        List<String> searchedSnippets = new ArrayList<>();        
        searchedSnippets.add("anotherString");
        searchedSnippets.add("one moreString");
        searchedSnippets.add("peCIf");
        
        boolean expected = true;
        boolean result = StringIgnoreCaseUtil.containsIgnoreCaseAnyFragment(whereToSearch, searchedSnippets);
        assertEquals(expected, result);
    }
    
    @Test 
    public void testContainsIgnoreCaseAnyFragment_does_not_contain() {
        String whereToSearch = "long-specific-name";
        
        List<String> searchedSnippets = new ArrayList<>();        
        searchedSnippets.add("anotherString");
        searchedSnippets.add("one moreString");
        searchedSnippets.add("no-matches");
        
        boolean expected = false;
        boolean result = StringIgnoreCaseUtil.containsIgnoreCaseAnyFragment(whereToSearch, searchedSnippets);
        assertEquals(expected, result);
    }    

    /**
     * Test of indexOfIgnoreCase method, of class StringIgnoreCaseUtil.
     */
    @Test
    public void testIndexOfIgnoreCase() {
        List<String> whereToSearch = new ArrayList<>();
        whereToSearch.add("string");        
        whereToSearch.add("looOooOOng string");
        whereToSearch.add("sEArchED");
        whereToSearch.add("anotherString");
        whereToSearch.add("one moreString");
        
        String searched = "SearCHed";
        
        int expResult = 2;
        int result = StringIgnoreCaseUtil.indexOfIgnoreCase(whereToSearch, searched);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIndexOfIgnoreCase_emptyList() {
        List<String> whereToSearch = new ArrayList<>();
        
        String searched = "SearCHed";
        
        int expResult = -1;
        int result = StringIgnoreCaseUtil.indexOfIgnoreCase(whereToSearch, searched);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIndexOfIgnoreCase_searchedIsNul() {
        List<String> whereToSearch = new ArrayList<>();
        whereToSearch.add("string");
        whereToSearch.add("sEArchED");
        whereToSearch.add("anotherString");
        whereToSearch.add("one moreString");
        
        String searched = null;
        
        int expResult = -1;
        int result = StringIgnoreCaseUtil.indexOfIgnoreCase(whereToSearch, searched);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIndexOfIgnoreCase_searchedNotContained() {
        List<String> whereToSearch = new ArrayList<>();
        whereToSearch.add("string");
        whereToSearch.add("anotherString");
        whereToSearch.add("one moreString");
        
        String searched = "SearCHed";
        
        int expResult = -1;
        int result = StringIgnoreCaseUtil.indexOfIgnoreCase(whereToSearch, searched);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIndexOfIgnoreCase_listIsNull() {
        List<String> whereToSearch = null;
        
        String searched = "SearCHed";
        
        int expResult = -1;
        int result = StringIgnoreCaseUtil.indexOfIgnoreCase(whereToSearch, searched);
        assertEquals(expResult, result);
    }

    /**
     * Test of containsKeyIgnoreCase method, of class StringIgnoreCaseUtil.
     */
    @Test
    public void testContainsKeyIgnoreCase() {
        String ignoreCaseKey = "searChED IGNoreCaSE kEy";
        
        Map<String, Object> mapToSearch = new HashMap<>();
        mapToSearch.put("one key", "value");
        mapToSearch.put("another_key", "another value");
        mapToSearch.put("searched ignorecase KeY", "searched value");
        mapToSearch.put("one more key", "one more value");
        
        boolean expResult = true;
        boolean result = StringIgnoreCaseUtil.containsKeyIgnoreCase(mapToSearch, ignoreCaseKey);
        assertEquals(expResult, result);        
    }
    
    @Test
    public void testContainsKeyIgnoreCase_keyIsNotPresent() {
        String ignoreCaseKey = "searChED IGNoreCaSE kEy";
        
        Map<String, Object> mapToSearch = new HashMap<>();
        mapToSearch.put("one key", "value");
        mapToSearch.put("another_key", "another value");
        mapToSearch.put("one more key", "one more value");
        
        boolean expResult = false;
        boolean result = StringIgnoreCaseUtil.containsKeyIgnoreCase(mapToSearch, ignoreCaseKey);
        assertEquals(expResult, result);        
    }
    
    @Test
    public void testContainsKeyIgnoreCase_keyIsNull() {
        String ignoreCaseKey = null;
        
        Map<String, Object> mapToSearch = new HashMap<>();
        mapToSearch.put("one key", "value");
        mapToSearch.put("another_key", "another value");
        mapToSearch.put("searched ignorecase KeY", "searched value");
        mapToSearch.put("one more key", "one more value");
        
        boolean expResult = false;
        boolean result = StringIgnoreCaseUtil.containsKeyIgnoreCase(mapToSearch, ignoreCaseKey);
        assertEquals(expResult, result);        
    }
    
    @Test
    public void testContainsKeyIgnoreCase_mapIsEmpty() {
        String ignoreCaseKey = "searChED IGNoreCaSE kEy";
        
        Map<String, Object> mapToSearch = new HashMap<>();
        
        boolean expResult = false;
        boolean result = StringIgnoreCaseUtil.containsKeyIgnoreCase(mapToSearch, ignoreCaseKey);
        assertEquals(expResult, result);        
    }
    
    @Test
    public void testContainsKeyIgnoreCase_mapIsNull() {
        String ignoreCaseKey = "searChED IGNoreCaSE kEy";
        
        Map<String, Object> mapToSearch = null;
        
        boolean expResult = false;
        boolean result = StringIgnoreCaseUtil.containsKeyIgnoreCase(mapToSearch, ignoreCaseKey);
        assertEquals(expResult, result);        
    }

    /**
     * Test of getIgnoreCase method, of class StringIgnoreCaseUtil.
     */
    @Test
    public void testGetIgnoreCase() {
        String searchedValue = "target_value";
        String ignoreCaseKey = "searChED IGNoreCaSE kEy";
        
        Map<String, String> mapToSearch = new HashMap<>();
        mapToSearch.put("one key", "value");
        mapToSearch.put("another_key", "another value");
        mapToSearch.put("searched ignorecase KeY", searchedValue);
        mapToSearch.put("one more key", "one more value");
        
        String result = StringIgnoreCaseUtil.getIgnoreCase(mapToSearch, ignoreCaseKey);
        assertEquals(searchedValue, result);
    }
    
    @Test
    public void testGetIgnoreCase_notContained() {
        String ignoreCaseKey = "searChED IGNoreCaSE kEy";
        
        Map<String, String> mapToSearch = new HashMap<>();
        mapToSearch.put("one key", "value");
        mapToSearch.put("another_key", "another value");
        mapToSearch.put("one more key", "one more value");
        
        String expected = null;
        String result = StringIgnoreCaseUtil.getIgnoreCase(mapToSearch, ignoreCaseKey);
        assertEquals(expected, result);
    }
    
    @Test
    public void testGetIgnoreCase_mapIsEmpty() {
        String ignoreCaseKey = "searChED IGNoreCaSE kEy";
        
        Map<String, String> mapToSearch = new HashMap<>();
        
        String expected = null;
        String result = StringIgnoreCaseUtil.getIgnoreCase(mapToSearch, ignoreCaseKey);
        assertEquals(expected, result);
    }
    
    @Test
    public void testGetIgnoreCase_keyIsNull() {
        String ignoreCaseKey = null;
        
        Map<String, String> mapToSearch = new HashMap<>();
        mapToSearch.put("one key", "value");
        mapToSearch.put("another_key", "another value");
        mapToSearch.put("searched ignorecase KeY", "searched value");
        mapToSearch.put("one more key", "one more value");
        
        String expected = null;
        String result = StringIgnoreCaseUtil.getIgnoreCase(mapToSearch, ignoreCaseKey);
        assertEquals(expected, result);
    }
    
    @Test
    public void testGetIgnoreCase_mapIsNull() {
        String ignoreCaseKey = "searChED IGNoreCaSE kEy";
        
        Map<String, String> mapToSearch = null;
        
        String expected = null;
        String result = StringIgnoreCaseUtil.getIgnoreCase(mapToSearch, ignoreCaseKey);
        assertEquals(expected, result);
    }
    
    @Test
    public void test_containsAnySnippetsInAnyWordsIgnoreCase_false() {
        List<String> whereToSearch = asList("argUMEeNT", "xxxXXxxX", "ComMANd");
        List<String> anyOf = asList("yyYYy", "@", "111");
        
        boolean notFound = containsAnySnippetsInAnyWordsIgnoreCase(whereToSearch, anyOf);
        assertFalse(notFound);
    }
    
    @Test
    public void test_containsAnySnippetsInAnyWordsIgnoreCase_true() {
        List<String> whereToSearch = asList("argUMEeNT", "xxxXXxxX", "ComMANd");
        List<String> anyOf = asList("maN", "@", "111");
        
        boolean found = containsAnySnippetsInAnyWordsIgnoreCase(whereToSearch, anyOf);
        assertTrue(found);
    }
}