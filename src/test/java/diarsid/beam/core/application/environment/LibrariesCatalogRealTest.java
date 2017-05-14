/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Diarsid
 */
public class LibrariesCatalogRealTest {
    
    private final static LibrariesCatalog libraries = new LibrariesCatalogReal(
            System.getProperty("basedir") + "/target/classes/bin",
            System.getProperty("basedir") + "/target/classes/lib");

    public LibrariesCatalogRealTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    /**
     * Test of getLibraries method, of class LibrariesCatalogReal.
     */
    @Test
    public void testGetLibraries() {
        libraries.getLibraries().forEach(System.out::println);
    }

}