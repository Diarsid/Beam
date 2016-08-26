/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.os.dependable;

/**
 *
 * @author Diarsid
 */
public interface OSDependableRuntimeCommandComposer {
    
    String composeFileInPathWithProgramShellScript(
            String fileAbsolutePath, String programAbsolutePath);
    
    String composeUrlWithSpecifiedBrowserShellScript();
}
