/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.responsivedata;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.modules.data.DaoCommands;

import static java.util.Collections.emptyList;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDaoCommands extends BeamCommonResponsiveDao<DaoCommands> {

    ResponsiveDaoCommands(DaoCommands dao, InnerIoEngine ioEngine) {
        super(dao, ioEngine);
    }
    
    public Optional<InvocationCommand> getByExactOriginalAndType(
            Initiator initiator, String original, CommandType type) {
        try {
            return super.dao().getByExactOriginalAndType(original, type);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public List<InvocationCommand> getByExactOriginalOfAnyType(
            Initiator initiator, String original) {
        try {
            return super.dao().getByExactOriginalOfAnyType(original);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public List<InvocationCommand> searchInOriginalByPattern(
            Initiator initiator, String pattern) {
        try {
            return super.dao().searchInOriginalByPattern(pattern);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public List<InvocationCommand> searchInOriginalByPatternAndType(
            Initiator initiator, String pattern, CommandType type) {
        try {
            return super.dao().searchInOriginalByPatternAndType(pattern, type);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public List<InvocationCommand> searchInExtendedByPattern(
            Initiator initiator, String pattern) {
        try {
            return super.dao().searchInExtendedByPattern(pattern);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public List<InvocationCommand> searchInExtendedByPatternAndType(
            Initiator initiator, String pattern, CommandType type) {
        try {
            return super.dao().searchInExtendedByPatternAndType(pattern, type);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public List<InvocationCommand> searchInExtendedByPatternGroupByExtended(
            Initiator initiator, String pattern) {
        try {
            return super.dao().searchInExtendedByPatternGroupByExtended(pattern);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public List<InvocationCommand> searchInExtendedByPatternAndTypeGroupByExtended(
            Initiator initiator, String pattern, CommandType type) {
        try {
            return super.dao().searchInExtendedByPatternAndTypeGroupByExtended(pattern, type);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public boolean save(
            Initiator initiator, InvocationCommand command) {
        try {
            return super.dao().save(command);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean save(
            Initiator initiator, List<? extends InvocationCommand> commands) {
        try {
            return super.dao().save(commands);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean delete(
            Initiator initiator, InvocationCommand command) {
        try {
            return super.dao().delete(command);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean deleteByExactOriginalOfAllTypes(
            Initiator initiator, String original) {
        try {
            return super.dao().deleteByExactOriginalOfAllTypes(original);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean deleteByExactExtendedOfType(
            Initiator initiator, String extended, CommandType type) {
        try {
            return super.dao().deleteByExactExtendedOfType(extended, type);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean deleteByExactOriginalOfType(
            Initiator initiator, String original, CommandType type) {
        try {
            return super.dao().deleteByExactOriginalOfType(original, type);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean deleteByPrefixInExtended(
            Initiator initiator, String prefixInExtended, CommandType type) {
        try {
            return super.dao().deleteByPrefixInExtended(prefixInExtended, type);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
}
