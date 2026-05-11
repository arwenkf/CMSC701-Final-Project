#!/bin/bash

INPUT_FILE="data/data_2/truth.txt"

awk '
{
    if ($2 == "PROB_YES") {
        print $1 > "data2-true-positives.txt"
    }
    else if ($2 == "NO") {
        print $1 > "data2-true-negatives.txt"
    }
}' "$INPUT_FILE"

echo "Done"
