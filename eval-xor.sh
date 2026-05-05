#!/bin/bash
# shellcheck disable=SC2034

DIR="$PWD/xor-filter"
echo "$DIR"

javac -cp "$DIR/lib/*" "$DIR"/src/*.java

cd "$DIR" || exit

FPR_VALUES=(0.0001 0.001 0.01 0.1 0.25 0.5 0.75)
NUM_RUNS=3

# ==================== 6945 ====================
for f in "${FPR_VALUES[@]}"; do
    total_build_time_6945=0
    total_query_time_6945=0

    for ((i=1; i<=NUM_RUNS; i++)); do
        echo "Run $i/$NUM_RUNS: Building 6945 with FPR $f"

        java -cp "lib/*:src" buildXor ../data/GCA_000006945.2/21-mers-GCA_000006945.2.txt "$f" bin-000006945
        echo "Run $i/$NUM_RUNS: Querying 6945 with FPR $f"
        java -cp "lib/*:src" queryXor bin-000006945 ../data/GCA_000006945.2/query-GCA_000006945.txt "res-GCA_000006945-$f-$i.txt" $f
    done
done

# ==================== 9045 ====================
for f in "${FPR_VALUES[@]}"; do

    for ((i=1; i<=NUM_RUNS; i++)); do
        echo "Run $i/$NUM_RUNS: Building 9045 with FPR $f"

        java -cp "lib/*:src" buildXor ../data/GCA_000009045.1/21-mers-GCA_000009045.1.txt "$f" bin-000009045

        echo "Run $i/$NUM_RUNS: Querying 9045 with FPR $f"

        java -cp "lib/*:src" queryXor bin-000009045 ../data/GCA_000009045.1/query-GCA_000009045.txt "res-GCA_000009045-$f-$i.txt" $f
    done
done

# ==================== data 1 ====================
for f in "${FPR_VALUES[@]}"; do

    for ((i=1; i<=NUM_RUNS; i++)); do
        echo "Run $i/$NUM_RUNS: Building data 1 with FPR $f"
        java -cp "lib/*:src" buildXor ../data/data_1/input.txt "$f" bin-data1
        echo "Run $i/$NUM_RUNS: Querying data 1 with FPR $f"
        java -cp "lib/*:src" queryXor bin-data1 ../data/data_1/query.txt "res-data1-$f-$i.txt" $f
    done
done

# ==================== data 2 ====================
for f in "${FPR_VALUES[@]}"; do
    for ((i=1; i<=NUM_RUNS; i++)); do
        echo "Run $i/$NUM_RUNS: Building data 2 with FPR $f"
        java -cp "lib/*:src" buildXor ../data/data_2/input.txt "$f" bin-data2
        echo "Run $i/$NUM_RUNS: Querying data 2 with FPR $f"
        java -cp "lib/*:src" queryXor bin-data2 ../data/data_2/query.txt "res-data2-$f-$i.txt" $f
    done
done

echo "Done"
