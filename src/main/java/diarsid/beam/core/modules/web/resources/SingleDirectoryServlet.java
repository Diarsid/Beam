/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.resources;

import javax.servlet.http.HttpServlet;

import diarsid.beam.core.modules.data.DaoWebPages;

/**
 *
 * @author Diarsid
 */
class SingleDirectoryServlet extends HttpServlet {
    
    private final DaoWebPages webDao;    
    
    SingleDirectoryServlet(DaoWebPages webDao) {
        this.webDao = webDao;
    }
}