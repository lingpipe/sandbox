package edu.uchicago.rzhetsky.cfg;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Cfg {

    private final Set<Production> mProductionSet;
    private final Set<LexEntry> mLexEntrySet;

    public Cfg(Collection<Production> productions,
               Collection<LexEntry> lexEntries) {
        mProductionSet = new HashSet<Production>(productions);
        mLexEntrySet = new HashSet<LexEntry>(lexEntries);
    }

    public Set<Production> productions() {
        return Collections.unmodifiableSet(mProductionSet);
    }

    public Set<LexEntry> lexEntries() {
        return Collections.unmodifiableSet(mLexEntrySet);
    }
    


}