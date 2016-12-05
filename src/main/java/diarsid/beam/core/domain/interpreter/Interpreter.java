/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.interpreter;

import diarsid.beam.core.domain.interpreter.recognizers.SingleWordRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.SingleWordSlashRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.BatchCallArgsRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.CreationOperationRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.DefaultRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.ExecutorCommandRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.InputCorrectnessRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.ListingRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.LocationCallArgsRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.LocationCreationRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.LocationRemovingArgsRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.NotesRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.PageCallArgsRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.PageCreationArgsRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.PageRemovingArgsRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.ProgramCallArgsRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.QuestionRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.RemovingOperationRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.TaskCreationArgumentsRecognizer;
import diarsid.beam.core.domain.interpreter.recognizers.TaskRemovingArgsRecognizer; 

import static diarsid.beam.core.domain.interpreter.RecognizerPriority.LOW;
import static diarsid.beam.core.domain.interpreter.RecognizerPriority.LOWEST;
import static diarsid.beam.core.domain.interpreter.RecognizerPriority.slightlyLowerThan;
import static diarsid.beam.core.domain.interpreter.recognizers.BatchRecognizer.batch;
import static diarsid.beam.core.domain.interpreter.recognizers.LocationRecognizer.location;
import static diarsid.beam.core.domain.interpreter.recognizers.PageRecognizer.page;
import static diarsid.beam.core.domain.interpreter.recognizers.TaskRecognizer.task;
import static diarsid.beam.core.util.StringUtils.normalize;

/**
 *
 * @author Diarsid
 */
public class Interpreter {
    
    private final Recognizer root;
    
    public Interpreter() {        
        this.root = this.prepareRecognizersTree();
    }
    
    private Recognizer prepareRecognizersTree() {
        IntermediateRecognizer input = new InputCorrectnessRecognizer();
        IntermediateRecognizer executor = new ExecutorCommandRecognizer();
        IntermediateRecognizer creation = new CreationOperationRecognizer();
        IntermediateRecognizer removing = new RemovingOperationRecognizer();        
        IntermediateRecognizer questions = new QuestionRecognizer();
        IntermediateRecognizer listing = new ListingRecognizer();
        
        Recognizer notes = new NotesRecognizer();
        
        Recognizer taskCreationArgs = new TaskCreationArgumentsRecognizer();
        Recognizer locationCreationArgs = new LocationCreationRecognizer();
        Recognizer pageCreationArgs = new PageCreationArgsRecognizer();
        
        Recognizer taskRemovingArgs = new TaskRemovingArgsRecognizer();
        Recognizer pageRemovingArgs = new PageRemovingArgsRecognizer();
        Recognizer locationRemovingArgs = new LocationRemovingArgsRecognizer();
        
        IntermediateRecognizer defaultRecognizer = new DefaultRecognizer();
        
        Recognizer pageCallArgs = new PageCallArgsRecognizer();
        Recognizer locationCallArgs = new LocationCallArgsRecognizer();
        Recognizer batchCallArgs = new BatchCallArgsRecognizer();
        Recognizer programCallArgs = new ProgramCallArgsRecognizer();
        
        Recognizer wordRecognizer = new SingleWordRecognizer();
        Recognizer slashRecognizer = new SingleWordSlashRecognizer();
        
        return input.branchesTo(
                executor.branchesTo(
                        pageCallArgs, 
                        locationCallArgs, 
                        batchCallArgs, 
                        programCallArgs), 
                creation.branchesTo(
                        location().branchesTo(locationCreationArgs),
                        task().branchesTo(taskCreationArgs),
                        page().branchesTo(pageCreationArgs),
                        batch().branchesTo()
                ),
                removing.branchesTo(
                        location().branchesTo(locationRemovingArgs),
                        task().branchesTo(taskRemovingArgs),
                        page().branchesTo(pageRemovingArgs),
                        batch().branchesTo()
                ),
                questions.branchesTo(
                        task().branchesTo(),  
                        location().branchesTo(),
                        page().branchesTo(),
                        batch().branchesTo()
                ),
                listing.branchesTo(),
                notes.withPriority(LOW),
                defaultRecognizer.withPriority(slightlyLowerThan(LOWEST)).branchesTo(
                        slashRecognizer, 
                        wordRecognizer)
        );
    }
    
    public void interprete(String inputString) {
        this.root.assess(new Input(normalize(inputString)));
    }
}
