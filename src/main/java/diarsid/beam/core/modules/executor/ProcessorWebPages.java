/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import diarsid.beam.core.entities.WebPage;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.HandlerWebPages;

/**
 *
 * @author Diarsid
 */
class ProcessorWebPages {
    
    private final IoInnerModule ioEngine;
    private final OS system;
    private final HandlerWebPages pagesHandler;
    private final IntelligentResolver intell;
    
    ProcessorWebPages(
            IoInnerModule io, 
            OS sys, 
            HandlerWebPages pages, 
            IntelligentResolver intell) {
        
        this.ioEngine = io;
        this.system = sys;
        this.pagesHandler = pages;
        this.intell = intell;
    }
    
    void openWebPage(List<String> params) {
        if (params.contains("with") || 
                params.contains("w") || 
                params.contains("in")) {
            this.openWebPageWithGivenBrowser(params);
        } else {
            this.openWebPages(params);
        }
    }    
    
    private void openWebPages(List<String> commandParams) {
        // command pattern: see [webPage_1] [webPage_2] [webPage_3]...
        WebPage page;
        for (int i = 1; i < commandParams.size(); i++) {
            // register current command as: see [webPage_i]
            this.intell.adjustCurrentCommand(
                    commandParams.get(0), 
                    commandParams.get(i));
            page = this.getWebPage(commandParams.get(i));
            if (page != null) {
                if (page.useDefaultBrowser()){
                    this.system.openUrlWithDefaultBrowser(page.getUrlAddress());
                } else {
                    this.system.openUrlWithGivenBrowser(page.getUrlAddress(), page.getBrowser());
                }
            }
        }
    }
    
    private void openWebPageWithGivenBrowser(List<String> commandParams) {
        // command pattern: see [webPage] with|w [browserName]
        if (commandParams.size() > 3 && 
                (commandParams.get(2).contains("w") || 
                commandParams.get(2).contains("in") )) {
            WebPage page = this.getWebPage(commandParams.get(1));
            String givenBrowser = commandParams.get(3);
            if (page != null) {
                if (givenBrowser.equals("default") || givenBrowser.equals("def")) {
                    this.system.openUrlWithDefaultBrowser(page.getUrlAddress());
                    this.pagesHandler.editWebPageBrowser(page.getName(), "default");
                } else {
                    this.system.openUrlWithGivenBrowser(page.getUrlAddress(), givenBrowser);
                    String pageBrowser = page.getBrowser();
                    if ( pageBrowser.contains(givenBrowser) 
                            || givenBrowser.contains(pageBrowser) ) {
                        // do nothing because it seams that it is the same browser
                        return;
                    } else {
                        this.rememberNewBrowserForPage(page, givenBrowser);
                    }                    
                }                
            }
        } else {
            this.ioEngine.reportMessage("Unrecognizale command.");
        }
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
        return this.resolveMultiplePages(pages);
    }
    
    private WebPage resolveMultiplePages(List<WebPage> pages) {
        if (pages.size() == 1) {
            return pages.get(0);
        } else if (pages.isEmpty()) {
            this.ioEngine.reportMessage("Couldn`t find such page.");
            return null;
        } else {
            List<String> pageNames = new ArrayList<>();
            for (WebPage wp : pages) {
                pageNames.add(
                        wp.getName() + " - " + 
                        wp.getDirectory() + "::" + 
                        wp.getPlacement().name().toLowerCase());
            }
            int choosedVariant = this.intell.resolve(
                    "There are several pages:",
                    pageNames);
            //int choosedVariant = this.ioEngine.resolveVariantsWithExternalIO(
            //        "There are several pages:", pageNames);
            
            if (choosedVariant < 0) {
                return null;
            } else {
                return pages.get(choosedVariant-1);
            } 
        }
    }    
}
