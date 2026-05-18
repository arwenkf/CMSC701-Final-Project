#!/bin/bash

DIR="$PWD/baseline-hash"

javac "$DIR"/src/*.java

cd "$DIR" || exit

NUM_RUNS=3

MIXED_6945="../data/GCA_000006945.2/query-GCA_000006945.txt"
TP_6945="../data/GCA_000006945.2/GCA_000006945-true-positives.txt"
TN_6945="../data/GCA_000006945.2/GCA_000006945-true-negatives.txt"

MIXED_DATA2="../data/data_2/query.txt"
TP_DATA2="../data/data_2/data2-true-positives.txt"
TN_DATA2="../data/data_2/data2-true-negatives.txt"

QUERY_6945=""
QUERY_DATA2=""

if [[ "$1" == "Mixed" ]]; then
    QUERY_6945=$MIXED_6945
    QUERY_DATA2=$MIXED_DATA2
elif [[ "$1" == "TP" ]]; then
    QUERY_6945=$TP_6945
    QUERY_DATA2=$TP_DATA2
elif [[ "$1" == "TN" ]]; then
    QUERY_6945=$TN_6945
    QUERY_DATA2=$TN_DATA2
fi

# ==================== 6945 ====================

for ((i=1; i<=NUM_RUNS; i++)); do
    echo "Run $i/$NUM_RUNS: Building and querying 6945"
    java -cp src baselineHash ../data/GCA_000006945.2/21-mers-GCA_000006945.2.txt $QUERY_6945 "res-GCA_000006945-$i.txt"
done

# ==================== data 2 ====================

for ((i=1; i<=NUM_RUNS; i++)); do
        echo "Run $i/$NUM_RUNS: Building and querying data 2"
        java -cp src baselineHash ../data/data_2/input.txt $QUERY_DATA2 "res-data2-$i.txt"
done

INPUT_FILE="$DIR/baseline-res.txt"
OUTPUT_FILE="../eval-baseline-$1.txt"

awk '
BEGIN {
    printf "| %-52s | %-12s | %-12s |\n", "Dataset", "Avg Build", "Avg Query"
    printf "|-%s-|-%s-|-%s-|\n", "---------------------------------------------------", "------------", "------------"
}

{ sub(/\r$/, "") }


$1 !~ /^(build:|query:)$/ {
    dataset = $0

    if (!seen[dataset]++) {
        order[++n] = dataset
    }
}

$1 == "build:" {
    build_sum[dataset] += $2
    run_count[dataset]++
}

$1 == "query:" {
    query_sum[dataset] += $2
}

END {

    for (i=1; i<=n; i++) {
        d = order[i]
        avg_build = build_sum[d] / run_count[d]
        avg_query = query_sum[d] / run_count[d]

        printf "| %-52s | %12.8f | %12.8f |\n", d, avg_build, avg_query
    }
}
' "$INPUT_FILE" > "$OUTPUT_FILE"
