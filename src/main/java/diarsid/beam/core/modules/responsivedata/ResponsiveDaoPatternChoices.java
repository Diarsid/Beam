/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.responsivedata;

import java.util.Optional;

import diarsid.beam.core.base.analyze.variantsweight.Variants;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.modules.data.DaoPatternChoices;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDaoPatternChoices extends BeamCommonResponsiveDao<DaoPatternChoices> {

    ResponsiveDaoPatternChoices(DaoPatternChoices dao, InnerIoEngine ioEngine) {
        super(dao, ioEngine);
    }
    
    public boolean hasMatchOf(
            Initiator initiator, String original, String extended, Variants variants) {
        try {
            return super.dao().hasMatchOf(original, extended, variants);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public Optional<String> findChoiceFor(
            Initiator initiator, String original, Variants variants) {
        try {
            return super.dao().findChoiceFor(original, variants);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public boolean save(
            Initiator initiator, String original, String extended, Variants variants) {
        try {
            return super.dao().save(original, extended, variants);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean save(
            Initiator initiator, InvocationCommand command, Variants variants) {
        try {
            return super.dao().save(command, variants);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean delete(Initiator initiator, String original) {
        try {
            return super.dao().delete(original);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean delete(Initiator initiator, InvocationCommand command) {
        try {
            return super.dao().delete(command);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
}
