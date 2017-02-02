/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application;

import java.io.File;

import diarsid.beam.core.domain.entities.Program;
import diarsid.beam.core.os.search.result.FileSearchResult;

/**
 *
 * @author Diarsid
 */
public interface ProgramsCatalog extends Catalog {
    
    FileSearchResult findProgramByPattern(String nameOrPattern);
    
    FileSearchResult findProgramByStrictName(String strictName);
    
    File asFile(Program program);
}
