/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.os.listing;

import java.nio.file.Path;

/**
 *
 * @author Diarsid
 */
public interface FeatureFolderDetector {
    
    boolean examine(Path folder);
}
