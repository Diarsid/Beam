/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.geometry;

import java.util.Objects;

import diarsid.support.objects.Possible;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import static diarsid.support.objects.Possibles.possibleButEmpty;
import static diarsid.support.objects.Possibles.possibleWith;

/**
 *
 * @author Diarsid
 */
public class RealMutableRectangle implements MutableRectangle {

    static class RealMutableRectangleAnchor implements MutableRectangleAnchor {

        RealMutableRectangleAnchor() {
        }
        
        RealMutableRectangleAnchor(RectangleAnchor anchor) {
            this.x = anchor.x();
            this.y = anchor.y();
        }
        
        RealMutableRectangleAnchor(double x, double y) {
            this.x = x;
            this.y = y;
        }

        protected Double x;
        protected Double y;

        @Override
        public Double x() {
            return this.x;
        }

        @Override
        public Double y() {
            return this.y;
        }

        @Override
        public boolean isEmpty() {
            return isNull(this.x) || isNull(this.y);
        }

        @Override
        public boolean isNotEmpty() {
            return nonNull(this.x) && nonNull(this.y);
        }

        @Override
        public void set(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public RectangleAnchor asImmutable() {
            return this;
        }

        @Override
        public void setX(double x) {
            this.x = x;
        }

        @Override
        public void setY(double y) {
            this.y = y;
        }

        @Override
        public void set(Point point) {
            this.set(point.x(), point.y());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + Objects.hashCode(this.x);
            hash = 41 * hash + Objects.hashCode(this.y);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj ) {
                return true;
            }
            if ( obj == null ) {
                return false;
            }
            if ( getClass() != obj.getClass() ) {
                return false;
            }
            final RealMutableRectangleAnchor other = ( RealMutableRectangleAnchor ) obj;
            if ( !Objects.equals(this.x, other.x) ) {
                return false;
            }
            if ( !Objects.equals(this.y, other.y) ) {
                return false;
            }
            return true;
        }

    }

    static class RealMutableRectangleSize implements MutableRectangleSize {

        RealMutableRectangleSize() {
        }
        
        RealMutableRectangleSize(RectangleSize size) {
            this.width = size.width();
            this.height = size.height();
        }
        
        RealMutableRectangleSize(double width, double height) {
            this.width = width;
            this.height = height;
        }

        protected Double width;
        protected Double height;

        @Override
        public Double width() {
            return this.width;
        }

        @Override
        public Double height() {
            return this.height;
        }

        @Override
        public boolean isEmpty() {
            return isNull(this.height) || isNull(this.width);
        }

        @Override
        public boolean isNotEmpty() {
            return nonNull(this.height) && nonNull(this.width);
        }

        @Override
        public void set(double width, double height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void set(RectangleSize otherSize) {
            this.width = otherSize.width();
            this.height = otherSize.height();
        }

        @Override
        public RectangleSize asImmutable() {
            return this;
        }

        @Override
        public void setWidth(double width) {
            this.width = width;
        }

        @Override
        public void setHeight(double height) {
            this.height = height;
        }

    }

    final RealMutableRectangleAnchor anchor;
    final RealMutableRectangleSize size;
    final Possible<RectangleSize> minSize;

    RealMutableRectangle() {
        this.anchor = new RealMutableRectangleAnchor();
        this.size = new RealMutableRectangleSize();
        this.minSize = possibleButEmpty();
    }
    
    RealMutableRectangle(RectangleAnchor givenAnchor, RectangleSize givenSize) {
        this.anchor = new RealMutableRectangleAnchor(givenAnchor);
        this.size = new RealMutableRectangleSize(givenSize);
        this.minSize = possibleButEmpty();
    }
    
    RealMutableRectangle(double x, double y, double width, double height) {
        this.anchor = new RealMutableRectangleAnchor(x, y);
        this.size = new RealMutableRectangleSize(width, height);
        this.minSize = possibleButEmpty();
    }
    
    RealMutableRectangle(
            RectangleAnchor givenAnchor, RectangleSize givenSize, RectangleSize minSize) {
        this.anchor = new RealMutableRectangleAnchor(givenAnchor);
        this.size = new RealMutableRectangleSize(givenSize);
        this.minSize = possibleWith(minSize);
    }
    
    RealMutableRectangle(
            double x, double y, double width, double height, double minWidth, double minHeight) {
        this.anchor = new RealMutableRectangleAnchor(x, y);
        this.size = new RealMutableRectangleSize(width, height);
        this.minSize = possibleWith(new RealMutableRectangleSize(minWidth, minHeight));
    }

    @Override
    public String toString() {
        return format("%s[x:%s, y:%s, width:%s, height:%s]", 
                      this.getClass().getSimpleName(), 
                      this.anchor.x, 
                      this.anchor.y,
                      this.size.width,
                      this.size.height);
    }

    @Override
    public Rectangle asImmutable() {
        return this;
    }

    @Override
    public MutableRectangleAnchor anchor() {
        return this.anchor;
    }

    @Override
    public MutableRectangleSize size() {
        return this.size;
    }

    @Override
    public Possible<RectangleSize> minSize() {
        return this.minSize;
    }

    protected Object[] params() {
        return new Object[] {
            this.anchor.x, this.anchor.y, this.size.width, this.size.height};
    }
        
}
