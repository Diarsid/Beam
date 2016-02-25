/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.IoInnerModule;

import diarsid.beam.core.modules.data.DaoIntellChoice;

/**
 *
 * @author Diarsid
 */
public class IntelligentResolver {
    
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
    
    public int resolve(
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
                return variants.indexOf(choice)+1;
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
            if ( (minLength*1.0 / medium ) > 0.6 ) {
                return "";
            } else {
                if (min.length() > 1) {
                    return min;
                } else {
                    return "";
                }                
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
            if ((minLength * 1.0 / maxLength) > 0.6) {
                return "";
            } else {
                if (min.length() > 1) {
                    return min;
                } else {
                    return "";
                }
            }
        }
    }
    
    private int askIoAndRememberChoice(
            String question, String command, List<String> variants) {
        
        int choiceNumber = 
                this.ioEngine.resolveVariantsWithExternalIO(question, variants);
        if ( choiceNumber > -1 ) {
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
        }
        return choiceNumber;
    }
    
    Map<String, String> getAllChoices() {
        return this.choiceDao.getAllChoices();
    }
    
    void setActive(boolean isActive) {
        this.active = isActive;
        if ( isActive ) {
            this.ioEngine.reportMessage("Intelligent command resolving enabled.");
        } else {
            this.ioEngine.reportMessage("Intelligent command resolving disabled.");
        }
    }
    
    void setAskUserToRememberHisChoice(boolean askUser) {
        this.shouldAskUser = askUser;
        if ( askUser ) {
            this.ioEngine.reportMessage("I will ask before remembering your choice.");
        } else {
            this.ioEngine.reportMessage("I will NOT ask before remembering your choice.");
        }        
    }
    
    boolean deleteMem(String command) {
        List<String> commands = this.choiceDao.getChoicesLike(command);
        if ( commands.isEmpty() ) {
            this.ioEngine.reportMessage("There is no such command in memory.");
            return false;
        } else if ( commands.size() > 1 ) {
            int delete = this.ioEngine.resolveVariantsWithExternalIO(
                    "Which command delete from memory?", commands);
            if (delete > 0) {
                String del = commands.get(delete-1);
                del = del.substring(0, del.indexOf("-> ")).trim();                
                return this.choiceDao.deleteChoiceForCommand(del);
            } else {
                return false;
            }            
        } else {
            return this.choiceDao.deleteChoiceForCommand(command+"%");
        }
    }
}
