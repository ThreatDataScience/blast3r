package com.razware.blast3r.system;

import java.util.ArrayList;

/**
 * Created by abreksa on 5/28/15.
 */
public class UniqueList<E> extends ArrayList<E> {
    @Override
    public boolean add(E e) {
        if (!this.contains(e)) {
            return super.add(e);
        }
        return false;
    }
}
