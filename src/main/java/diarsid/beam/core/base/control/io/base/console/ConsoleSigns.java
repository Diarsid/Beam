/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console;

/**
 *
 * @author Diarsid
 */
public class ConsoleSigns {
    
    public final static String SIGN_OF_FOLDER = "[_] ";
    public final static String SIGN_OF_FILE = " -  ";
    public final static String SIGN_OF_TOO_LARGE = " ...too large";
    public final static String SIGN_OF_TOO_LONG = "...";
    
    public static String removeFolderSign(String line) {
        return line.trim().substring(4);
    }
    
    public static String removeFileSign(String line) {
        return line.trim().substring(3);
    }
}
