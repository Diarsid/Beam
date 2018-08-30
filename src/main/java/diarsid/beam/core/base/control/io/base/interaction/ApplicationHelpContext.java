/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Diarsid
 */
public class ApplicationHelpContext {
    
    private final Map<HelpKey, HelpInfo> helps;
    private final Random random;

    public ApplicationHelpContext() {
        this.helps = new HashMap<>();
        this.random = new Random();
    }
    
    public HelpKey add(List<String> helpInfo) {
        HelpInfo help = new HelpInfo(helpInfo);
        HelpKey key = new HelpKey(this.random.nextInt());
        this.helps.put(key, help);
        return key;
    }
    
    public HelpInfo get(HelpKey key) {
        return this.helps.get(key);
    } 
    
}
