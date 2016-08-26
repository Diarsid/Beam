/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.processors.workers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import diarsid.beam.core.entities.global.WebPage;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.HandlerWebPages;
import diarsid.beam.core.modules.executor.IntelligentExecutorCommandContext;
import diarsid.beam.core.modules.executor.OS;
import diarsid.beam.core.modules.executor.processors.ProcessorWebPages;
import diarsid.beam.core.modules.executor.workflow.OperationResult;

import static diarsid.beam.core.modules.executor.workflow.OperationResultImpl.failByInvalidArgument;
import static diarsid.beam.core.modules.executor.workflow.OperationResultImpl.failByInvalidLogic;

/**
 *
 * @author Diarsid
 */
class ProcessorWebPagesWorker implements ProcessorWebPages {
    
    private final IoInnerModule ioEngine;
    private final OS system;
    private final HandlerWebPages pagesHandler;
    private final IntelligentExecutorCommandContext intellContext;
    
    ProcessorWebPagesWorker(
            IoInnerModule io, 
            OS sys, 
            HandlerWebPages pages, 
            IntelligentExecutorCommandContext intell) {
        
        this.ioEngine = io;
        this.system = sys;
        this.pagesHandler = pages;
        this.intellContext = intell;
    }
    
    @Override
    public List<OperationResult> openWebPage(List<String> params) {
        List<OperationResult> operations = new ArrayList<>();
        if ( params.contains("with") || 
                params.contains("w") || 
                params.contains("in") ) {
            operations.add(this.openWebPageWithGivenBrowser(params));
        } else {
            operations.addAll(this.openWebPages(params));
        }
        return operations;
    }    
    
    private List<OperationResult> openWebPages(List<String> commandParams) {
        // command pattern: see [webPage_1] [webPage_2] [webPage_3]...
        WebPage page;
        String processedPageName;
        List<OperationResult> operations = new ArrayList<>();
        for (int i = 1; i < commandParams.size(); i++) {
            processedPageName = commandParams.get(i);
            // register current command as: see [webPage_i]
            this.intellContext.adjustCurrentlyExecutedCommand(
                    "see", processedPageName);
            page = this.getWebPage(processedPageName);
            if ( page != null ) {
                operations.add(this.processPage(page));
            } else {
                operations.add(failByInvalidArgument(processedPageName));
            }
        }
        return operations;
    }

    private OperationResult processPage(WebPage page) {
        OperationResult result;
        if ( page.useDefaultBrowser() ) {
            result = this.system.openUrlWithDefaultBrowser(
                    page.getUrlAddress());
        } else {
            result = this.system.openUrlWithGivenBrowser(
                    page.getUrlAddress(), page.getBrowser());
        }
        return result;
    }
    
    private OperationResult openWebPageWithGivenBrowser(
            List<String> commandParams) {
        
        // command pattern: see [webPage] with|w [browserName]
        if (commandParams.size() > 3 && 
                (commandParams.get(2).contains("w") || 
                commandParams.get(2).contains("in") )) {
            
            String givenPageName = commandParams.get(1);
            WebPage page = this.getWebPage(givenPageName);
            String givenBrowser = commandParams.get(3);
            
            if ( page != null ) {
                if ( givenBrowser.equals("default") || givenBrowser.equals("def") ) {
                    return this.processPageWithDefaultBrowser(page);
                } else {
                    return this.processPageWithNonDefaultBrowser(page, givenBrowser);
                }                
            } else {
                return failByInvalidArgument(givenPageName);
            }
        } else {
            this.ioEngine.reportMessage("Unrecognizale command.");
            return failByInvalidLogic();
        }
    }

    private OperationResult processPageWithNonDefaultBrowser(
            WebPage page, String givenBrowser) {
        
        OperationResult result;
        result = this.system.openUrlWithGivenBrowser(
                page.getUrlAddress(), givenBrowser);
        this.rememberNewBrowserForPageIfValid(result, givenBrowser, page);
        return result;
    }

    private void rememberNewBrowserForPageIfValid(
            OperationResult result, String givenBrowser, WebPage page) {
        if ( result.ifFail() ) {
            return;
        }
        if ( page.getBrowser().contains(givenBrowser)
                || givenBrowser.contains(page.getBrowser()) ) {
            // do nothing because it seems that it is the same browser
        } else {
            this.rememberNewBrowserForPage(page, givenBrowser);
        }
    }

    private OperationResult processPageWithDefaultBrowser(WebPage page) {
        OperationResult result;
        result = this.system.openUrlWithDefaultBrowser(page.getUrlAddress());
        this.pagesHandler.editWebPageBrowser(page.getName(), "default");
        return result;
    }
    
    private void rememberNewBrowserForPage(WebPage page, String givenBrowser) {
        String[] vars = {"yes", "no"};
        int chosen = this.ioEngine.resolveVariantsWithExternalIO(
                "Use given browser always for this page?", 
                Arrays.asList(vars));
        if ( chosen == 1 ) {
            if (this.pagesHandler.editWebPageBrowser(page.getName(), givenBrowser)) {
                this.ioEngine.reportMessage("Get it.");
            }                   
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
            int choosedVariant = this.intellContext.resolve(
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
