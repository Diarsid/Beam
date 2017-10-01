/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;

/**
 *
 * @author Diarsid
 */
public class TimeParser {
    
    private final Set<TimePatternParser> parsers;
    
    TimeParser(Set<TimePatternParser> parsers) {
        this.parsers = parsers;
    }
    
    public List<String> timePatterns() {
        return this.parsers
                .stream()
                .flatMap(parser -> parser.timePatterns().stream())
                .collect(toList());
    }
    
    public Optional<Time> parse(String timePattern) {
        Optional<Time> time;
        for (TimePatternParser parser : this.parsers) {
            time = parser.parse(timePattern);
            if ( time.isPresent() ) {
                return time;
            }
        }
        return Optional.empty();
    }
    
    public TimeAndText parse(List<String> args) {
        if ( hasOne(args) ) {
            Optional<Time> probableTime = this.parse(getOne(args));
            if ( probableTime.isPresent() ) {
                return new TimeAndText(probableTime.get());
            } else {
                return new TimeAndText(getOne(args));
            }
        } else {            
            String twoArgTimePattern = join(" ", args.get(0), args.get(1));            
            Optional<Time> probableTwoArgTime = this.parse(twoArgTimePattern);
            if ( probableTwoArgTime.isPresent() ) {
                return new TimeAndText(probableTwoArgTime.get(), join(" ", args.subList(2, args.size())));
            } else {
                String oneArgTimePattern = getOne(args);
                Optional<Time> probableOneArgTime = this.parse(oneArgTimePattern);
                if ( probableOneArgTime.isPresent() ) {
                    return new TimeAndText(probableOneArgTime.get(), join(" ", args.subList(1, args.size())));                    
                } else {
                    return new TimeAndText(join(" ", args));
                }
            }
        }
    }
}
