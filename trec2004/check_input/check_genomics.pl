#!/usr//bin/perl -w

use strict;

# Check a TREC 2004 genomics track adhoc task submission for various
# common errors:
#      * extra fields
#      * multiple run tags
#      * missing or extraneous topics
#      * invalid retrieved documents
#      * duplicate retrieved documents in a single topic
#      * too many documents retrieved for a topic
#      * fewer than maximum allowed retrieved for a topic (warning)
# Messages regarding submission are printed to an error log

# Results input file is in the form
#     topic_num Q0 docno rank sim tag
# Script uses UNIX sort routine to ensure input is sorted by increasing
# topic number and decreasing sim.  If run on non-unix system,
# use alternate open command, but make sure input file is sorted
# Note that line numbers in the error output refer to the SORTED file!

# Change this variable to the directory where the error log should be put
my $errlog_dir = ".";

# If more than 25 errors, then stop processing; something drastically
# wrong with the file.
my $MAX_ERRORS = 25; 

my $MINQ = 1;
my $MAXQ = 50;
my $MAX_RET = 1000;

my $task;               # task run is submitted to (argument)
my $results_file;       # results file (argument)
my %topic_docnos;	# has of docs retrived for current topic
my ($topic_string,$q0,$docno,$rank,$sim,$tag,$rest);
my @num_ret;		# number of docs retrieved per topic
my $errlog;             # name of error log file
my $line;               # line from input file;
my ($topic, $old_topic, $t);
my $run_id;
my $found;
my ($q0warn, $num_errors, $line_num);
my ($last_i, $i);


$#ARGV == 1 ||
	die "Usage: $0 task resultsfile\n\twhere task is either 'adhoc' or 'cat'.\n";

$task = $ARGV[0];
if ($task ne "adhoc" && $task ne "cat") {
    die "Task must be either 'adhoc' or 'cat', not '$task'\n";
}
$results_file = $ARGV[1];

if ($task eq "cat") {
    print "Format of categorization task not yet specified.\n";
    exit 0;
}


for ($t=$MINQ; $t<=$MAXQ; $t++) {
    $num_ret[$t] = 0;
}

# Sort the input file by topic_num, sim and read result
# ASSUMES UNIX; FOR non-unix, comment out this open, and use
# alternate open --- make sure file is sorted!
open RESULTS, "sort +0 -1 +4 -5gr $results_file |" ||
	die "Unable to open (or sort) results file $results_file: $!\n";
#open RESULTS, "<$results_file" ||
#	die "Unable to open results file $results_file: $!\n";


$last_i = -1;
while ( ($i=index($results_file,"/",$last_i+1)) > -1) {
    $last_i = $i;
}
$errlog = $errlog_dir . "/" . substr($results_file,$last_i+1) . ".errlog";
open ERRLOG, ">$errlog" ||
	die "Cannot open error log for writing\n";

$q0warn = 0;
$num_errors = 0;
$line_num = 0;
$old_topic = "-1";
$run_id = "";
while ($line = <RESULTS>) {
    chomp $line;
    $line_num++;
    next if ($line =~ /^\s*$/);

    undef $tag;
    ($topic_string,$q0,$docno,$rank,$sim,$tag,$rest) = split " ", $line;
    if ($rest)  {
	&error("Too many fields");
	die "\n";
    }

    # make sure runtag is ok
    if (! $run_id) { 	# first line --- remember tag 
	$run_id = $tag;
	if ($run_id !~ /^[A-Za-z0-9]{1,12}$/) {
	    &error("Run tag `$run_id' is malformed");
	    next;
   	}
    }
    else {			# otherwise just make sure one tag used
	if ($tag ne $run_id) {
	    &error("Run tag inconsistent (`$tag' and `$run_id')");
	    next;
	}
    }

    # get topic number
    if ($topic_string ne $old_topic) {
	$old_topic = $topic_string;
	undef %topic_docnos;
        while ($topic_string =~ /^0/) {
            $topic_string = substr $topic_string, 1;
        }
	$topic = $topic_string;
	if ($topic < $MINQ || $topic > $MAXQ) {
            &error("Unknown topic ($topic_string)");
            $topic = 0;
            next;
        }  
    }


    # make sure second field is "Q0"
    if ($q0 ne "Q0" && ! $q0warn) {
        $q0warn = 1;
        &error("Field 2 is `$q0' not `Q0'");
    }


    # approximate check for correct docno
    if ($docno !~ /^\d+$/) {	# invalid DOCNO
        &error("Unknown document `$docno'");
	next;
    }
    if (exists $topic_docnos{$docno}) {
        &error("Document `$docno' retrieved more than once for topic $topic_string");
        next;
    }
    $topic_docnos{$docno} = $topic;


    $num_ret[$topic]++;
}



# Do global checks:
#   error if some topic has no (or too many) documents retrieved for it
#   warn if too few documents retrieved for a topic
for ($t=$MINQ; $t<=$MAXQ; $t++) { 
    if ($num_ret[$t] == 0) {
        &error("No documents retrieved for topic $t");
    }
    elsif ($num_ret[$t] > $MAX_RET) {
        &error("Too many documents ($num_ret[$t]) retrieved for topic $t");
    }
    elsif ($num_ret[$t] < $MAX_RET) {
	print ERRLOG "$0 of $results_file:  WARNING: only $num_ret[$t] documents retrieved for topic $t\n"
    }
}

print ERRLOG "Finished processing $results_file\n";
close ERRLOG || die "Close failed for error log $errlog: $!\n";
print "Finished processing $results_file\n";

if ($num_errors) { exit 255; }
exit 0;


# print error message, keeping track of total number of errors
# line numbers refer to SORTED file since that is the actual input file
sub error {
   my $msg_string = pop(@_);

    print ERRLOG 
    "$0 of $results_file: Error on line $line_num --- $msg_string\n";

    $num_errors++;
    if ($num_errors > $MAX_ERRORS) {
        print ERRLOG "$0 of $results_file: Quit. Too many errors!\n";
        close ERRLOG ||
		die "Close failed for error log $errlog: $!\n";
	exit 255;
    }
}
