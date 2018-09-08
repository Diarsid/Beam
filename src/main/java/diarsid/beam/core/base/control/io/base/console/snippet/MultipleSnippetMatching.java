/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console.snippet;

import java.util.List;

import static java.util.Arrays.asList;


class MultipleSnippetMatching implements SnippetMatching {
    
    private final List<SnippetMatching> matchings;
    
    private MultipleSnippetMatching(List<SnippetMatching> matchings) {
        this.matchings = matchings;
    }
    
    static SnippetMatching matchesAll(SnippetMatching... otherMatchings) {
        return new MultipleSnippetMatching(asList(otherMatchings));
    }

    @Override
    public boolean matches(String line) {
        for (int i = 0; i < this.matchings.size(); i++) {
            if ( ! this.matchings.get(i).matches(line) ) {
                return false;
            }
        }
        return true;
    }
    
}
