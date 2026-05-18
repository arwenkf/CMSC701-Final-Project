# Case Study and Comparison of AMQ Structures for Genomic k-mers

Approximate Membership Query Structures have developed significantly over time and have become a useful tool in querying the presence of $k$-mers in a genomic sequence. One of the earlier and most famous AMQ structures is the Bloom Filter. The idea is still being iterated on today, though, and in this paper we compare and benchmark the Bloom Filter, Cuckoo Filter, and XOR Filter, the latter two being more modern AMQ structures. We compare them with some sample data as well as a real world genomic sequence to gauge their performance in real genomic tasks. The three AMQ structures we chose to focus on in this project are Bloom filters, Cuckoo filters, and XOR filters.

- The implementation for the Bloom and Cuckoo filters can be found under `Bloom-Filter/src/Runner.java`.
- The implementation of the Xor filter is under `xor-filter/src/<build || query>Xor.java`.
- The baseline hash implementation can be found under `baseline-hash`

All of our datasets, along with their queries are found under `./data`

The results we have depicted in our paper can be found in `./results`

## Reproducing the Experiments

To reproduce the experiments for the construction \& query speeds, first unzip all the zipped files under `./data`. Then, each filter, except the cuckoo filters have their own evaluation scripts to re-run the experiments. There are three "query modes": `Mixed`, `TP` (for only true positive queries), and `TN` (for only true negative queries). To run a filter, follow the instructions below.

- Bloom Filter: 
  - `./eval-bloom-cuckoo.sh <query mode> Bloom`
- Cuckoo Filter: 
  - `./eval-bloom-cuckoo.sh <query mode> Cuckoo`
- Xor Filter: 
  - `./eval-xor.sh <query mode>`
- Baseline: 
  - `./baseline-hash.sh <query mode>`

To reproduce the actual false positive rates outputted by a filter, run `fpr.sh` with the filter you just evaluated:
- Bloom Filter: 
  - `./fpr.sh Bloom`
- Cuckoo Filter: 
  - `./fpr.sh Cuckoo`
- Xor Filter: 
  - `./fpr.sh Xor`
- Baseline: 
  - `./fpr.sh Baseline`