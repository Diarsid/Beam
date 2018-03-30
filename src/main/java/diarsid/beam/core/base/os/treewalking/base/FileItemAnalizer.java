/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.os.treewalking.base;

import java.nio.file.Path;
import java.util.List;

import diarsid.beam.core.application.environment.Configuration;

import static java.nio.file.Files.isDirectory;

import static diarsid.beam.core.base.util.CollectionsUtils.arrayListOf;
import static diarsid.beam.core.base.util.PathUtils.asName;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCaseAnyFragment;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsWordInIgnoreCase;

/**
 *
 * @author Diarsid
 */
class FileItemAnalizer {    
    
    private final List<String> executables;
    
    private final List<String> programSpecificFileSigns;
    private final List<String> programSpecificFolders;
    
    private final List<String> projectDefinitiveFiles;
    private final List<String> projectDefinitiveFolderSigns;
    private final List<String> projectSpecificFileSigns;
    private final List<String> projectSpecificFolderSigns;
    
    private final List<String> restrictedSpecificFolders;
    
    FileItemAnalizer(Configuration configuration) {
        this.executables = arrayListOf(
                ".bat", ".sh", ".exe", ".bash");
        this.executables.addAll(
                configuration.asList("filesystem.executables"));
        
        this.programSpecificFileSigns = arrayListOf(
                ".dll", "conf", ".cfg", ".db", ".dat", 
                "readme", ".log");
        this.programSpecificFileSigns.addAll(
                configuration.asList("filesystem.program.specific.files")); 
        
        this.programSpecificFolders = arrayListOf(
                "bin", "lib", "log", "conf", "config", 
                "temp");
        this.programSpecificFolders.addAll(
                configuration.asList("filesystem.program.specific.folders"));
        
        this.projectDefinitiveFiles = arrayListOf(
                ".project", ".pom", "pom.xml", "build.gradle", 
                " build.xml", "setup.py", ".babelrc", "makefile",
                ".iml", "package.json", "csproj", ".sl", ".sln", 
                "userpref", "vbproj");
        this.projectDefinitiveFiles.addAll(
                configuration.asList("filesystem.project.definitive.files"));
        
        this.projectDefinitiveFolderSigns = arrayListOf(
                "nbproject", ".idea", ".ant", "node_modules", 
                "xcodeproj", "xcworkspace");
        this.projectDefinitiveFolderSigns.addAll(
                configuration.asList("filesystem.project.definitive.folders"));
        
        this.projectSpecificFileSigns = arrayListOf(
                ".gitattributes", ".gitignore", ".sonar", 
                ".cpp", ".py", ".java", ".class", ".cs", ".js",
                "jsx", ".html", "readme.md", "license.md", ".go", 
                ".m", ".swift", ".cc", ".c", ".cxx", ".c++", 
                ".hbm", ".php", "readme", "license");
        this.projectSpecificFileSigns.addAll(
                configuration.asList("filesystem.project.specific.files"));
        
        this.projectSpecificFolderSigns = arrayListOf(
                ".git", "src", "bin", "build");
        this.projectSpecificFolderSigns.addAll(
                configuration.asList("filesystem.project.specific.folders"));
        
        this.restrictedSpecificFolders = arrayListOf(
                ".settings", "$RECYCLE.BIN");
        this.restrictedSpecificFolders.addAll(
                configuration.asList("filesystem.restricted.folders"));
    }
    
    boolean isProjectDefinitiveElement(Path item) {
        if ( isDirectory(item) ) {
            return containsIgnoreCaseAnyFragment(asName(item), this.projectDefinitiveFolderSigns);
        } else {
            return containsIgnoreCaseAnyFragment(asName(item), this.projectDefinitiveFiles);
        }        
    }
    
    boolean isProjectSpecificElement(Path item) {
        if ( isDirectory(item) ) {
            return containsIgnoreCaseAnyFragment(asName(item), this.projectSpecificFolderSigns);
        } else {
            return containsIgnoreCaseAnyFragment(asName(item), this.projectSpecificFileSigns);
        }
    }
    
    boolean isRestrictedFolder(Path folder) {
        return containsWordInIgnoreCase(this.restrictedSpecificFolders, asName(folder));
    }
    
    boolean isProgramSpecificElement(Path item) {
        if ( isDirectory(item) ) {
            return containsIgnoreCaseAnyFragment(asName(item), this.programSpecificFolders);
        } else {
            return containsIgnoreCaseAnyFragment(asName(item), this.programSpecificFileSigns);
        }
    }
}
