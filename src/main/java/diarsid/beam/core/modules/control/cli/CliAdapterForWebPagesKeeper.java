/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import java.util.function.Function;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.ValueOperationComplete;
import diarsid.beam.core.base.control.flow.VoidOperation;
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
        ValueOperation<WebPage> flow = this.pagesKeeper.findWebPageByPattern(initiator, command);
        Function<ValueOperationComplete, Message> onSuccess = (success) -> {
            return ((WebPage) success.getOrThrow()).toMessage();
        }; 
        super.reportValueOperationFlow(initiator, flow, onSuccess, "page not found.");
    }
    
    void editWebPageAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidOperation flow = this.pagesKeeper.editWebPage(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "done!");
    }
    
    void createWebPageAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidOperation flow = this.pagesKeeper.createWebPage(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "created!");
    }
    
    void deleteWebPageAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidOperation flow = this.pagesKeeper.removeWebPage(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "removed.");
    }
    
    void showWebPlace(Initiator initiator, EmptyCommand command) {
        ValueOperation<Message> flow = this.pagesKeeper.getWebPlace(initiator, command);
        Function<ValueOperationComplete, Message> onSuccess = (success) -> {
            return (Message) success.getOrThrow();
        }; 
        super.reportValueOperationFlow(initiator, flow, onSuccess, "Panel not available.");
    }
    
    void captureWebPageImage(Initiator initiator, ArgumentsCommand command) {
        VoidOperation flow = this.pagesKeeper.captureImage(initiator, command);
        super.reportVoidOperationFlow(initiator, flow);
    }
}
