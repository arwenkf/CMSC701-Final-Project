import matplotlib.pyplot as plt
import pandas as pd

# ---- 6945 ---- #

# mixed build
# data = {
#     "False Positive Input": [0.0001, 0.001, 0.01, 0.1, 0.25, 0.5, 0.75],
#     "Xor Filter": [7.41633, 7.27283, 7.40246, 7.39522, 7.39192, 7.17374, 7.16488],
#     "Bloom Filter": [3.458586, 2.686196, 2.51685, 2.111713, 1.973324, 1.786059, 1.593277],
#     "Cuckoo Filter": [2.666941, 2.553771, 2.595677, 2.585632, 2.565667, 2.579959, 2.587813],
#     "Baseline": [1.84191915, 1.84191915, 1.84191915, 1.84191915, 1.84191915, 1.84191915, 1.84191915]
# }

# mixed - query
# data = {
#     "False Positive Input": [0.0001, 0.001, 0.01, 0.1, 0.25, 0.5, 0.75],
#     "Xor Filter": [1.02674, 0.908771, 0.876852, 0.798444, 0.747355, 0.747547, 0.745835],
#     "Bloom Filter": [2.93664, 2.302841, 2.05581, 1.438664, 1.30471, 1.227927, 1.022777],
#     "Cuckoo Filter": [4.241031, 4.107994, 4.089813, 4.072282, 4.06481, 3.966585, 3.96681],
#     "Baseline": [2.11370430, 2.11370430, 2.11370430, 2.11370430, 2.11370430, 2.11370430, 2.11370430]
# }

# mixed - actual fpr
# data = {
#     "False Positive Input": [0.0001, 0.001, 0.01, 0.1, 0.25, 0.5, 0.75],
#     "Xor Filter": [0.000061, 0.000984, 0.007799, 0.055073, 0.24987, 0.499928, 0.499934],
#     "Bloom Filter": [0.0001, 0.001011, 0.009966, 0.102452, 0.269857, 0.56296, 0.811901],
#     "Cuckoo Filter": [0.000037, 0.00057, 0.00475, 0.037195, 0.140421, 0.252035, 0.252035],
#     "Baseline": [0, 0, 0, 0, 0, 0, 0]
# }

# true negatives query
# data = {
#     "False Positive Input": [0.0001, 0.001, 0.01, 0.1, 0.25, 0.5, 0.75],
#     "Xor Filter": [0.75891149, 0.58963396, 0.45978264, 0.42320684, 0.38975642, 0.37739664, 0.37228376],
#     "Bloom Filter": [0.705713, 0.633063, 0.646743, 0.58975, 0.590794, 0.544089, 0.469582],
#     "Cuckoo Filter": [1.993781, 1.915578, 1.913277, 1.941428, 1.883863, 1.867775, 1.863504],
#     "Baseline": [0.68332453, 0.68332453, 0.68332453, 0.68332453, 0.68332453, 0.68332453, 0.68332453]
# }

# true positives query
# data = {
#     "False Positive Input": [0.0001, 0.001, 0.01, 0.1, 0.25, 0.5, 0.75],
#     "Xor Filter": [0.52326255, 0.48563301, 0.46676733, 0.41289463, 0.40681333, 0.407201, 0.40781848],
#     "Bloom Filter": [2.180088, 1.567412, 1.134659, 0.801315, 0.695993, 0.590466, 0.499777],
#     "Cuckoo Filter": [2.392255, 2.344882, 2.3528, 2.366425, 2.343305, 2.34596, 2.311168],
#     "Baseline": [1.42841827, 1.42841827, 1.42841827, 1.42841827, 1.42841827, 1.42841827, 1.42841827]
# }

# ---- data 2 ---- #

# mixed build
# data = {
#     "False Positive Input": [0.0001, 0.001, 0.01, 0.1, 0.25, 0.5, 0.75],
#     "Xor Filter": [0.097599, 0.09688, 0.094901, 0.095058, 0.140133, 0.105057, 0.109394],
#     "Bloom Filter": [0.06644, 0.062552, 0.055534, 0.049792, 0.047912, 0.047365, 0.047905],
#     "Cuckoo Filter": [0.064214, 0.062734, 0.05995, 0.058876, 0.059151, 0.057183, 0.059131],
#     "Baseline": [0.08530900, 0.08530900, 0.08530900, 0.08530900, 0.08530900, 0.08530900, 0.08530900]
# }

