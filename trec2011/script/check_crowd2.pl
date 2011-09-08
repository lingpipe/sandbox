#!/usr/bin/perl -w

use strict;

# Check a TREC 2011 crowdsourcing track task 2 submission for various
# common errors:
#      * extra/missing fields
#      * missing/extra  topic/docid pairs
# Messages regarding submission are printed to an error log

# Results input file is in the form
#     <topic-id> <doc-id> <rank-label> <class-label> 

# This script requires a list of the [topic,docno] pairs
# for which labels must be returned.  A list of the 
# pairs in the test set is posted on the same page
# as this script in the active participants' section of the TREC web site.
# Change the location of this file on your system below.

# Change these variable values to the full path name of the file
# of required pairs and the directory where the error log should be put
my $reqpairs = "crowd2-required.txt";
my $errlog_dir = ".";

# If more than MAX_ERRORS errors, then stop processing; something drastically
# wrong with the file.
my $MAX_ERRORS = 25; 


my %required;			# set of [topic,docno] pairs in test set
my $results_file;               # input file to be checked
my $line_num;                   # current input line number
my $errlog;                     # file name of error log
my $num_errors;      		# flag for errors detected
my $line;                       # current input line
my ($topicid,$docid,$rankl,$classl);
my ($topic,$rest);
my ($i,$last_i);

my $usage = "Usage: $0 resultsfile\n";
$results_file = shift or die $usage;

if ( (! -e $reqpairs) || (! open REQUIRED, "<$reqpairs") ) {
    die "Can't find/read list of required [topic-docno] pairs: $!\n";
}
while ($line = <REQUIRED>) {
    chomp $line;
    next if ($line =~ /^\s*$/);

    ($topic,$docid) = split " ", $line;
    $required{$topic}{$docid} = 0;
}
close REQUIRED;

open RESULTS, "<$results_file" ||
    die "Unable to open results file $results_file: $!\n";

$last_i = -1;
while ( ($i=index($results_file,"/",$last_i+1)) > -1) {
    $last_i = $i;
}
$errlog = $errlog_dir . "/" . substr($results_file,$last_i+1) . ".errlog";
open ERRLOG, ">$errlog" ||
    die "Cannot open error log for writing\n";

    
$num_errors = 0;
$line_num = 0;
$rest = "";
while ($line = <RESULTS>) {
    chomp $line;
    next if ($line =~ /^\s*$/);

    $line_num++;
    ($topicid,$docid,$rankl,$classl,$rest) = split " ", $line, 5;
    if ($rest) {
	&error("Too many fields");
	exit 255;
    }
	
    # rank label is either "na" or an integer >= 1
    # class label is either "na" or a real between 0 and 1
    # both can be na together
    if ( ($rankl !~ /^[1-9][0-9]*$/) && ($rankl ne "na") ) {
	&error("Invalid rank label ('$rankl')");
	next;
    }
    if ( ($classl !~ /^[0-1]?\.?[0-9]*$/) && ($classl ne "na") ) {
	&error("Invalid class label ('$classl')");
	next;
    }
    if ($classl ne "na") {
	if ($classl > 1) {
	    &error("Class label must be a real between 0 and 1 (not $classl)");
	    next;
	}
    }

    # topic ids are integers between 1 and 99999
    if ($topicid !~ /^[1-9][0-9]?[0-9]?[0-9]?[0-9]?$/) {
	&error("Invalid topic id ('$topicid')");
	next;
    }
	
    # make sure docid has right format
    if (! exists $required{$topicid}{$docid}) {
	&error("Label returned for unknown pair [$topicid,$docid]");
    }
    elsif ($required{$topicid}{$docid} != 0) {
	&error("Multiple labels returned for pair [$topicid,$docid]");
    }
    else {
        $required{$topicid}{$docid}++;
    }
}



# Do global checks:
#   make sure all required pairs have been assigned exactly one label 
foreach $topic (keys %required) {
    foreach $docid (keys %{$required{$topic}}) {
        if ($required{$topic}{$docid} != 1) {
            &error("No label supplied for required pair [$topic,$docid]");
	}
    }
}


print ERRLOG "Finished processing $results_file\n";
close ERRLOG || die "Close failed for error log $errlog: $!\n";
if ($num_errors) {
    exit 255;
}
exit 0;


# print error message, keeping track of total number of errors
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
