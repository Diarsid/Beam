/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.actions;

/**
 *
 * @author Diarsid
 */
@FunctionalInterface
public interface EmptyCallback extends Callback {
    void call();
}
