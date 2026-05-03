#!/bin/bash
# shellcheck disable=SC2034

DIR="$PWD/Bloom-Filter"
echo "$DIR"
RES_FILE="./bloom-eval.txt"

javac -cp "$DIR/lib/*" "$DIR"/src/*.java

cd "$DIR" || exit

FPR_VALUES=(0.0001 0.001 0.01 0.1 0.25 0.5 0.75)
NUM_RUNS=3

printf "6945\n" >> "$RES_FILE"
for f in "${FPR_VALUES[@]}"; do
    for ((i=1; i<=NUM_RUNS; i++)); do
        echo "Run $i/$NUM_RUNS: Building 6945 with FPR $f"
        java -cp "lib/*:src" Runner ../data/GCA_000006945.2/21-mers-GCA_000006945.2.txt "$f" ../data/GCA_000006945.2/query-GCA_000006945.txt "res-GCA_000006945-$f-$i.txt"
    done
done
printf "9045\n" >> "$RES_FILE"
for f in "${FPR_VALUES[@]}"; do
    for ((i=1; i<=NUM_RUNS; i++)); do
        echo "Run $i/$NUM_RUNS: Building 9045 with FPR $f"
        java -cp "lib/*:src" Runner ../data/GCA_000009045.1/21-mers-GCA_000009045.1.txt "$f" ../data/GCA_000009045.1/query-GCA_000009045.txt "res-GCA_000009045-$f-$i.txt"
    done
done

printf "data1\n" >> "$RES_FILE"
for f in "${FPR_VALUES[@]}"; do
    for ((i=1; i<=NUM_RUNS; i++)); do
        echo "Run $i/$NUM_RUNS: Building data 1 with FPR $f"
        java -cp "lib/*:src" Runner ../data/data_1/input.txt "$f" ../data/data_1/query.txt "res-data1-$f-$i.txt"
    done
done

printf "data2\n" >> "$RES_FILE"
for f in "${FPR_VALUES[@]}"; do
    for ((i=1; i<=NUM_RUNS; i++)); do
        echo "Run $i/$NUM_RUNS: Building data 2 with FPR $f"
        java -cp "lib/*:src" Runner ../data/data_2/input.txt "$f" ../data/data_2/query.txt "res-data2-$f-$i.txt"
    done
done

FILE="./bloom-eval.txt"

awk '
BEGIN {
    ds_idx = 0
}
{
    if (NF == 0) next;

    if (NF == 1 && $1 !~ /^fpr:/) {
        dataset = $1;
        # Keep track of the order of datasets
        if (!seen_ds[dataset]++) {
            ds_list[++ds_idx] = dataset;
        }
    }

    else if ($1 ~ /^fpr:/) {
        fpr = $1;
        if (!seen_ds_fpr[dataset, fpr]++) {
            ds_fpr_list[dataset, ++fpr_count[dataset]] = fpr;
        }
    }
    else if (NF == 2) {
        sum1[dataset, fpr] += $1;
        sum2[dataset, fpr] += $2;
        cnt[dataset, fpr]++;
    }
}
END {
    for (i = 1; i <= ds_idx; i++) {
        ds = ds_list[i];
        print "==============================================="
        print "Dataset: " ds
        print "==============================================="

        printf "%-12s | %-14s | %-14s\n", "FPR", "Avg Build Time", "Avg Query Time"
        print "-----------------------------------------------"

        for (j = 1; j <= fpr_count[ds]; j++) {
            f = ds_fpr_list[ds, j];

            if (cnt[ds, f] > 0) {
                avg1 = sum1[ds, f] / cnt[ds, f];
                avg2 = sum2[ds, f] / cnt[ds, f];
                printf "%-12s | %14.6f | %14.6f\n", f, avg1, avg2;
            }
        }
        print ""
    }
}
' "$FILE" > "bloom-res.txt"

rm $FILE

echo "Done"
