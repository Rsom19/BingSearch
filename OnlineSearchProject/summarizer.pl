#!/usr/bin/env perl
use strict;
use warnings;

use Getopt::Long;
use Data::Dumper;
use File::Basename qw(dirname);
use File::Temp qw(tempfile);
use HTTP::Daemon;
use HTTP::Status qw(:constants);
use IO::Scalar;
use JSON;
use Lingua::EN::Sentence qw(get_sentences);
use threads;

use open qw(:utf8);

#binmode STDOUT, ':utf8';

# Load resources
my $stoplist_path = "stoplist";
my $stems_path = "stems";
my $idf_path = "idf";
my $run_solver_path = "runLPSolver.py";
#my $solver_path ="/d01/LPSolver/"
my $solver_path="C:\\Users\\Marwan\\AppData\\Local\\Enthought\\Canopy\\User\\Lib\\site-packages\\pulp\\solverdir\\cbc\\win\\64\\";

my %stop = do { 
    open my $fh, '<', $stoplist_path or die; 
    map { chomp; $_ => 1 } <$fh>;
};

my %stemmed = do {
    open my $fh, '<', $stems_path or die;
    map { chomp; my ($h, $t) = split /\t/, $_; $h => $t } <$fh>;
};

my %idf = do {
    open my $fh, '<', $idf_path or die;
    map { chomp; my ($h, $t) = split /\t/, $_; $h => $t } <$fh>;
};


# utilities
#
sub normalize {
    local $_ = shift;
    s/\W+/ /g;
    s/^\s+|\s+$//g;
    lc $_;
}