# mixed query
# data = {
#     "False Positive Input": [0.0001, 0.001, 0.01, 0.1, 0.25, 0.5, 0.75],
#     "Xor Filter": [0.019189, 0.018887, 0.018838, 0.019179, 0.019672, 0.020811, 0.020984],
#     "Bloom Filter": [0.047239, 0.041764, 0.033581, 0.030251, 0.028008, 0.027182, 0.026701],
#     "Cuckoo Filter": [0.051074, 0.047852, 0.050794, 0.051826, 0.048845, 0.04589, 0.047198],
#     "Baseline": [0.01545126, 0.01545126, 0.01545126, 0.01545126, 0.01545126, 0.01545126, 0.01545126]
# }

# mixed fpr
# data= {
#     "False Positive Input": [0.0001, 0.001, 0.01, 0.1, 0.25, 0.5, 0.75],
#     "Xor Filter": [0, 0.001067, 0.0092, 0.0648, 0.2458, 0.497333, 0.497867],
#     "Bloom Filter": [0, 0.0016, 0.0096, 0.107, 0.2678, 0.5656, 0.8062],
#     "Cuckoo Filter": [0, 0.0006, 0.0068, 0.0434, 0.1562, 0.2804, 0.2804],
#     "Baseline": [0, 0, 0, 0, 0, 0, 0]
# }

# true negatives
# data = {
#     "False Positive Input": [0.0001, 0.001, 0.01, 0.1, 0.25, 0.5, 0.75],
#     "Xor Filter": [0.00503496, 0.0050168, 0.00558359, 0.00492265, 0.00506149, 0.00499156, 0.00541677],
#     "Bloom Filter": [0.006805, 0.006982, 0.00657, 0.006857, 0.00693, 0.00648, 0.006661],
#     "Cuckoo Filter": [0.007768, 0.007868, 0.007464, 0.008509, 0.007893, 0.007653, 0.007741],
#     "Baseline": [0.000879, 0.000879, 0.000879, 0.000879, 0.000879, 0.000879, 0.000879]
# }

# true positives
data = {
    "False Positive Input": [0.0001, 0.001, 0.01, 0.1, 0.25, 0.5, 0.75],
    "Xor Filter": [0.02036605, 0.01983406, 0.01948919, 0.01945619, 0.01789213, 0.01903675, 0.01898269],
    "Bloom Filter": [0.045596, 0.037666, 0.031061, 0.026284, 0.024903, 0.024518, 0.023118],
    "Cuckoo Filter": [0.048482, 0.046268, 0.047816, 0.051681, 0.044664, 0.045251, 0.044382],
    "Baseline": [0.01200566, 0.01200566, 0.01200566, 0.01200566, 0.01200566, 0.01200566, 0.01200566]
}

df = pd.DataFrame(data)

plt.figure(figsize=(10, 6))

plt.plot(df["False Positive Input"], df["Xor Filter"], marker='o', linewidth=2, label="Xor Filter")
plt.plot(df["False Positive Input"], df["Bloom Filter"], marker='s', linewidth=2, label="Bloom Filter")
plt.plot(df["False Positive Input"], df["Cuckoo Filter"], marker='^', linewidth=2, label="Cuckoo Filter")
plt.plot(df["False Positive Input"], df["Baseline"], linestyle='--', color='gray', linewidth=2, label="Baseline")

plt.xscale('log')

CHOSEN_FONT = 'DejaVu Serif'
plt.title("Total Query Time vs. False Positive Input \n Dataset 2\n Only True Positive Queries",
          fontname=CHOSEN_FONT, fontsize=16, fontweight='bold', color='#1a1a1a', pad=15)

plt.xlabel("False Positive Input (Log Scale)", fontname=CHOSEN_FONT, fontsize=12, fontweight='bold')
plt.ylabel("Time (in seconds)", fontname=CHOSEN_FONT, fontsize=12, fontweight='bold')

plt.xticks(df["False Positive Input"], labels=[str(x) for x in df["False Positive Input"]],
           fontname=CHOSEN_FONT, fontsize=10)
plt.yticks(fontname=CHOSEN_FONT, fontsize=10)

plt.legend(loc='center left', prop={'family': CHOSEN_FONT, 'size': 11})

plt.grid(True, which="both", linestyle=":", alpha=0.6)
plt.tight_layout()
plt.savefig('high_res_chart.png', dpi=1000, bbox_inches='tight')
plt.show()
