/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.jdbc.transactions.PerRowConversion;

import static diarsid.beam.core.base.control.io.commands.Commands.restoreExecutorCommandFrom;
import static diarsid.beam.core.domain.entities.WebPages.restorePage;

/**
 *
 * @author Diarsid
 */
class RowToEntityConversions {
    
    static final PerRowConversion<Location> ROW_TO_LOCATION  = (row) -> {
        return new Location(
                (String) row.get("loc_name"),
                (String) row.get("loc_path"));
    };
    
    static final PerRowConversion<ExecutorCommand> ROW_TO_COMMAND = (row) -> {
        return restoreExecutorCommandFrom(
                (String) row.get("bat_command_type"), 
                (String) row.get("bat_command_original")
        );
    };
    
    static final PerRowConversion<WebPage> ROW_TO_PAGE = (row) -> {
        return restorePage(
                (String) row.get("name"),
                (String) row.get("shortcuts"),
                (String) row.get("url"),
                (int) row.get("ordering"),
                (int) row.get("dir_id"));
    };
}
