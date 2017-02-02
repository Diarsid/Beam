/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.tasks;

/**
 *
 * @author Diarsid
 */
public enum LagType {
    
    LAG_AFTER_INITIAL_START {
        @Override
        public boolean isShort(long minutes) {
            return ( minutes <= 30 );
        }
        
        @Override
        public boolean isNeitherShortNorLong(long minutes) {
            return ( 30 < minutes ) && ( minutes <= (60 * 24) );
        }
    },
    LAG_AFTER_TEMPORARY_PAUSE  {
        @Override
        public boolean isShort(long minutes) {
            return ( minutes <= 30 );
        }
        
        @Override
        public boolean isNeitherShortNorLong(long minutes) {
            return ( 30 < minutes ) && ( minutes <= (60 * 5) );
        }
    };
    
    abstract boolean isShort(long minutes);
    
    abstract boolean isNeitherShortNorLong(long minutes);
}
