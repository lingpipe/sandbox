		TREC 2004 check input utilities

The check_* family of PERL scripts check for errors in a
TREC 2004 submission.  The list of errors and warnings they issue
is given below.  A submission that contains an error will be rejected 
by the automatic submission processing system.  Submissions that cause
warnings to be issued are acceptable since the warnings simply recognize
situations that occur frequently in runs that are incorrect.

The novelty and robust track scripts require an additional
data file as part of the check_input process.  For the 
robust track, the file contains a list of all the document ids in
the collection; for the novelty track, the file is a set of
document ids and sentence counts.  These files should be
downloaded from the TREC web site.  The name of the data file
and the directory in which it exists are variables in the scripts.
You will need to edit the scripts to reflect your setup (see below). 

Since the submission requirements for the tracks differ from one
another, there is a different script for each track.
    check_genomics.pl: script for runs to be submitted to the
	genome track.  It takes two arguments, the task 
        ("adhoc", or "cat") and the name of the file that contains
        the run results.  Since the categorization task has not been
        completely defined yet, the script only checks ad hoc task runs.
    check_hard.pl: script for runs submitted to the HARD track.
	It takes a single argument, the name of the file that
	contains the run results.
    check_novelty.pl: script for runs submitted to the novelty track.
	It takes two arguments, the task ("task1", "task2", "task3"
        or "task4") and the name of the file that contains
  	the run results.  Requires a data file (default name "novelty").
    check_qa.pl: script for runs to be submitted to the question
        answering track.  It takes one argument, the name of the file
	that contains the run results.
    check_robust.pl: script for runs submitted to the robust
	retrieval track.  It takes one argument, the name of
        the file that contains the run results.  Requires
	the file docnos.robust .
    check_terabyte.pl: script for runs submitted to the terabyte
        track.  It contains one argument, the name of the
	file that contains the run results.
    check_web.pl: script for runs to be submitted to the 
        web track.  It takes two arguments, the task ("mixed",
	"enterprise", or "classification") and the name of the file
        that contains the run results.

The scripts are designed to continue to check the input after
encountering (most) errors, but they stop after 25 errors so as
not to waste time and disk space on cascading errors.  They print
the message "Finished processing <results-file-name>" to both
standard error and a log file when the end of the script is reached.
If a script should quit before the end, a Perl message similar to
    "Died at <script-name> line <xxx>, <RESULTS> chunk <yyy>"
will be printed.  The log file will contain the script's error
messages, which should be much more informative.

All the scripts are written using Perl.  See
	http://language.perl.com/info/software.html
for a variety of ways to obtain Perl.

