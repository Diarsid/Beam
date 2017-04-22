/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.domain.entities.NamedEntityType;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.jdbc.transactions.PerRowConversion;

import static java.lang.String.format;

import static diarsid.beam.core.base.control.io.commands.Commands.restoreExecutorCommandFrom;
import static diarsid.beam.core.domain.entities.NamedEntityType.fromString;
import static diarsid.beam.core.domain.entities.WebPages.restorePage;

/**
 *
 * @author Diarsid
 */
class RowToEntityConversions {
    
    static final PerRowConversion<NamedEntity> ROW_TO_NAMED_ENTITY = (row) -> {
        return new NamedEntity() {

            private final String name = (String) row.get("entity_name");
            private final NamedEntityType type = fromString((String) row.get("entity_type"));

            @Override
            public String name() {
                return this.name;
            }

            @Override
            public NamedEntityType type() {
                return this.type;
            }

            @Override
            public Variant toVariant(int variantIndex) {
                return new Variant(
                        this.name,
                        format("%s (%s)", this.name, this.type.displayName()), 
                        variantIndex);
            }
        };
    };
    
    static final PerRowConversion<Location> ROW_TO_LOCATION  = (row) -> {
        return new Location(
                (String) row.get("loc_name"),
                (String) row.get("loc_path"));
    };
    
    static final PerRowConversion<WebPage> ROW_TO_PAGE = (row) -> {
        return restorePage(
                (String) row.get("name"),
                (String) row.get("shortcuts"),
                (String) row.get("url"),
                (int) row.get("ordering"),
                (int) row.get("dir_id"));
    };
    
    static final PerRowConversion<ExecutorCommand> ROW_TO_COMMAND = (row) -> {
        return restoreExecutorCommandFrom(
                (String) row.get("bat_command_type"), 
                (String) row.get("bat_command_original")
        );
    };
}
