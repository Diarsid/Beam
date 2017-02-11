/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.service.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebPageDirectory;
import diarsid.beam.core.domain.validation.flow.ValidationResult;
import diarsid.beam.core.modules.PagesManagerModule;
import diarsid.beam.core.modules.web.core.container.Resource;
import diarsid.beam.core.modules.web.core.container.ResponseJsonWriter;
import diarsid.beam.core.modules.web.core.rest.RestUrlParamsParser;
import diarsid.beam.core.modules.web.service.json.entities.JsonWebDirectory;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import static diarsid.beam.core.domain.entities.WebPagePlacement.placementOf;
import static diarsid.beam.core.domain.entities.WebPagePlacement.validatePlacementName;

/**
 *
 * @author Diarsid
 */
public class AllDirectoriesResource extends Resource {
    
    private static final String NAME;
    private static final String MAPPING;
    private static final String MAPPING_REGEXP;
    private static final String PLACE_PARAM;
    
    static {
        NAME = "ALL_DIRECTORIES_RESOURCE";
        PLACE_PARAM = "{placement}";
        MAPPING = "/resources/" + PLACE_PARAM + "/directories";
        MAPPING_REGEXP = "/resources/(webpanel|bookmarks)/directories";
    }
    
    private final PagesManagerModule pagesManager;
    private final ResponseJsonWriter responseWriter;
    
    public AllDirectoriesResource(
            RestUrlParamsParser parser, PagesManagerModule pages, ResponseJsonWriter responseJsonWriter) {
        super(NAME, MAPPING, MAPPING_REGEXP, parser);
        this.pagesManager = pages;
        this.responseWriter = responseJsonWriter;
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String place = super.parseRestUrl(request).get(PLACE_PARAM);
        ValidationResult placeValid = validatePlacementName(place);
        if ( placeValid.isOk() ) {
            List<WebPageDirectory> dirs = this.pagesManager.getAllDirectoriesIn(placementOf(place));
            List<JsonWebDirectory> jsonDirs = new ArrayList<>();
            List<WebPage> pages;
            for (WebPageDirectory dir : dirs) {
                pages = this.pagesManager.getAllWebPagesInDirectoryAndPlacement(
                        dir.getName(), placementOf(place), true);
                jsonDirs.add(new JsonWebDirectory(dir, pages));
            }
            this.responseWriter.writeJsonAnswerAndClose(jsonDirs, response);
        } else {
            this.responseWriter.writeJsonErrorAndClose(
                    SC_BAD_REQUEST, placeValid.getFailureMessage(), response);
        }        
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
    }
}
