/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.os.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import diarsid.beam.core.os.search.result.FileSearchResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static diarsid.beam.core.os.search.FileSearchMode.ALL;
import static diarsid.beam.core.os.search.FileSearchMode.FILES_ONLY;


/**
 *
 * @author Diarsid
 */
//@Ignore
public class FileSearcherServiceTest {
    
    private static final FileSearcherService searcher;
    private static final String root;
    static {
        FileSearchByPathPatternReusableFileVisitor visitorByPath = 
                new FileSearchByPathPatternReusableFileVisitor();
        FileSearchByNamePatternReusableFileVisitor visitorByName = 
                new FileSearchByNamePatternReusableFileVisitor();
        
        searcher = new FileSearcherService(3, 5, visitorByName, visitorByPath);
        
        root = Paths.get("./temp").normalize().toAbsolutePath().toString();
        System.out.println("[FileIntelligentSearcherTest] ROOT: " + root);
    }

    public FileSearcherServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        List<String> items = new ArrayList<String>();
        items.add("./temp");
        //items.add("./temp/file_aAa.txt");
        items.add("./temp/folder_1");
        items.add("./temp/folder_1/file_1.txt");
        items.add("./temp/folder_1/file_2.txt");
        items.add("./temp/folder_1/AAaaDir");
        items.add("./temp/folder_1/inner");
        items.add("./temp/folder_1/inner/nested");
        items.add("./temp/folder_1/inner/aAAaa.txt");
        items.add("./temp/folder_1/inner/bbbb.txt");
        items.add("./temp/folder_1/inner/aaabbbzzz.txt");
        items.add("./temp/folder_1/inner/nested/XXxx.txt");
        items.add("./temp/folder_1/inner/nested/xXXXx.txt");
        items.add("./temp/folder_1/inner/nested/yyyAAA.txt");
        items.add("./temp/second/one/two");
        items.add("./temp/second/one/two/yyyAAA.txt");
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
        
        for (String path : items) {
            assertTrue(Files.exists(Paths.get(path)));
        }
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
        FileSearchResult result = searcher.find("file_1", root, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String file = result.success().getFoundFile();
                assertTrue(file.contains("folder_1/file_1.txt"));
            } else {
                fail();
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindTarget_findFile_withWildcard_success() {
        FileSearchResult result = searcher.find("fi-1", root, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String file = result.success().getFoundFile();
                assertTrue(file.contains("folder_1/file_1.txt"));
            } else {
                fail();
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindTarget_findFolder_withoutWildcard_success() {
        FileSearchResult result = searcher.find("inn", root, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String file = result.success().getFoundFile();
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
        FileSearchResult result = searcher.find("in-r", root, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String file = result.success().getFoundFile();
                assertTrue(file.equals("folder_1/inner"));
            } else {
                fail();
            }
        } else {
            fail();
        }
        System.out.println("passed");
    }
    
    @Test
    public void testFindTarget_findMultipeFile_withoutWildcard_success() {
        FileSearchResult result = searcher.find("file", root, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                fail();
            } else {
                List<String> files = result.success().getMultipleFoundFiles();
                assertTrue(files.contains("folder_1/file_1.txt"));
                assertTrue(files.contains("folder_1/file_2.txt"));
                assertTrue(files.contains("file_z.txt"));
                assertTrue(files.size() == 3);
            }
        } else {
            fail();
        }
    }

    @Test
    public void testFindTarget_findMultipeFile_withoutWildcard_ingoreCase_success() {
        FileSearchResult result = searcher.find("aaa", root, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                fail();
            } else {
                List<String> files = result.success().getMultipleFoundFiles();
                assertTrue(files.contains("folder_1/AAaaDir"));
                assertTrue(files.contains("folder_1/inner/aAAaa.txt"));
                assertTrue(files.contains("folder_1/inner/aaabbbzzz.txt"));
                assertTrue(files.size() == 3);
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindTarget_findSingleFile_withWildcard_failure() {
        FileSearchResult result = searcher.find("fold-ile", root, ALL);
        if ( result.isOk() ) {
            fail();
        } else {
            if ( result.failure().targetNotFound() ) {
                assertTrue(true);
                System.out.println("passed");
            }
            if ( result.failure().hasTargetInvalidMessage() ) {
                fail();
            }
            if ( result.failure().locationNotFound() ) {
                fail();
            }
            if ( result.failure().targetNotAccessible() ) {
                fail();
            }
        }
    }    
    
    @Test
    public void testFindTarget_byPath_findFile_withoutWildcard_success() {
        FileSearchResult result = searcher.find("inn/yy", root, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String file = result.success().getFoundFile();
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
        FileSearchResult result = searcher.find("inn/y-a", root, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String file = result.success().getFoundFile();
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
    public void testFindTarget_byPath2_findFile_withWildcard_success() {
        FileSearchResult result = searcher.find("inn/es/y-a", root, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String file = result.success().getFoundFile();
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
        FileSearchResult result = searcher.find("inn/fold/y-a", root, ALL);
        if ( result.isOk() ) {
            fail();
        } else {
            if ( result.failure().targetNotFound() ) {
                assertTrue(true);
                System.out.println("passed");
            }
            if ( result.failure().hasTargetInvalidMessage() ) {
                fail();
            }
            if ( result.failure().locationNotFound() ) {
                fail();
            }
            if ( result.failure().targetNotAccessible() ) {
                fail();
            }
        }        
    } 
    
    @Test
    public void testFindTarget_findMultipeFile_byPath_withoutWildcard_ingoreCase_success() {
        FileSearchResult result = searcher.find("IN/xx", root, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                fail();
            } else {
                List<String> files = result.success().getMultipleFoundFiles();
                assertTrue(files.contains("folder_1/inner/nested/XXxx.txt"));
                assertTrue(files.contains("folder_1/inner/nested/xXXXx.txt"));
                assertTrue(files.size() == 2);
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindTarget_findMultipeFile_byPath_withWildcard_ingoreCase_success() {
        FileSearchResult result = searcher.find("I-eR/xx", root, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                fail();
            } else {
                List<String> files = result.success().getMultipleFoundFiles();
                assertTrue(files.contains("folder_1/inner/nested/XXxx.txt"));
                assertTrue(files.contains("folder_1/inner/nested/xXXXx.txt"));
                assertTrue(files.size() == 2);
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindTarget_findSingleFile_byPathWithFollowedSeparator_withoutWildcard_ingoreCase_failure() {
        FileSearchResult result = searcher.find("iNNer/", root, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String file = result.success().getFoundFile();
                assertEquals("folder_1/inner", file);
            } else {
                fail();
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindTarget_findSingleFile_byPathWithLeadSeparator_withoutWildcard_ingoreCase_failure() {
        FileSearchResult result = searcher.find("/iNNer", root, ALL);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String file = result.success().getFoundFile();
                assertEquals("folder_1/inner", file);
            } else {
                fail();
            }
        } else {
            fail();
        }
    }
    
    @Test
    public void testFindStrictly_success() {
        FileSearchResult result = searcher.findStrictly("filE_Z.txt", root, FILES_ONLY);
        assertTrue(result.isOk());        
    }
    
    @Test
    public void testFindStrictly_fail() {
        FileSearchResult result = searcher.findStrictly("filE_.txt", root, FILES_ONLY);
        assertFalse(result.isOk());        
    }
}