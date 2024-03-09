import matplotlib.pyplot as plt

# Number of threads
threads = [1, 2, 3, 4, 5,6,7,8,9,10,11,12,13,14,15,16]
threads2 = [2,4,8,16]

# Milliseconds for each lock type
filter_lock_a = [12732, 7610, 7997, 3142, 2311,2166,1533,1306,1144,967,973,871,829,1065,861,977]
filter_lock_b = [
    11107.507342062352, 4747.947153828834, 2970.2720000123563,
    2362.767342103878, 2957.864568491632, 2044.9096697600437,
    2230.2489152567828, 2135.963290822253, 1883.983245132451,
    2639.056018107302, 1648.036583284502, 1627.737597330405,
    1468.718166419858, 1316.843053141597, 1380.5925929034818,
    1256.4012702417867
]
bakery = [
    11183.704537262482, 9057.70601095638, 5396.359399730505,
    2784.0802394197644, 2526.165644824243, 2370.3757713176215,
    2476.0365397735522, 2114.2922093913817, 2110.8024484382095,
    2136.2976713757216, 1979.6353519912964, 1762.4862995275812,
    1500.6532124868097, 1317.5439585805925, 574.8049627330579,
    226.89682832858452
]
tournament = [18571,3602,2932,2345]

plt.figure(figsize=(10, 16))

# Plot each lock type
plt.plot(threads, filter_lock_a, marker='o', label='Filter Lock A')
plt.plot(threads, filter_lock_b, marker='o', label='Filter Lock B')
plt.plot(threads, bakery, marker='o', label='Bakery')
plt.plot(threads2, tournament, marker='o', label='Tournament')

# Add labels and title
plt.xlabel('Number of Threads')
plt.ylabel('No.of Critical sections per unit time')
plt.title('Throughput')

# Add a legend
plt.legend()

# Show the plot
plt.show()