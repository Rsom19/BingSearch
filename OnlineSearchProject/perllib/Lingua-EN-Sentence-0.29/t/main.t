#------------------------------------------------------------------------------
# Test script for Lingua::EN::::Sentence.pm
#
# Author      : Kim Ryan, 
# Last update : 2015-03-10
#------------------------------------------------------------------------------

use warnings;
use strict;
use Test::More tests => 3;

BEGIN {

  # does it load properly?
  require_ok('Lingua::EN::Sentence');
}
use Lingua::EN::Sentence qw( get_sentences add_acronyms get_acronyms);

my $par = q{
Returns the number of sentences in string. A sentence ends with a dot
exclamtion or question mark followed by a space! Dots in abbreviations such as e.g. are ignored, as well as common
abbreviations such as Dr. Ms. esp. Apr. Calif. and Ave. insitals such as 'Mr. A. Smith' plus more?
Dots in muti word abrreviatons such as U.S.A are also ignored.
This string has 4 sentences.
};

my $sentences=get_sentences($par);     
is( @$sentences, 5,'sub sentence_count');

$par .= 'Now add an acronym, such as qld. for Queensland to the paragraph.';
add_acronyms('Qld');
$sentences=get_sentences($par);
is( @$sentences, 6,'sub add_acronyms');


