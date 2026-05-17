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
        java -cp "lib/*:src" queryXor bin-000006945 ../data/GCA_000006945.2/GCA_000006945-true-positives.txt "res-GCA_000006945-$f-$i.txt" $f
    done
done

# ==================== data 2 ====================
for f in "${FPR_VALUES[@]}"; do
    for ((i=1; i<=NUM_RUNS; i++)); do
        echo "Run $i/$NUM_RUNS: Building data 2 with FPR $f"
        java -cp "lib/*:src" buildXor ../data/data_2/input.txt "$f" bin-data2
        echo "Run $i/$NUM_RUNS: Querying data 2 with FPR $f"
        java -cp "lib/*:src" queryXor bin-data2 ../data/data_2/data2-true-positives.txt "res-data2-$f-$i.txt" $f
    done
done

QUERY_FILE="xor-eval-query.txt"
BUILD_FILE="xor-eval-build.txt"
OUTPUT_FILE="../eval-xor-true-pos.txt"

awk '
BEGIN {
    printf "| %-20s | %-8s | %-12s | %-12s |\n", "Dataset", "FPR", "Avg Build", "Avg Query"
    printf "|-%s-|-%s-|-%s-|-%s-|\n", "--------------------", "--------", "------------", "------------"
}

FNR == 1 { file_index++ }

{ sub(/\r$/, "") }

(FNR % 3) == 1 {
    split($0, path_parts, "/")
    dataset = path_parts[3]
}

(FNR % 3) == 2 {
    fpr = $2
    key = dataset SUBSEP fpr

    if (!seen[key]++) {
        order[++n] = key
    }
}

(FNR % 3) == 0 {
    val = $1
    if (file_index == 1) {
        query_sum[key] += val
        query_count[key]++
    } else {
        build_sum[key] += val
        build_count[key]++
    }
}

END {
    for (i=1; i<=n; i++) {
        k = order[i]

        split(k, parts, SUBSEP)
        d = parts[1]
        f = parts[2]

        bc = build_count[k] > 0 ? build_count[k] : 1
        qc = query_count[k] > 0 ? query_count[k] : 1

        avg_build = build_sum[k] / bc
        avg_query = query_sum[k] / qc

        printf "| %-20s | %8s | %12.8f | %12.8f |\n", d, f, avg_build, avg_query
    }
}
' "$QUERY_FILE" "$BUILD_FILE" > "$OUTPUT_FILE"
