/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.executor;

import java.util.ArrayList;
import java.util.List;

import com.drs.beam.core.modules.DataModule;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.data.DaoIntellChoice;

/**
 *
 * @author Diarsid
 */
class IntelligentResolver {
    
    private final IoInnerModule ioEngine;
    private final DaoIntellChoice choiceDao;
            
    private boolean active;
    private boolean shouldAskUser;
    
    IntelligentResolver(DataModule data, IoInnerModule io) {
        this.ioEngine = io;
        this.active = true;
        this.shouldAskUser = false;
        this.choiceDao = data.getIntellChoiceDao();
    }
    
    int resolve(
            String question, String command, List<String> variants) {        
        
        if ( this.active ) {
            return this.tryToChoose(question, command, variants);
        } else {
            return this.askIoAndRememberChoice(question, command, variants);
        }
    }
    
    private int tryToChoose(
            String question, String command, List<String> variants) {
        
        String choice;
        choice = tryToGuessChoice(variants);
        if (choice.isEmpty()) {
            choice = this.choiceDao.getChoiceFor(command);
        } 
        if (choice.isEmpty()) {
            return this.askIoAndRememberChoice(question, command, variants);
        } else {
            if ( variants.contains(choice) ) {
                return variants.indexOf(choice);
            } else {
                return this.askIoAndRememberChoice(question, command, variants);
            }
        }
    }
    
    private String tryToGuessChoice(List<String> variants) {
        String min = variants.get(0);
        int maxLength = variants.get(0).length();
        int minLength = variants.get(0).length();
        int medium = 0;
        if ( variants.size() > 2 ) {             
            for (int i = 0; i < variants.size(); i++) {
                if (variants.get(i).length() > maxLength) {
                    if (maxLength < minLength) {
                        minLength = maxLength;
                        min = variants.get(i);
                    } 
                    maxLength = variants.get(i).length();
                } else if (variants.get(i).length() < minLength) {
                    minLength = variants.get(i).length();
                    min = variants.get(i);
                } 
                medium = medium + variants.get(i).length();
            }
            medium = (medium - maxLength - minLength) / (variants.size() - 2);
            if ( (minLength*1.0 / medium ) > 0.7 ) {
                return "";
            } else {
                return min;
            }
        } else {
            if (variants.get(0).length() > variants.get(1).length()) {
                maxLength = variants.get(0).length();
                minLength = variants.get(1).length();
                min = variants.get(1);
            } else if (variants.get(0).length() < variants.get(1).length()) {
                maxLength = variants.get(1).length();
                minLength = variants.get(0).length();
                min = variants.get(0);
            } else {
                return "";
            }
            if ((minLength * 1.0 / maxLength) > 0.7) {
                return "";
            } else {
                return min;
            }
        }
    }
    
    void setActive(boolean isActive) {
        this.active = isActive;
    }
    
    void setAskUserToRememberHisChoice(boolean askUser) {
        this.shouldAskUser = askUser;
    }
    
    boolean deleteMem(String command) {
        List<String> commands = this.choiceDao.getCommandsInChoicesLike(command);
        if ( commands.isEmpty() ) {
            this.ioEngine.reportMessage("There is no such command in memory.");
            return false;
        } else if ( commands.size() > 1 ) {
            int delete = this.ioEngine.resolveVariantsWithExternalIO(
                    "Which command delete from memory?", commands);
            return this.choiceDao.deleteChoiceForCommand(commands.get(delete-1));
        } else {
            return this.choiceDao.deleteChoiceForCommand(command);
        }
    }
    
    private int askIoAndRememberChoice(
            String question, String command, List<String> variants) {
        
        int choiceNumber = 
                this.ioEngine.resolveVariantsWithExternalIO(question, variants);
        if ( this.shouldAskUser ) {
            List<String> yesOrNo = new ArrayList<>();
            yesOrNo.add("yes");
            yesOrNo.add("no");
            int remember = this.ioEngine.resolveVariantsWithExternalIO(
                    "remember your choice for this command?", yesOrNo);
            if ( remember == 1 ) {
                this.choiceDao.newChoice(command, variants.get(choiceNumber-1));
            }
        } else {
            this.choiceDao.newChoice(command, variants.get(choiceNumber-1));
        }
        return choiceNumber;
    }
}
