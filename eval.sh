#!/bin/bash
# shellcheck disable=SC2034

DIR="$PWD/xor-filter"
echo "$DIR"
RES_FILE="$PWD/res-xor.txt"

> "$RES_FILE"

javac -cp "$DIR/lib/*" "$DIR"/src/*.java

cd "$DIR" || exit

FPR_VALUES=(0.0001 0.001 0.01 0.1 0.25 0.5 0.75)
NUM_RUNS=3

echo -e "false pos | avg build time | avg query time " >> "$RES_FILE"

echo -e "6945" >> "$RES_FILE"

for f in "${FPR_VALUES[@]}"; do

    total_build_time_6945=0
    total_query_time_6945=0

    for ((i=1; i<=NUM_RUNS; i++)); do
        echo "Run $i/$NUM_RUNS: Building 6945 with FPR $f"
        start_time=$(date +%s.%N)
        java -cp "lib/*:src" buildXor ../data/GCA_000006945.2/21-mers-GCA_000006945.2.txt "$f" bin-000006945
        end_time=$(date +%s.%N)

        build_time=$(echo "$end_time - $start_time" | bc)
        total_build_time_6945=$(echo "$total_build_time_6945 + $build_time" | bc)

        echo "Run $i/$NUM_RUNS: Querying 6945 with FPR $f"
        start_time=$(date +%s.%N)
        java -cp "lib/*:src" queryXor bin-000006945 ../data/GCA_000006945.2/query-GCA_000006945.txt "res-GCA_000006945-$f-$i.txt"
        end_time=$(date +%s.%N)

        query_time=$(echo "$end_time - $start_time" | bc)
        total_query_time_6945=$(echo "$total_query_time_6945 + $query_time" | bc)
    done

    avg_build_time_6945=$(echo "scale=4; $total_build_time_6945 / $NUM_RUNS" | bc)
    avg_query_time_6945=$(echo "scale=4; $total_query_time_6945 / $NUM_RUNS" | bc)

    echo -e " $f | $avg_build_time_6945 | $avg_query_time_6945" >> "$RES_FILE"
done

echo -e "9045" >> "$RES_FILE"

for f in "${FPR_VALUES[@]}"; do
    total_build_time_9045=0
    total_query_time_9045=0

    for ((i=1; i<=NUM_RUNS; i++)); do
        echo "Run $i/$NUM_RUNS: Building 9045 with FPR $f"
        start_time=$(date +%s.%N)
        java -cp "lib/*:src" buildXor ../data/GCA_000009045.1/21-mers-GCA_000009045.1.txt "$f" bin-000009045
        end_time=$(date +%s.%N)

        build_time=$(echo "$end_time - $start_time" | bc)
        total_build_time_9045=$(echo "$total_build_time_9045 + $build_time" | bc)

        echo "Run $i/$NUM_RUNS: Querying 9045 with FPR $f"
        start_time=$(date +%s.%N)
        java -cp "lib/*:src" queryXor bin-000009045 ../data/GCA_000009045.1/query-GCA_000009045.txt "res-GCA_000009045-$f-$i.txt"
        end_time=$(date +%s.%N)

        query_time=$(echo "$end_time - $start_time" | bc)
        total_query_time_9045=$(echo "$total_query_time_9045 + $query_time" | bc)
    done

    avg_build_time_9045=$(echo "scale=4; $total_build_time_9045 / $NUM_RUNS" | bc)
    avg_query_time_9045=$(echo "scale=4; $total_query_time_9045 / $NUM_RUNS" | bc)

    echo -e " $f | $avg_build_time_9045 | $avg_query_time_9045" >> "$RES_FILE"
done

echo "Done"
