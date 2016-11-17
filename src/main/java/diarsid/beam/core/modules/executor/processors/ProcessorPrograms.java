/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.processors;

/**
 *
 * @author Diarsid
 */
public interface ProcessorPrograms {

    void runMarkedProgram(String mark, String programName);

    void runProgram(String programName);    
}
