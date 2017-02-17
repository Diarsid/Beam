/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.inputparsing.time;

import java.util.Optional;

/**
 *
 * @author Diarsid
 */
interface TimePatternParser {
    
    Optional<TasksTime> parse(String timePattern);
}
