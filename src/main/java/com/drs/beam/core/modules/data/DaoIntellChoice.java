/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.data;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Diarsid
 */
public interface DaoIntellChoice {
    
    String getChoiceFor(String command);
    
    List<String> getChoicesLike(String command);
    
    Map<String, String> getAllChoices();
    
    boolean deleteChoiceForCommand(String command);
    
    boolean newChoice(String command, String choice);
}
