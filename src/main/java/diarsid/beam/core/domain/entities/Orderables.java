/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.util.List;

import static java.util.Collections.sort;

/**
 *
 * @author Diarsid
 */
public class Orderables {
    
    public Orderables() {
    }
    
    public static void reorderAccordingToNewOrder(
            List<? extends Orderable> items, int itemOldOrder, int itemNewOrder) {
        int collectionSize = items.size();
        if ( itemNewOrder == itemOldOrder ) {
            // do not sort collection at all.
            return;
        }
        itemOldOrder = adjustHighLimit(itemOldOrder, collectionSize);
        itemOldOrder = adjustLowLimit(itemOldOrder);
        itemNewOrder = adjustHighLimit(itemNewOrder, collectionSize);
        itemNewOrder = adjustLowLimit(itemNewOrder); 
        
        if ( itemOldOrder < itemNewOrder ) {
            /*
             * Items:
             *   1 2 3 4 5 6 
             * Let us move ordered elemnt(e) at position 2 (old order) 
             * to position 5 (new order) :
             *   1 2 3 4 5 6
             *     e----->
             * We could achieve desirable result through next two steps:
             * 1) set new order(5) to element with order 2:
             *   1 5 3 4 5 6
             *     ^
             * 2) let us decrease order values for elements between 2 
             * (excluding) and 5 (including):
             *   1 5 2 3 4 6
             *       ^ ^ ^
             * As this algorithm will be applied only to managed JPA entities,
             * changing of their order values will be sufficient to reorder them.
             */
            // step 1.
            items.get(itemOldOrder).setOrder(itemNewOrder);
            // step 2.
            for (int i = itemOldOrder + 1; i <= itemNewOrder; i++) {
                items.get(i).setOrder(i - 1);
            }
            sort(items);
        } else {
            /*
             * itemOldOrder > itemNewOrder
             * 
             * Items:
             *   1 2 3 4 5 6
             * Let us move ordered element(e) at position 5 (old order) 
             * to position 2 (new order) :
             *   1 2 3 4 5 6
             *     <-----e
             * We could achieve desirable result through next three steps:
             * 1) set new order(2) to element with order 5:
             *   1 2 3 4 2 6
             *           ^
             * 2) let us increase order values for elements between 2 
             * (including) and 5 (excluding):
             *   1 3 4 5 2 6
             *     ^ ^ ^
             * As this algorithm will be applied only to managed JPA entities,
             * changing of their order values will be sufficient to reorder them.
             */
            // step 1.
            items.get(itemOldOrder).setOrder(itemNewOrder);
            // step 2.
            for (int i = itemNewOrder; i < itemOldOrder; i++) {
                items.get(i).setOrder(i + 1);
            }
            sort(items);
        }
    }
    
    public static void reorderToExtractWebItemLater(List<? extends Orderable> items, int itemOrder) {
        itemOrder = adjustHighLimit(itemOrder, items.size());
        itemOrder = adjustLowLimit(itemOrder);
        /*
         * Items:
         *   1 2 3 4 5 6
         * 
         * Let us delete element at position 3.
         *   1 2 3 4 5 6 
         *       x
         * We need reorder items after it:
         *   1 2 x 4 5 6 -> 1 2 x 3 4 5
         * To achieve this result we should decrease order values of all elements 
         * having order values higher than one that has been removed:
         *   1 2 x 4 5 6 -> 1 2 x 3 4 5
         *         ^ ^ ^          ^ ^ ^
         */
        for (int i = itemOrder + 1; i < items.size(); i ++) {
            items.get(i).setOrder(i - 1);
        }
    }
    
    public static void reorderToInsertWebItemLater(
            List<? extends Orderable> items, Orderable newItem, int itemNewOrder) {
        /*
         * Items:
         *   1 2 3 4 5
         * 
         * Let us insert new item into position 3.
         *   1 2 e 3 4 5 
         *       ^
         * 1) Increment order values of all elements with order >= 3
         *   1 2 4 5 6 
         *       ^ ^ ^
         * 2) Set new item order to new item:
         *   1 2 3 4 5 6
         *       ^      
         */
        if ( itemNewOrder >= items.size() ) {
            itemNewOrder = items.size();
        }
        itemNewOrder = adjustLowLimit(itemNewOrder);
        // 1)
        for (int i = itemNewOrder; i < items.size(); i++) {
            items.get(i).setOrder(i + 1); 
        }
        // 2)
        
        newItem.setOrder(itemNewOrder);
    }

    private static int adjustLowLimit(int itemOrder) {
        if ( itemOrder < 0 ) {
            return 0;
        } else {
            return itemOrder;
        }
    }

    private static int adjustHighLimit(int itemOrder, int collectionSize) {
        if ( itemOrder >= collectionSize ) {
            // place item to the end of the list
            return (collectionSize - 1);
        } else {
            return itemOrder;
        }
    }
}
