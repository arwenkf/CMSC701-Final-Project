import random

def generate_random_false_positives(true_positives_set, num_needed, k=21):

    alphabet = "AGCT"
    false_positives = set()

    print(f"Generating {num_needed} random false positives...")

    while len(false_positives) < num_needed:
        candidate = "".join(random.choices(alphabet, k=k))

        if candidate not in true_positives_set and candidate not in false_positives:
            false_positives.add(candidate)

    return list(false_positives)

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


    how_many_to_generate = 4000000

    random_fps = generate_random_false_positives(true_positives_set, how_many_to_generate)

    with open("fp.txt", "w") as out_random:
        for fp in random_fps:
            out_random.write(fp + "\n")

    print(f"Done")
