/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.web.resources;

import javax.servlet.http.HttpServlet;

import com.drs.beam.core.modules.data.DaoWebPages;

/**
 *
 * @author Diarsid
 */
class AllPagesInDirectoryServlet extends HttpServlet {
    
    private final DaoWebPages webDao;
    
    AllPagesInDirectoryServlet(DaoWebPages webDao) {
        this.webDao = webDao;
    }
}