Possibly required modifications to the check_*.pl scripts
------------------------------------------------------------
The scripts have only been tested on a linux machine.
Each prints errors and warnings into a log file.

    * The scripts assume the Perl executable is located at
      /usr/bin/perl.  Modify the first line to be the actual
      path of the executable, or invoke Perl from the command line
      and feed it the script.

    * check_novelty and check_robust assume a data file of a
      default name exists in a default directory.  The names are
      contained in the variables docno_dir and docnos_file that are
      set at the beginning of each script (immediately after the
      comment that says "Change these variables...").  The default
      value of the directory is "/runs/aux", which is where docno files
      reside at NIST, so this will have to be changed to reflect
      its real location.

    * Each script defines a path for the log file it creates.
      When invoked with a results-file named "foo", the error log
      will be named "foo.errlog" and will be put in the directory
      specified by the variable "errlog_dir" (defined immediately
      after docno_dir is defined in the scripts).  errlog_dir
      defaults to ".", so the log file will be put in the directory
      in which the perl script is invoked by default.
      (The script must have write permission in this directory.)

    * Most of the scripts require that the results file be sorted 
      by some particular combination of columns.  A script will do the sort
      itself on unix machines, but this sort may not work on other machines.
      If you do not want the script to do the sorting, comment
      out (start the line with a # sign) the code immediately after
      the "Sort the input file..." comment and uncomment
      (remove the # sign from) the subsequent lines.
      If you use the sort, note that the line numbers given in the
      error log refer to the line in the SORTED file, not (necessarily)
      in the original input file.  IF YOU REMOVE THE SORT, MAKE SURE
      YOU GIVE IT A SORTED RESULTS FILE.


Errors and Warnings:
--------------------
In the list of errors below, "topic" should be understood to include
QA "question".

    Errors
    ------
    * "Answer string given when docid is NIL" --- the document id is
      specified as NIL (signifying no answer in the collection),
      yet a non-empty answer is provided.  [QA track only.]

    * "Both offset and length must be -1 to indicate a NULL passage" ---
      exactly one of passage offset and passage length is -1, and
      both must be -1 to specify the entire document is retrieved.
      [HARD track only.]

    * "Difficulty predictions are not a strict ordering between 1 and 250" ---
      two topics were assigned the same difficulty ranking.
      [Robust track only.]

    * "Document `<xxx>' not valid for topic <t>" --- the given document
      is not one of the documents to be used for this topic.
      [Novelty track only.]

    * "Document `<xxx>' retrieved more than once for topic <t> --- the
      given document appears more than once in the ranking for
      a single topic.  The current line is the line of the
      second (or subsequent) occurrence in the sorted file.
      [Does not apply to QA, HARD, or novelty runs.]

    * "Field 2 is `<xxx>' not `Q0'" --- the second field for many
      tracks is expected to be the literal Q0.  The second field
      was once used to show an iteration number, but it is no
      longer used.  Nonetheless, existing code expects a `Q0'.

    * "Invalid classification value (<x>)" --- the category
      assigned to a topic in the web track is something other than
      'NP', 'HP', or 'TD'. [Web track classification task only.]

    * "Invalid difficulty prediction <x>" --- robust track difficulty
      predictions rank the test set of questions from least difficult
      to most difficult.  Thus, a prediction must be an integer between
      1 and 250 inclusive, and the prediction on the current
      line is out of range.  [Robust track only.]

    * "Invalid string (<xxx>) for topic number" --- the topic field
      contained something that did not match the legal format of
      a topic id for this task. (Note this might have broken the
      sort routine, so if this error occurs, other spurious errors
      might also occur.) 

    * "Invalid target number (<q>)" --- The number supplied as a question's
      target number is not the number of a valid target.  [QA track only.]

    * "Invalid type ($type): type must be one of 'relevant'
      or 'new'" --- The second field of a novelty track submission
      must be exactly one of "relevant" or "new".  [Novelty
      track only.] 

    * "Missing answer for question <q>" --- the answer text
      field of an input line has length 0.  The most likely way to get
      this message is to have the wrong number of fields per input line.
      [Applies only to QA runs, and is not issued if docno is "NIL".]

    * Multiple categories assigned to topic <t>" --- Topic t was assigned
      more than one category; all topics must be assigned to exactly
      one category of 'NP', 'HP', or 'TD'. [Web track classification task
      only.]

    * NIL response not allowed for <xxx> question" --- The question is 
      a 'list' or 'other' question.  An answer is guaranteed to exist
      in the collection for these answer types, and thus NIL is
      an invalid response for a question of this type. [QA
      track only.]

    * No category assigned to topic <t>" --- Topic t was not
      assigned a category; all topics must be assigned to exactly
      one category of 'NP', 'HP', or 'TD'. [Web track classification task
      only.]

    * "No documents retrieved for topic <t>" or "No items retrieved
      for topic <t>" or "No answer given for question <x.y> --- the results
      file has no entries for a topic that is in the test set.  Runs must
      produce at least one response for the entire test set.  [Does not apply
      to novelty runs.]

    * "No relevant sentences should be returned for task <xxx>" ---
      xxx is either task2 or task4 of the novelty track.  For these
      tasks, the relevant sentences are given, so none should
      be returned in the result set.

    * "Passage length must be an integer, not '<xxx>'" --- xxx is an
      illegal string for a passage length. [HARD track only.]

    * Passage length must be -1 or a positive integer, not <xxx>" ---
      xxx is 0 or a negative integer other than -1, which is an illegal
      value for a passage length.  [HARD track only.]

    * "Passage offset must be an integer, not '<xxx>'" --- xxx is an
      illegal string for a passage offset. [HARD track only.]

    * Passage offset must be -1 or a non-negative integer, not <xxx>" ---
      xxx is a negative integer other than -1, which is an illegal
      value for a passage offset.  [HARD track only.]

    * "Run tag `<xxx>' is malformed" --- the run tag <xxx> is either the
      empty string, or it contains characters other than upper and lower
      case letters and digits, or it is longer than 12 characters.
      NIST requires run tags to contain no punctuation because punctuation
      invariably causes plotting software to break (the tags are used
      as graph labels).  An empty run tag is frequently the symptom
      of the wrong number of fields on the line.

    * "Run tag inconsistent (`<yyy>' and `<xxx>')" --- the first line
      had a run tag of xxx and the current line had a run tag of yyy.
      The tag must be identical on each line.

    * "Sentence <xxx> in document <yyy> retrieved in new set but not
      in relevant set for topic <t>" --- The set of docno,sentence-number
      pairs in the "new" set of a topic is required to be a subset
      of the pairs given in the "relevant" set for that topic.
      [Novelty track task1 only.]

    * "Sentence <xxx> in document <yyy> retrieved more than once in relevant
      set for topic <t>" ---  The same docno,sentence-number pair was
      retrieved multiple times within the set of "relevant" sentences
      for the given topic. [Novelty track only.]

    * "Sentence <xxx> in document <yyy> retrieved more than once in new
      set for topic <t>" ---  The same docno,sentence-number pair was
      retrieved multiple times within the set of "new" sentences
      for the given topic. [Novelty track only.]

    * "Sentence <xxx> invalid sentence for document <yyy>" --- the sentence
      number given is either <= 0 or > the maximum number of sentences
      in document yyy. [Novelty track only.]

    * Too many answers given for factoid question <x.y>" ---
      Too many responses returned for a factoid question.
      Exactly one response per factoid question is required.
      [QA track only.]

    * "Too many documents (<xxx>) retrieved for topic <t>" or 
      "Too many items (<xxx>) retrieved for topic <t> --- the number of items
      retrieved/answers supplied for the given topic, xxx, exceeds
      the maximum allowed.  [Does not apply to novelty runs.]

    * "Too many fields" --- a line in the file contains more than 
      the expected number of fields.  (Note: too few fields is
      usually recognized as an error in the run tag.  However,
      the wrong number of fields may screw up the sort, so other
      reported error messages and/or reported line numbers may
      be spurious.)  Can't be detected for QA runs since the answer
      string contains embedded white space.

    * "Too many fields in prediction line" --- a line in the second
      part of a robust track run (the prediction of difficulty) 
      contains too many fields. [Robust track only.]

    * "Topic <xxx> not used for <run-type>" ---  the topic given in the
      current line is not a topic to be used for the given task.

    * "Unknown document `<xxx>'" --- the given docno, xxx, is not in
      the collection for the task.  Note that docnos are case
      sensitive!  NIL (but not nil) is accepted as a docno
      for the QA track.  For all but the robust and novelty tracks,
      the check is only an approximate pattern match that
      accepts all valid docnos but also other strings.
      The pattern match is able to catch that the wrong document
      collection was used, but not more subtle problems.
      (The string match is used for these runs because the number
      of valid docnos is large enough that explicit checks are
      exceedingly painful.)

    * "Unknown topic (<xxx>)" ---  the given topic id matches the format
      of a topic id, but is not a topic that is used for this task this year.


    Warnings
    --------

    * "nothing returned for topic <t>" --- no relevant and thus no
      new sentences were returned for topic t.  [Novelty track only].

    * "only <xxx> documents retrieved for topic <t>" ---  the number
      of documents retrieved for topic t, xxx, is less than the
      maximum allowed.  This is a useful check for groups who expected
      the run to retrieve the maximum for each topic.  (trec_eval
      counts empty ranks as if they contained an irrelevant document.
      So, a run's score cannot be hurt by retrieving the maximum
      for each topic.  However, a run is not required to retrieve the
      maximum for each topic.)

Notes
-----

Because the list of topic numbers and correct DOCNOs are an integral
part of the checking process, the scripts work only for TREC 2004
submissions.

These scripts are the latest in a series of scripts NIST has used
internally to check results.  They are very similar to scripts
used in previous years, though obviously changed to reflect this year's
guidelines.  Since we don't yet have any TREC  2004 runs,
something may have slipped through the cracks during our testing. 
Let me know if you see questionable behavior (ellen.voorhees@nist.gov).

