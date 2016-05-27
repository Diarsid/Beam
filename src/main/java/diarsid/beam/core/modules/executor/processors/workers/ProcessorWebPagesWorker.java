/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.processors.workers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import diarsid.beam.core.entities.WebPage;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.HandlerWebPages;
import diarsid.beam.core.modules.executor.IntelligentExecutorCommandContext;
import diarsid.beam.core.modules.executor.OS;
import diarsid.beam.core.modules.executor.processors.ProcessorWebPages;
import diarsid.beam.core.modules.executor.workflow.OperationResult;

import static diarsid.beam.core.modules.executor.workflow.OperationResult.failByInvalidArgument;
import static diarsid.beam.core.modules.executor.workflow.OperationResult.failByInvalidLogic;

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
        if (params.contains("with") || 
                params.contains("w") || 
                params.contains("in")) {
            operations.add(this.openWebPageWithGivenBrowser(params));
        } else {
            operations.addAll(this.openWebPages(params));
        }
        return operations;
    }    
    
    private List<OperationResult> openWebPages(List<String> commandParams) {
        // command pattern: see [webPage_1] [webPage_2] [webPage_3]...
        WebPage page;
        List<OperationResult> operations = new ArrayList<>();
        for (int i = 1; i < commandParams.size(); i++) {
            // register current command as: see [webPage_i]
            this.intellContext.adjustCurrentlyExecutedCommand(
                    commandParams.get(0), 
                    commandParams.get(i));
            page = this.getWebPage(commandParams.get(i));
            if (page != null) {
                operations.add(this.processPageWithItsOwnBrowser(page));
            } else {
                operations.add(failByInvalidArgument(commandParams.get(i)));
            }
        }
        return operations;
    }

    private OperationResult processPageWithItsOwnBrowser(WebPage page) {
        OperationResult result;
        if (page.useDefaultBrowser()){
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
            WebPage page = this.getWebPage(commandParams.get(1));
            String givenBrowser = commandParams.get(3);
            if (page != null) {
                if (givenBrowser.equals("default") || givenBrowser.equals("def")) {
                    return processPageWithDefaultBrowser(page);
                } else {
                    return processPageWithNonDefaultBrowser(page, givenBrowser);
                }                
            } else {
                return failByInvalidArgument(commandParams.get(1));
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
        String pageBrowser = page.getBrowser();
        if ( pageBrowser.contains(givenBrowser)
                || givenBrowser.contains(pageBrowser) ) {
            // do nothing because it seams that it is the same browser
        } else {
            this.rememberNewBrowserForPage(page, givenBrowser);
        }
        return result;
    }

    private OperationResult processPageWithDefaultBrowser(WebPage page) {
        OperationResult result;
        result = this.system.openUrlWithDefaultBrowser(
                page.getUrlAddress());
        this.pagesHandler.editWebPageBrowser(
                page.getName(), "default");
        return result;
    }
    
    private void rememberNewBrowserForPage(WebPage page, String givenBrowser) {
        String[] vars = {"yes", "no"};
        int choosed = this.ioEngine.resolveVariantsWithExternalIO(
                "Use given browser always for this page?", 
                Arrays.asList(vars));
        if (choosed == 1) {
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
        
        if (pages.size() == 1) {
            return pages.get(0);
        } else if (pages.isEmpty()) {
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
                return pages.get(choosedVariant-1);
            } 
        }
    }    
}
