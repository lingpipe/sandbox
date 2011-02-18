LingPipe Package for MITRE Challenge 2011
=================================================================

The Contest
--------------------------------

MITRE is hosting an unsupervised name-matching challenge.  The
home page is:

http://www.mitre.org/work/challenge/

From there, you can register for the challenge and download the data,
which consists of two lists of names, one long "index" file (about
825K names), and one short "query" file (around 9K names).  The task
is to rank potential matches in the index file for each name in the query
file.  

There is no gold standard as to which names actually match, so MITRE's
using a liberal notion of matching corresponding to "requires furtehr
human review to reject".  Furthermore, this notion of matching may change
as the task evolves.


This Distribution
--------------------------------

This directory contains baseline code for an entry based on character
n-grams.  The entry is set up so that the character n-gram scores are
used as a filter, which should have high sensitivity (low false
negative rate) for matches, though low specificity (high false
positive rate).  Parameters controlling its agressiveness may be
tuned.

If you download the data and unpack it into directory $DATADIR, you
can run the task as follows

% ant -Ddata-dir=$DATADIR ngrams

This will write a system response ready for submission to the
challenge in the default output directory /runs.


Three-Pass Matching
--------------------------------

Our approach is based on three passes. 

First pass: Create a character n-gram index and select potential pairs
based on having at least one n-gram in common.  The n-gram length is
parameterizable through the constant INDEX_NGRAM.  Setting it lower
increases run time but may increase sensitivity for obscure matches.

Second pass: Rank the first-pass possible matches by TF/IDF distance
over their character n-grams.  The range of n-grams is parameterizable
with MATCH_NGRAM_MIN and MATCH_NGRAM_MAX; setting these to 2 and 4
respectively produces matches based on 2-, 3-, and 4-grams.  TF/IDF
weighting will weight the less frequent n-grams more heavily.  The
maximum nubmer of candidates surviving this stage may be bounded by
setting the MAX_RESULTS_INDEX variable in the code.

Third pass: Arbitrary rescoring.  The method
    
     double rescore(String[],String[],double);

takes the original fields (first name/last name) as string arrays and
a double consisting of the n-gram match score, and allows an arbitrary
score to be returned.  As distributed, this method just returns the
score passed in.  The maximum number of results surviving the final
ranking is determiend by the variable MAX_RESULTS.


