/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import diarsid.beam.core.modules.executor.os.search.result.FileSearchResult;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 *
 * @author Diarsid
 */
//@Ignore
public class FileIntelligentSearcherTest {
    
    private static final FileIntelligentSearcher searcher;
    private static final String root;
    static {
        FileSearchByPathPatternReusableFileVisitor visitorByPath = 
                new FileSearchByPathPatternReusableFileVisitor();
        FileSearchByNamePatternReusableFileVisitor visitorByName = 
                new FileSearchByNamePatternReusableFileVisitor();
        
        searcher = new FileIntelligentSearcher(3, visitorByName, visitorByPath);
        
        root = Paths.get("./temp").normalize().toAbsolutePath().toString();
        System.out.println("[FileIntelligentSearcherTest] ROOT: " + root);
    }

    public FileIntelligentSearcherTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        List<String> items = new ArrayList<String>();
        items.add("./temp");
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
     * Test of findTarget method, of class FileIntelligentSearcher.
     */
    @Test
    public void testFindTarget_findFile_withoutWildcard_success() {
        FileSearchResult result = searcher.findTarget("file_1", root);
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
        FileSearchResult result = searcher.findTarget("fi-1", root);
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
        FileSearchResult result = searcher.findTarget("inn", root);
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
        FileSearchResult result = searcher.findTarget("in-r", root);
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
        FileSearchResult result = searcher.findTarget("file", root);
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
        FileSearchResult result = searcher.findTarget("aaa", root);
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
        FileSearchResult result = searcher.findTarget("fold-ile", root);
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
        FileSearchResult result = searcher.findTarget("inn/yy", root);
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
        FileSearchResult result = searcher.findTarget("inn/y-a", root);
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
        FileSearchResult result = searcher.findTarget("inn/es/y-a", root);
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
    public void testFindTarget_byPath_findFile_withWildcard_failure() {
        FileSearchResult result = searcher.findTarget("inn/fold/y-a", root);
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
        FileSearchResult result = searcher.findTarget("IN/xx", root);
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
        FileSearchResult result = searcher.findTarget("I-eR/xx", root);
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
}