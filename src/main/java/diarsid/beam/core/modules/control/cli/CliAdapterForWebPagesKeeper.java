/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.modules.domainkeeper.WebPagesKeeper;

/**
 *
 * @author Diarsid
 */
public class CliAdapterForWebPagesKeeper extends AbstractCliAdapter {
    
    private final WebPagesKeeper pagesKeeper;
    
    public CliAdapterForWebPagesKeeper(InnerIoEngine ioEngine, WebPagesKeeper pagesKeeper) {
        super(ioEngine);
        this.pagesKeeper = pagesKeeper;
    }
}
