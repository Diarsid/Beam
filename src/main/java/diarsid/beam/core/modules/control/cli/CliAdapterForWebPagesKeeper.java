/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import java.util.function.Function;

import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.ValueFlowCompleted;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.EmptyCommand;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.modules.domainkeeper.WebPagesKeeper;

/**
 *
 * @author Diarsid
 */
class CliAdapterForWebPagesKeeper extends AbstractCliAdapter {
    
    private final WebPagesKeeper pagesKeeper;
    
    CliAdapterForWebPagesKeeper(WebPagesKeeper pagesKeeper, InnerIoEngine ioEngine) {
        super(ioEngine);
        this.pagesKeeper = pagesKeeper;
    }
    
    void findWebPageAndReport(Initiator initiator, ArgumentsCommand command) {
        ValueFlow<WebPage> flow = this.pagesKeeper.findWebPageByPattern(initiator, command);
        Function<ValueFlowCompleted<WebPage>, Message> onSuccess = (success) -> {
            return success.orThrow().toMessage();
        }; 
        super.reportValueFlow(initiator, flow, onSuccess, "page not found.");
    }
    
    void editWebPageAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.pagesKeeper.editWebPage(initiator, command);
        super.reportVoidFlow(initiator, flow, "done!");
    }
    
    void createWebPageAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.pagesKeeper.createWebPage(initiator, command);
        super.reportVoidFlow(initiator, flow, "created!");
    }
    
    void deleteWebPageAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.pagesKeeper.removeWebPage(initiator, command);
        super.reportVoidFlow(initiator, flow, "removed.");
    }
    
    void showWebPlace(Initiator initiator, EmptyCommand command) {
        ValueFlow<Message> flow = this.pagesKeeper.getWebPlace(initiator, command);
        Function<ValueFlowCompleted<Message>, Message> onSuccess = (success) -> {
            return success.orThrow();
        }; 
        super.reportValueFlow(initiator, flow, onSuccess, "Panel not available.");
    }
    
    void showAllWebPages(Initiator initiator) {
        ValueFlow<Message> flow = this.pagesKeeper.findAll(initiator);
        Function<ValueFlowCompleted<Message>, Message> onSuccess = (success) -> {
            return success.orThrow();
        }; 
        super.reportValueFlow(initiator, flow, onSuccess, "cannot get all WebPages.");
    }
    
    void captureWebPageImage(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.pagesKeeper.captureImage(initiator, command);
        super.reportVoidFlow(initiator, flow);
    }
}
