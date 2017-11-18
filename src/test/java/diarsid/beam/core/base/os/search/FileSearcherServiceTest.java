/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.os.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import diarsid.beam.core.base.os.treewalking.search.FileSearcher;
import diarsid.beam.core.base.os.treewalking.search.result.FileSearchResult;

import static java.util.Arrays.asList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static diarsid.beam.core.base.os.treewalking.search.FileSearchMatching.DIRECT_MATCH;
import static diarsid.beam.core.base.os.treewalking.search.FileSearchMatching.PATTERN_MATCH;
import static diarsid.beam.core.base.os.treewalking.search.FileSearchMatching.SIMILAR_MATCH;
import static diarsid.beam.core.base.os.treewalking.search.FileSearchMatching.STRICT_MATCH;
import static diarsid.beam.core.base.os.treewalking.search.FileSearchMode.ALL;
import static diarsid.beam.core.base.os.treewalking.search.FileSearchMode.FILES_ONLY;
import static diarsid.beam.core.base.os.treewalking.search.FileSearcher.searcherWithDepthsOf;


/**
 *
 * @author Diarsid
 */
//@Ignore
public class FileSearcherServiceTest {
    
    private static final Logger logger = LoggerFactory.getLogger(FileSearcherServiceTest.class);
    
    private static final FileSearcher searcher;
    private static final String root;
    static {        
        searcher = searcherWithDepthsOf(5);
        
        root = Paths.get("./temp").normalize().toAbsolutePath().toString();
        System.out.println("[FileIntelligentSearcherTest] ROOT: " + root);
    }

    public FileSearcherServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        List<String> items = new ArrayList<String>();
        items.add("./temp");
        items.add("./temp/folder_1");
        items.add("./temp/second/one/two");
        items.add("./temp/second/one/two/yyyAAA.txt");
        items.add("./temp/folder_1/file_1.txt");
        items.add("./temp/folder_1/file_2.txt");
        items.add("./temp/folder_1/AAaaDir");
        items.add("./temp/folder_1/inner");
        items.add("./temp/folder_1/inner/nested");
        items.add("./temp/folder_1/inner/aAAaa.txt");
        items.add("./temp/folder_1/inner/bbbb.txt");
        items.add("./temp/folder_1/inner/aaabbbzzz.txt");
        items.add("./temp/folder_1/inner/nested/list_read.txt");
        items.add("./temp/folder_1/inner/nested/list_movie.txt");
        items.add("./temp/folder_1/inner/nested/yyyAAA.txt");
        items.add("./temp/file_z.txt");
        
