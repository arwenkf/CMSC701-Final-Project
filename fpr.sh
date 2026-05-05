#!/bin/bash

GT_6945="$PWD/data/GCA_000006945.2/truth-GCA_000006945.txt"
GT_9045="$PWD/data/GCA_000009045.1/truth-GCA_000009045.txt"

GT_DATA1="$PWD/data/data_1/truth.txt"
GT_DATA2="$PWD/data/data_2/truth.txt"

if [ ! -f "$GT_6945" ]; then
    echo "Error: Ground truth file for 6945 '$GT_6945' not found."
    exit 1
fi

if [ ! -f "$GT_9045" ]; then
    echo "Error: Ground truth file for 9045 '$GT_9045' not found."
    exit 1
fi

if [ ! -f "$GT_DATA1" ]; then
    echo "Error: Ground truth file for data1 '$GT_DATA1' not found."
    exit 1
fi

if [ ! -f "$GT_DATA2" ]; then
    echo "Error: Ground truth file for data2 '$GT_DATA2' not found."
    exit 1
fi

cd "$PWD/xor-filter" || exit

RES_FILE="xor-fpr.txt"
> "$RES_FILE"

echo "Processing files..."

for file in res-*.txt; do
    [ -e "$file" ] || continue

    if [[ "$file" =~ res-GCA_([0-9]+)-([0-9\.]+)(-[0-9]+)?\.txt ]]; then
        dataset="${BASH_REMATCH[1]}"
        target_fpr="${BASH_REMATCH[2]}"

    elif [[ "$file" =~ res-(data[12])-([0-9\.]+)(-[0-9]+)?\.txt ]]; then
        dataset="${BASH_REMATCH[1]}"
        target_fpr="${BASH_REMATCH[2]}"

    else
        continue
    fi

    if [ "$dataset" == "000006945" ]; then
        CURRENT_GT="$GT_6945"
    elif [ "$dataset" == "000009045" ]; then
        CURRENT_GT="$GT_9045"
    elif [ "$dataset" == "data1" ]; then
        CURRENT_GT="$GT_DATA1"
    elif [ "$dataset" == "data2" ]; then
        CURRENT_GT="$GT_DATA2"
    else
        echo "Warning: Unknown dataset '$dataset' in file '$file'. Skipping."
        continue
    fi

    read fp total_negs actual_fpr <<< $(awk '
        NR==FNR {
            gt[$1]=$2;
            if($2=="NO") negs++;
            next
        }
        {
            if($1 in gt && gt[$1]=="NO" && $2!="NO") fp++
        }
        END {
            print fp+0, negs+0, (negs>0 ? fp/negs : 0)
        }
    ' "$CURRENT_GT" "$file")

    echo "$dataset $target_fpr $actual_fpr" >> "$RES_FILE"
done


SUMMARY_FILE="xor-fpr-opt-res.txt"

echo "" > "$SUMMARY_FILE"
printf "%-12s | %-12s | %-10s | %-15s\n" "Dataset" "Target FPR" "# of Runs" "Avg Actual FPR" >> "$SUMMARY_FILE"
echo "-------------+--------------+------------+-----------------" >> "$SUMMARY_FILE"


awk '
{
    dataset = $1
    target = $2
    fpr = $3
    key = dataset "_" target

    sum[key] += fpr
    count[key]++
    d[key] = dataset
    t[key] = target
}
END {
    for (k in sum) {
        avg = sum[k] / count[k]
        printf "%-12s | %-12s | %-10s | %.6f\n", d[k], t[k], count[k], avg
    }
}' "$RES_FILE" | sort -k1,1 -k2,2n >> "$SUMMARY_FILE"
