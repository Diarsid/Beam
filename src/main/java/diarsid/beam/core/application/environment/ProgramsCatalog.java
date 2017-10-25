/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.environment;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.domain.entities.Program;

/**
 *
 * @author Diarsid
 */
public interface ProgramsCatalog extends Catalog {
    
    Optional<Program> findProgramByDirectName(String name);
    
    List<Program> findProgramsByPatternSimilarity(String pattern);
    
    List<Program> findProgramsByWholePattern(String pattern);
    
    List<Program> findProgramsByStrictName(String strictName);
    
    List<Program> getAll();
}
