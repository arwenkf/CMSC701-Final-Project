#!/bin/bash
input=$1
output=$2
jellyfish count -m 21 -s 100M -t 10 -C $input
jellyfish dump mer_counts.jf > $output
sed -i '/^>/d' $output
rm mer_counts.jf