/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.shared.modules.config;

import java.io.File;

/**
 *
 * @author Diarsid
 */
public interface XmlReader {
    
    XmlContent read(File configFile);
}
