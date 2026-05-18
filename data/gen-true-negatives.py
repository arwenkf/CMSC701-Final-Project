import random

def generate_random_true_negatives(true_positives_set, num_needed, k=21):

    alphabet = "AGCT"
    true_negatives = set()

    while len(true_negatives) < num_needed:
        candidate = "".join(random.choices(alphabet, k=k))

        if candidate not in true_positives_set and candidate not in true_negatives:
            true_negatives.add(candidate)

    return list(true_negatives)

if __name__ == "__main__":
    tp_file_path = "./data/k-mers/21-mers-GCA_000009045.1.txt"

    true_positives_set = set()
    true_positives_list = []

    print(f"Loading true positives from {tp_file_path}...")

    with open(tp_file_path, "r") as file:
        for line in file:
            kmer = line.strip()
            if kmer:
                true_positives_set.add(kmer)
                true_positives_list.append(kmer)

    print(f"Successfully loaded {len(true_positives_set):,} unique true positives.\n")


    count = 4000000

    random_tns = generate_random_true_negatives(true_positives_set, count)

    with open("tn.txt", "w") as out_random:
        for tn in random_tns:
            out_random.write(tn + "\n")

    print(f"Done")
