/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.base.builder;

/**
 *
 * @author Diarsid
 */
public class Tables {
    
    private final static DataBaseModel MODEL = new DataBaseModel();
    
    public static String commandChoicesTableScript() {
        return MODEL.getTable("command_choices").getSqlScript();
    }
    
    public static String actionChoicesTableScript() {
        return MODEL.getTable("action_choices").getSqlScript();
    }
}
