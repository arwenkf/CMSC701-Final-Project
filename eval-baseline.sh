#!/bin/bash
# shellcheck disable=SC2034

DIR="$PWD/baseline-hash"
echo "$DIR"

javac "$DIR"/src/*.java

cd "$DIR" || exit

NUM_RUNS=3

# ==================== 6945 ====================

for ((i=1; i<=NUM_RUNS; i++)); do
    echo "Run $i/$NUM_RUNS: Building and querying 6945"
    java -cp src baselineHash ../data/GCA_000006945.2/21-mers-GCA_000006945.2.txt ../data/GCA_000006945.2/GCA_000006945-true-negatives.txt "res-GCA_000006945-$i.txt"
done

# ==================== data 2 ====================

for ((i=1; i<=NUM_RUNS; i++)); do
        echo "Run $i/$NUM_RUNS: Building and querying data 2"
        java -cp src baselineHash ../data/data_2/input.txt ../data/data_2/data2-true-negatives.txt "res-data2-$i.txt"
done

INPUT_FILE="$DIR/baseline-res.txt"
OUTPUT_FILE="../eval-baseline-true-negatives.txt"

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
