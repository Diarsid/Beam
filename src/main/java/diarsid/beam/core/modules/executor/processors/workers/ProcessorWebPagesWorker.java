/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.processors.workers;

import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.HandlerWebPages;
import diarsid.beam.core.modules.executor.OS;
import diarsid.beam.core.modules.executor.context.ExecutorContext;
import diarsid.beam.core.modules.executor.processors.ProcessorWebPages;

import static java.util.Objects.nonNull;

/**
 *
 * @author Diarsid
 */
class ProcessorWebPagesWorker implements ProcessorWebPages {
    
    private final IoInnerModule ioEngine;
    private final OS system;
    private final HandlerWebPages pagesHandler;
    private final ExecutorContext context;
    
    ProcessorWebPagesWorker(
            IoInnerModule io, 
            OS sys, 
            HandlerWebPages pages, 
            ExecutorContext intell) {        
        this.ioEngine = io;
        this.system = sys;
        this.pagesHandler = pages;
        this.context = intell;
    }
    
    @Override
    public void openWebPage(List<String> params) {
        WebPage page = this.getWebPage(params.get(1));
        if ( nonNull(page) ) {
            this.context.adjustCurrentlyExecutedCommand("see " + params.get(1));
            this.system.openUrlWithDefaultBrowser(page.getUrlAddress());
        } else {
            this.context.discardCurrentlyExecutedCommandInPatternAndOperation(
                    "see", params.get(1));
        }
    }  
    
    private WebPage getWebPage(String name) {        
        List<WebPage> pages = this.pagesHandler.getWebPages(name);
        return this.resolveMultiplePages(name, pages);
    }
    
    private WebPage resolveMultiplePages(
            String requiredPageName, List<WebPage> pages) {
        
        if ( pages.size() == 1 ) {
            return pages.get(0);
        } else if ( pages.isEmpty() ) {
            this.ioEngine.reportMessage("Couldn`t find such page.");
            return null;
        } else {
            List<String> displayedPagesInfo = new ArrayList<>();
            for (WebPage wp : pages) {
                displayedPagesInfo.add(
                        wp.getName() + " - " + 
                        wp.getDirectory() + "::" + 
                        wp.getPlacement().name().toLowerCase());
            }
            int choosedVariant = this.context.resolve(
                    "There are several pages:",
                    requiredPageName,
                    displayedPagesInfo);
            if (choosedVariant < 0) {
                return null;
            } else {
                return pages.get(choosedVariant - 1);
            } 
        }
    }    
}
