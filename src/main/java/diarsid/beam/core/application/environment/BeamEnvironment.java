/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import diarsid.beam.core.base.analyze.similarity.Similarity;
import diarsid.beam.core.base.analyze.variantsweight.Analyze;
import diarsid.support.configuration.Configuration;

import static diarsid.beam.core.application.environment.CurrentWorkingDirectory.currentWorkingDirectory;
import static diarsid.beam.core.application.environment.ScriptSyntax.scriptSyntax;
import static diarsid.beam.core.base.os.treewalking.search.FileSearcher.searcherWithDepthsOf;
import static diarsid.support.configuration.Configuration.actualConfiguration;
import static diarsid.support.configuration.Configuration.configure;
import static diarsid.support.objects.Pools.pools;

/**
 *
 * @author Diarsid
 */
public class BeamEnvironment {
    
    private static final Analyze ANALYZE;
    private static final Similarity SIMILARITY;
    
    static {
            configure()
                    .withDefault(
                            "log = true",
                            "data.store = ../res/data",
                            "data.driver = org.h2.Driver",
                            "data.user = root",
                            "data.pass = root",
                            "data.log = true",
                            "data.access.version = 2",
                            "catalogs.programs = ../env/programs",
                            "catalogs.notes = ../env/notes",
                            "filesystem.executables = ",
                            "filesystem.program.specific.files = ",
                            "filesystem.program.specific.folders = ",
                            "filesystem.project.definitive.files = ",
                            "filesystem.project.definitive.folders = ",
                            "filesystem.project.specific.files = ",
                            "filesystem.project.specific.folders = ",
                            "filesystem.restricted.folders = ",
                            "ui.images.resources = ../res/images/",
                            "ui.images.capture.webpages.resize = true",
                            "ui.console.runOnStart = true",
                            "ui.console.showOnControlClick = true",
                            "ui.console.default.height = ",
                            "ui.console.default.width = ",
                            "ui.screen.insets = 30",
//                            "analyze.weight.base.log = false",
//                            "analyze.weight.positions.search.log = false",
//                            "analyze.weight.positions.clusters.log = false",
                            "analyze.weight.base.log = true",
                            "analyze.weight.positions.search.log = true",
                            "analyze.weight.positions.clusters.log = true",
                            "analyze.result.variants.limit = 11",
                            "analyze.similarity.log.base = true",
                            "analyze.similarity.log.advanced = true",
                            "web.local.host = 127.0.0.1",
                            "web.local.port = 32001",
                            "web.local.path = /beam/core",
                            "web.local.resources = ../res/static_web_context",
                            "rmi.core.active = true",
                            "rmi.core.port = 43006",
                            "rmi.core.host = 127.0.0.1",
                            "rmi.sysconsole.port = 43005",
                            "rmi.sysconsole.host = 127.0.0.1",
                            "core.jvm.option = -Djava.rmi.server.hostname=127.0.0.1",
                            "core.jvm.option = -Dfile.encoding=UTF-8",
                            "core.jvm.option = -Dlog4j.configuration=file:../config/log4j.properties",
                            "core.jvm.option = -Xms32m",
                            "core.jvm.option = -Xmx32m",
                            "sysconsole.jvm.option = -Djava.rmi.server.hostname=127.0.0.1",
                            "sysconsole.jvm.option = -Dfile.encoding=UTF-8",
                            "sysconsole.jvm.option = -Dlog4j.configuration=file:../config/log4j.properties",
                            "sysconsole.jvm.option = -Xms4m",
                            "sysconsole.jvm.option = -Xmx4m",
                            "starter.jvm.option = -Xmx32m",
                            "starter.jvm.option = -Xms32m",
                            "starter.jvm.option = -Dfile.encoding=UTF-8",
                            "starter.jvm.option = -Dlog4j.configuration=file:../config/log4j.properties")
                    .read("../config/beam.config");
        
        SIMILARITY = new Similarity(configuration());
        ANALYZE = new Analyze(configuration(), SIMILARITY, pools());
    }

    private BeamEnvironment() {
    }

    public static ScriptsCatalog scriptsCatalog() {
        return new ScriptsCatalogReal(
                currentWorkingDirectory(), librariesCatalog(), configuration(), scriptSyntax())
                .refreshScripts();
    }

    public static LibrariesCatalog librariesCatalog() {
        return new LibrariesCatalogReal(".", "../lib");
    }

    public static ProgramsCatalog programsCatalog() {
        return new ProgramsCatalogReal(
                configuration().asString("catalogs.programs"),                 
                searcherWithDepthsOf(3, SIMILARITY));
    }

    public static NotesCatalog notesCatalog() {
        return new NotesCatalogReal(
                configuration().asString("catalogs.notes"), 
                searcherWithDepthsOf(5, SIMILARITY));
    }
    
    public static Configuration configuration() {
        return actualConfiguration();
    }
    
    public static Similarity similarity() {
        return SIMILARITY;
    }
    
    public static Analyze analyze() {
        return ANALYZE;
    }
    
}
