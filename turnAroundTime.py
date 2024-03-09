import matplotlib.pyplot as plt

# Number of threads
threads = [1, 2, 3, 4, 5,6,7,8,9,10,11,12,13,14,15,16]
threads2 = [2,4,8,16]

# Milliseconds for each lock type
filter_lock_a = [419, 975, 946, 2934, 3934,4367,6089,7223,8367,10122,10051,10890,11814,11919,11609,10231]
filter_lock_b = [530, 1802, 3083, 4005, 3170, 4715, 4327, 4540, 5162,3660,5912,6031,6710,7446,7124,7623]
bakery = [
    525.0941, 854.29295, 1666.3942666666667, 3452.531375,
    3847.07778, 4127.8347, 3964.166885714286, 4649.0051125,
    4636.396655555555, 4613.73307, 4988.713709090909, 5595.846033333333,
    6601.951338461538, 7513.031221428571, 17328.666306666666, 34023.7808
]
tournament = [343,2522,3250,4024]

plt.figure(figsize=(10, 16))

# Plot each lock type
plt.plot(threads, filter_lock_a, marker='o', label='Filter Lock A')
plt.plot(threads, filter_lock_b, marker='o', label='Filter Lock B')
plt.plot(threads, bakery, marker='o', label='Bakery')
plt.plot(threads2, tournament, marker='o', label='Tournament')

# Add labels and title
plt.xlabel('Number of Threads')
plt.ylabel('Milliseconds')
plt.title('Turn Around Time')

# Add a legend
plt.legend()

# Show the plot
plt.show()