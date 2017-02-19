/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

import java.util.List;
import java.util.Optional;

import static java.lang.String.join;

import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;

/**
 *
 * @author Diarsid
 */
public class TimeAndTextParser {
    
    private final TimePatternParsersHolder timePatternParsers;
    
    TimeAndTextParser(TimePatternParsersHolder timePatternParsers) {
        this.timePatternParsers = timePatternParsers;
    }
        
    public TasksTimeAndText parse(List<String> args) {
        if ( hasOne(args) ) {
            Optional<TaskTime> probableTime = this.timePatternParsers.parse(getOne(args));
            if ( probableTime.isPresent() ) {
                return new TasksTimeAndText(probableTime.get());
            } else {
                return new TasksTimeAndText(getOne(args));
            }
        } else {            
            String twoArgTimePattern = join(" ", args.get(0), args.get(1));            
            Optional<TaskTime> probableTwoArgTime = this.timePatternParsers.parse(twoArgTimePattern);
            if ( probableTwoArgTime.isPresent() ) {
                return new TasksTimeAndText(probableTwoArgTime.get(), join(" ", args.subList(2, args.size())));
            } else {
                String oneArgTimePattern = getOne(args);
                Optional<TaskTime> probableOneArgTime = this.timePatternParsers.parse(oneArgTimePattern);
                if ( probableOneArgTime.isPresent() ) {
                    return new TasksTimeAndText(probableOneArgTime.get(), join(" ", args.subList(1, args.size())));                    
                } else {
                    return new TasksTimeAndText(join(" ", args));
                }
            }
        }
    }
}
