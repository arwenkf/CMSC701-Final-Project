#!/bin/bash

KMER_INPUT=""
FALSE_POS=""

# concat PROB_YES probability to true positives
sed 's/$/ PROB_YES/' "$KMER_INPUT" > tp-"$KMER_INPUT".txt

# concat NO to false positives
sed 's/$/ NO/' "$FALSE_POS" > fp-"$FALSE_POS".txt

# concat true and false randomly to create truth file
cat fp-"$FALSE_POS".txt  tp-"$KMER_INPUT".txt | sort -R > truth-"$KMER_INPUT".txt

# create query file
awk '{print $1}' truth-"$KMER_INPUT".txt > query-"$KMER_INPUT".txt
