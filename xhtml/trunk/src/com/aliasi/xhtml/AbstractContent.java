package com.aliasi.xhtml;

import java.util.Set;

abstract class AbstractContent implements Content {

    abstract void propagateMask(int parentMask);

    abstract boolean isCyclic(Set visitedSet);

}