# This implements 'sentenceSplitter.pl'
#
sub split_sentences { 
    my $text = shift;

    my $sent_ref = get_sentences $text;
    return map { s/^\s+|\s+$//g; $_ } @$sent_ref;
}

# This implements 'generateCplexInputfile.pl'
#
sub generate_Cplex_inputfile {
    my ($k, $qtitle, $qbody, @sentences) = @_;
    my $lambda = 0.1;

    # this is just pulling term distribution from query
    # FIXME: there might be better way of doing this
    my %answertermfrek = ( 1 => {}, 4 => {} );
    my @qword = split /\s+/, normalize $qtitle;
    for my $qelement (@qword) {
	next if exists $stop{$qelement};
	my $qelem = $stemmed{$qelement} || $qelement;
	$answertermfrek{1}{$qelem}++;
    }

    @qword = split /\s+/, normalize $qbody;
    for my $qelement (@qword) {
	next if exists $stop{$qelement};
	my $qelem = $stemmed{$qelement} || $qelement;
	$answertermfrek{4}{$qelem}++;
    }

    # read document target	
    my $sentencenum=0;
    my %weightOfWord=();
    my %weightOfSentence=();
    my %costOfSentence=();
    my %associationWordSentence=();

    for my $line (@sentences) {
	$sentencenum++;

	#preprocess
	my @word = split /\s+/, normalize $line;
	next if @word < 5;

	my @sent = ();
	for my $element (@word) {
	    next if exists $stop{$element};
	    my $elem = $stemmed{$element} || $element;
	    push @sent, $elem;

	    my $weight = 0;
	    for my $i (sort keys %answertermfrek) { 
		next if !exists $answertermfrek{$i}{$elem};

		my $tf = $answertermfrek{$i}{$elem};
		my $penalty = log($i + 1);
		my $idfx = $idf{$elem} || 0;
		$weight += ($tf * $idfx) / $penalty; #print "$elem\t$i\t$tf\t$idfx\t$tes\n";
	    } 
	    $weight = sprintf "%.2f", $weight; 

	    my $ww = (1 - $lambda) * $weight;
	    my $ws = $lambda * $weight;
	    
	    if ($ww != 0) {
		#weight of word
		$weightOfWord{$elem} ||= $ww; 
		$associationWordSentence{$elem}{$sentencenum} ||= 1; 
	    }
	    
	    if ($ws != 0) {
		#weight of sentence
		$weightOfSentence{$sentencenum} += $ws
	    }
	}

	my $sent = join ' ', @sent;
	$costOfSentence{$sentencenum} = length $line if $sent ne '';
    }

    #print header		
    my $result = '';
    tie *OUT, 'IO::Scalar', \$result;
    #print OUT "\\ENCODING=ISO-8859-1\n";
    #print OUT "\\Problem name:\n\n";
    print OUT "Maximize\n";

    #print objective function
    print OUT " obj: ";
    my $obj = "";
    my $i=1;
    my $bounds="";
    my $integers="";
    foreach my $x (sort keys %weightOfWord){
	    my $y = $weightOfWord{$x}; #print "z$i\t$x\n";
	    $obj= $obj."$y z$i + ";
	    $bounds=$bounds." 0 <= z$i <= 1\n";
	    $integers=$integers." z$i\n";
	    $i++;
	    
    }
    foreach my $x (sort {$a <=> $b} keys %weightOfSentence){
	    my $y = $weightOfSentence{$x};
	    $obj= $obj."$y x$x + ";
    }
    $obj=~s/[+]\s$//g;
    print OUT "$obj\n";


    #print first constraint
    print OUT "Subject To\n";
    my $c0=" c0: ";
    foreach my $x (sort {$a <=> $b} keys %costOfSentence){
	    my $y = $costOfSentence{$x};
	    $c0= $c0."$y x$x + ";
	    $bounds=$bounds." 0 <= x$x <= 1\n";
	    $integers=$integers." x$x\n";
    }
    $c0=~s/[+]\s$//g;
    $c0=$c0."<= $k";
    print OUT "$c0\n";


    #print the rest of the constraints
    my $c=1;
    foreach my $x (sort keys %associationWordSentence){
	    my $cnext= " c$c: ";
	    foreach my $y (sort {$a <=> $b} keys %{$associationWordSentence{$x}}){
		    $cnext= $cnext."x$y + ";
	    }
	    $cnext=~s/[+]\s$//g;
	    $cnext=$cnext."- z$c >= 0";
	    print OUT "$cnext\n";
	    $c++;
    }

    #print bounds
    print OUT "Bounds\n";
    print OUT "$bounds";

    #print integers
    print OUT "Integers\n";
    print OUT "$integers";
    print OUT "End\n";
    close(OUT);

    return $result;
}

# The callback that does the real work
#
sub summarize {
    my $json0 = shift;

    my $json=decode_json($json0);
    for (qw(texts question limit)) {
	return "error: missing '$_'" unless exists $json->{$_};
    }

    my $texts = $json->{texts}; 
    my $question = $json->{question};
    my $limit = $json->{limit};
    
    
    return "nothing to summarize" unless @$texts > 0;
    my $q_title = $question->{title} || ''; 
    my $q_body = $question->{body} || ''; 
    my $text = join ' ', @$texts;
    $text =~ s/\s+/ /g; 

    #print "$texts\n";    
    #print "$limit\n";
    #print "$question\n";
    #print "$q_title\n";
    #print "$q_body\n";
    #print "$text\n";
    
    my @sentences = split_sentences $text;
    my $program = generate_Cplex_inputfile $limit, $q_title, $q_body, @sentences;

    print "------------- SENTENCES ---------\n";
    do {
	my $lineno = 0;
	print "[", ++$lineno, "] ", $_, "\n" for @sentences;
    };

    # write program to some file and generate another temp
    my $input_fn;
    my $output_fn;

    do { (my $fh, $input_fn) = tempfile; print $fh $program, "\n"; };
    (undef, $output_fn) = tempfile;
    print "input $input_fn\n";
    print "output $output_fn\n";
    # execute the python code
    return 'error: invalid $run_solver_path' unless -f $run_solver_path;
    `python $run_solver_path $input_fn $output_fn $solver_path`;

    my @result = do { open my $fh, "<", $output_fn; <$fh> };
    #print $#result."\n";

    print "------------- RESULT ------------\n";
    print $_ for @result; 
    print "\n";

    my @ids = map { /^x(\d+)/ && ($1 - 1) } @result;

    my $summary = join ' ', grep !/^\s*$/, map $sentences[$_], @ids;

    return $summary || '';
}

my $json_input=$ARGV[0];
print Dumper \@ARGV;
my $result=&summarize($json_input);
print $result;
