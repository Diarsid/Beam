/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.io.IOException;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.WebRequest;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.WebDirectory;

/**
 *
 * @author Diarsid
 */
public interface WebDirectoriesKeeper {
    
    VoidOperation createWebDirectory(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation deleteWebDirectory(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation editWebDirectory(
            Initiator initiator, ArgumentsCommand command);
    
    ValueOperation<? extends WebDirectory> findWebDirectory(
            Initiator initiator, ArgumentsCommand command);
    
    void createWebDirectory(
            WebRequest webRequest) throws IOException;
    
    void deleteWebDirectory(
            WebRequest webRequest) throws IOException;
    
    void editWebDirectoryName(
            WebRequest webRequest) throws IOException;
    
    void editWebDirectoryPlace(
            WebRequest webRequest) throws IOException;
    
    void editWebDirectoryOrder(
            WebRequest webRequest) throws IOException;
    
    void getWebDirectory(
            WebRequest webRequest) throws IOException;
    
    void getWebDirectoryPages(
            WebRequest webRequest) throws IOException;
    
    void getAllDirectoriesInPlace(
            WebRequest webRequest) throws IOException;
}
