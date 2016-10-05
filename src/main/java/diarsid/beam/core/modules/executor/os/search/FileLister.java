/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.search;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import diarsid.beam.core.entities.local.Location;

/**
 *
 * @author Diarsid
 */
public class FileLister {
    
    private final FileListerReusableFileVisitor lister;
    
    public FileLister(FileListerReusableFileVisitor lister) {
        this.lister = lister;
    }
    
    public List<String> listContentOf(Location location, int depth) {
        return this.list(Paths.get(location.getPath()), depth);
    }
    
    public List<String> listContentOf(Path root, int depth) {      
        return this.list(root, depth);
    }
    
    private List<String> list(Path root, int depth) {        
        List<String> result = this.lister.reuseWithNew(root, depth);
        this.lister.clear();
        return result;
    }
}
