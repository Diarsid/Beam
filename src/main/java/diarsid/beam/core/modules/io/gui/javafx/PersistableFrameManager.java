/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx;

import java.util.function.Function;

import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.modules.data.DaoNamedRectangles;
import diarsid.beam.core.modules.io.gui.geometry.MutableNamedRectangle;
import diarsid.beam.core.modules.io.gui.geometry.RealMutableNamedRectangle;
import diarsid.beam.core.modules.io.gui.geometry.Screen;

import static java.lang.String.format;

import static diarsid.support.log.Logging.logFor;

/**
 *
 * @author Diarsid
 */
public class PersistableFrameManager {
    
    private final Screen screen;
    private final DaoNamedRectangles daoNamedRectangles;
    private final Function<MutableNamedRectangle, Boolean> persistRectangleCall;

    PersistableFrameManager(Screen screen, DaoNamedRectangles daoNamedRectangles) {
        this.screen = screen;
        this.daoNamedRectangles = daoNamedRectangles;
        this.persistRectangleCall = (rectangle) -> {
            try {
                boolean saved = this.daoNamedRectangles.save(rectangle);            
                if ( ! saved ) {
                    logFor(this).error(format("%s have not been saved!", rectangle));
                }
                return saved;
            } catch (DataExtractionException e) {
                logFor(this).error(format("Cannot save %s!", rectangle), e);
                return false;
            }
        };
    }
    
    public WindowPersistableFrame get(String name) throws DataExtractionException {
        MutableNamedRectangle rectangle = new RealMutableNamedRectangle(name);
        boolean dataFetched = this.daoNamedRectangles.fetchDataInto(rectangle);
        if ( dataFetched ) {
            logFor(this).info("fetched: " + rectangle);
            boolean changedOnFitInScreen = this.screen.fit(rectangle);
            if ( changedOnFitInScreen ) {
                this.daoNamedRectangles.save(rectangle);
            }
        } else {
            logFor(this).info(format("rectangle '%s' not found.", name));
        }
        return new WindowPersistableFrame(rectangle, dataFetched, this.persistRectangleCall);
    }
    
}