        try {
            for (String path : items) {
                if ( path.contains(".txt") ) {
                    Files.createFile(Paths.get(path));
                } else {
                    Files.createDirectories(Paths.get(path));
                }
            }
        } catch (IOException e) {
            System.out.println("IOException during @BeaforeClass: " + e.getMessage());
        }
        
//        for (String path : items) {
//            assertTrue(Files.exists(Paths.get(path)));
//        }
    }

    @AfterClass
    public static void tearDownClass() {
        try {
            FileUtils.forceDelete(Paths.get("./temp").normalize().toFile());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Test of find method, of class FileSearcherService.
     */
    @Test
    public void testFindTarget_findFile_withoutWildcard_success() {
        FileSearchResult result = searcher.find("file_1", root, SIMILAR_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                fail();
            } else {
                List<String> files = result.success().foundFiles();
                assertTrue(files.contains("folder_1/file_1.txt"));
                assertTrue(files.contains("folder_1/file_2.txt"));
                assertTrue(files.contains("file_z.txt"));
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindTarget_findFile_withWildcard_success() {
        FileSearchResult result = searcher.find("foldaaa", root, SIMILAR_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                fail();                
            } else {
                List<String> expectedFiles = asList(
                        "folder_1/AAaaDir",      
                        "folder_1/inner/aAAaa.txt",
                        "folder_1/inner/aaabbbzzz.txt",
                        "folder_1/inner/nested/yyyAAA.txt"
                );
                List<String> foundFiles = result.success().foundFiles();                
                assertMatching(foundFiles, expectedFiles);
            }
        } else {
            fail();
        }
    }
    
    private static void assertMatching(List<String> foundFiles, List<String> expectedFiles) {
        for (String expected : expectedFiles) {
            assertTrue(foundFiles.contains(expected));
        }
        assertEquals(expectedFiles.size(), foundFiles.size());
    }
    
    @Test
    public void testFindTarget_findFolder_withoutWildcard_success() {
        FileSearchResult result = searcher.find("inn", root, PATTERN_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String file = result.success().foundFile();
                assertTrue(file.equals("folder_1/inner"));
            } else {
                fail();
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindTarget_findFolder_withWildcard_success() {
        FileSearchResult result = searcher.find("inr", root, SIMILAR_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                fail();                
            } else {                
                List<String> expectedFiles = asList(
                        "folder_1/inner",
                        "folder_1/inner/aAAaa.txt",
                        "folder_1/inner/aaabbbzzz.txt",                
                        "folder_1/inner/bbbb.txt", 
                        "folder_1/inner/nested",              
                        "folder_1/inner/nested/list_movie.txt",                
                        "folder_1/inner/nested/list_read.txt",                
                        "folder_1/inner/nested/yyyAAA.txt"
                );  
                List<String> foundFiles = result.success().foundFiles();
                assertMatching(foundFiles, expectedFiles);
            }
        } else {
            fail();
        }
        System.out.println("passed");
    }
    
    @Test
    public void testFindTarget_findMultipeFile_withoutWildcard_success() {
        FileSearchResult result = searcher.find("file", root, PATTERN_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                fail();
            } else {
                List<String> files = result.success().foundFiles();
                assertTrue(files.contains("folder_1/file_1.txt"));
                assertTrue(files.contains("folder_1/file_2.txt"));
                assertTrue(files.contains("file_z.txt"));
                assertEquals(3, files.size());
            }
        } else {
            fail();
        }
    }

    @Test
    public void testFindTarget_findMultipeFile_withoutWildcard_ingoreCase_success() {
        FileSearchResult result = searcher.find("aaa", root, PATTERN_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                fail();
            } else {
                List<String> files = result.success().foundFiles();
                assertTrue(files.contains("folder_1/AAaaDir"));
                assertTrue(files.contains("folder_1/inner/aAAaa.txt"));
                assertTrue(files.contains("folder_1/inner/aaabbbzzz.txt"));
                assertTrue(files.contains("folder_1/inner/nested/yyyAAA.txt"));
                assertTrue(files.contains("second/one/two/yyyAAA.txt"));
                assertEquals(5, files.size());
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindTarget_findSingleFile_withWildcard_failure() {
        FileSearchResult result = searcher.find("foldile", root, SIMILAR_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                fail();
            } else {
                List<String> expectedFiles = asList(   
                        "folder_1",
                        "folder_1/file_1.txt",
                        "folder_1/file_2.txt",
                        "folder_1/inner/nested/list_movie.txt",
                        "folder_1/inner/nested/list_read.txt"
                );
                List<String> foundFiles = result.success().foundFiles();
                assertMatching(foundFiles, expectedFiles);
            }
        } else {
            fail();
        }
    }    
    
    @Test
    public void testFindTarget_byPath_findFile_withoutWildcard_success() {
        FileSearchResult result = searcher.find("inn/yy", root, SIMILAR_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String file = result.success().foundFile();
                assertTrue(file.equals("folder_1/inner/nested/yyyAAA.txt"));
                System.out.println("passed");
            } else {
                fail();
            }
        } else {
            fail();
        }        
    }    
    
    @Test
    public void testFindTarget_byPath_findFile_withWildcard_success() {
        FileSearchResult result = searcher.find("inn/yatx", root, SIMILAR_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                fail();
            } else {      
                List<String> expectedFiles = asList(   
                        "folder_1/inner/aAAaa.txt",
                        "folder_1/inner/nested/yyyAAA.txt"
                );
                List<String> foundFiles = result.success().foundFiles();
                assertMatching(foundFiles, expectedFiles);
            }
        } else {
            fail();
        }        
    } 
    
    @Test
    public void testFindTarget_byPath2_findFile_withWildcard_success() {
        FileSearchResult result = searcher.find("inn/es/ya", root, SIMILAR_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String file = result.success().foundFile();
                assertTrue(file.equals("folder_1/inner/nested/yyyAAA.txt"));
            } else {
                fail();
            }
        } else {
            fail();
        }        
    } 
    
    @Test
    public void testFindTarget_byPath_findFile_withWildcard_failure() {
        FileSearchResult result = searcher.find("inn/fold/ya", root, SIMILAR_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                assertThat(result.success().foundFile(), equalTo("folder_1/inner/nested/yyyAAA.txt"));                
            } else {                
                fail();
            }
        } else {
            fail();
        }        
    } 
    
    @Test
    public void testFindTarget_findMultipeFile_byPath_withoutWildcard_ingoreCase_success() {
        FileSearchResult result = searcher.find("nst/list", root, SIMILAR_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                fail();
            } else {
                List<String> files = result.success().foundFiles();
                assertTrue(files.contains("folder_1/inner/nested/list_read.txt"));
                assertTrue(files.contains("folder_1/inner/nested/list_movie.txt"));
                assertEquals(2, files.size());
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindTarget_findMultipeFile_byPath_withWildcard_ingoreCase_success() {
        FileSearchResult result = searcher.find("IeR/list", root, SIMILAR_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                fail();
            } else {
                List<String> files = result.success().foundFiles();
                assertTrue(files.contains("folder_1/inner/nested/list_read.txt"));
                assertTrue(files.contains("folder_1/inner/nested/list_movie.txt"));
                assertEquals(2, files.size());
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindTarget_findMultipeFile_byPath() {
        FileSearchResult result = searcher.find("Iner/nst/ya", root, SIMILAR_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                assertTrue(result.success().foundFile().equals("folder_1/inner/nested/yyyAAA.txt"));
            } else {
                fail();
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindTarget_findSingleFile_byPathWithFollowedSeparator_withoutWildcard_ingoreCase_failure() {
        FileSearchResult result = searcher.find("iNNer/", root, PATTERN_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String file = result.success().foundFile();
                assertEquals("folder_1/inner", file);
            } else {
                result.success().foundFiles().forEach(System.out::println);
                fail();
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindTarget_findSingleFile_byPathWithLeadSeparator_withoutWildcard_ingoreCase_failure() {
        FileSearchResult result = searcher.find("/iNNer", root, PATTERN_MATCH, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String file = result.success().foundFile();
                assertEquals("folder_1/inner", file);
            } else {
                result.success().foundFiles().forEach(System.out::println);
                fail();
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindTarget_findFile_byCrossPathPattern() {
        FileSearchResult result = searcher.find("sectwo", root, SIMILAR_MATCH, FILES_ONLY);
        assertEquals(true, result.isOk());
    }
    
    @Test
    public void testFindDirectly_success() {
        FileSearchResult result = searcher.find("filE_Z.txt", root, DIRECT_MATCH, FILES_ONLY);
        assertTrue(result.isOk());
    }
    
    @Test
    public void testFindDirectly_fail() {
        FileSearchResult result = searcher.find("filE_.txt", root, DIRECT_MATCH, FILES_ONLY);
        assertFalse(result.isOk());        
    }
    
    @Test
    public void testFindDirectly_deep_fail() {
        FileSearchResult result = searcher.find("aAAaa.txt", root, DIRECT_MATCH, FILES_ONLY);
        assertFalse(result.isOk());
        
    }
    
    @Test
    public void testFindStrictly_deep_success() {
        FileSearchResult result = searcher.find("aaaaA.TXT", root, STRICT_MATCH, FILES_ONLY);
        assertTrue(result.isOk());
        String file = result.success().foundFile();
        assertEquals("folder_1/inner/aAAaa.txt", file);
    }
}