/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.data;

import java.util.List;

/**
 *
 * @author Diarsid
 */
public interface DaoIntellChoice {
    
    String getChoiceFor(String command);
    
    List<String> getCommandsInChoicesLike(String command);
    
    boolean deleteChoiceForCommand(String command);
    
    boolean newChoice(String command, String choice);
}
