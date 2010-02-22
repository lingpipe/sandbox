package edu.uchicago.rzhetsky.cfg;

public class LexEntry {

    private final String mMother;
    private final String mWord;

    LexEntry(String mother, 
             String word) {
        mMother = mother;
        mWord = word;
    }

    public String mother() {
        return mMother;
    }

    public String word() {
        return mWord;
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof LexEntry))
            return false;
        @SuppressWarnings("unchecked")
        LexEntry thatEntry
            = (LexEntry) that;
        return mother().equals(thatEntry.mother())
            && word().equals(thatEntry.word());
    }

    @Override
    public String toString() {
        return mother() + " -> |" + word() + "|";
    }

    public static LexEntry entry(String mother,
                                 String word) {
        return new LexEntry(mother,word);
    }

}