/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os;


public class WindowsDependableRuntimeCommandComposer implements OSDependableRuntimeCommandComposer {
    
    public WindowsDependableRuntimeCommandComposer() {
    }

    @Override
    public String composeFileInPathWithProgramShellScript(
            String fileAbsolutePath, String programAbsolutePath) {
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder
                .append("cmd /c start ")
                .append(programAbsolutePath)
                .append(" ")
                .append(fileAbsolutePath);
        return commandBuilder.toString();
    }

    @Override
    public String composeUrlWithSpecifiedBrowserShellScript() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
