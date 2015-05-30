/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/29/2015
 */

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
