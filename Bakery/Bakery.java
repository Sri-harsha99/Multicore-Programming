import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

class BakeryLock {
    private AtomicBoolean[] entering;
    private AtomicIntegerArray number;
    private int numThreads;

    public BakeryLock(int numThreads) {
        this.numThreads = numThreads;
        entering = new AtomicBoolean[numThreads];
        number = new AtomicIntegerArray(numThreads);

        for (int i = 0; i < numThreads; ++i) {
            entering[i] = new AtomicBoolean();
            number.set(i, 0);
        }
    }

    private int max() {
        int max = number.get(0);
        for (int i = 1; i < numThreads; ++i) {
            int value = number.get(i);
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public void lock(int threadID) {
        entering[threadID].set(true);
        number.set(threadID, 1 + max());

        for (int j = 0; j < numThreads; ++j) {
            if (j != threadID) {
                while (entering[j].get() && (number.get(j) < number.get(threadID) || (number.get(j) == number.get(threadID) && j < threadID)) ) {
                    // spin
                }
            }
        }
    }

    public void unlock(int threadID) {
        entering[threadID].set(false);
    }
}

class Counter {
    private BakeryLock lock;
    private AtomicInteger count;

    public Counter(BakeryLock lock) {
        this.lock = lock;
        this.count = new AtomicInteger();
    }

    public void increment(int threadID) {
        lock.lock(threadID);
        count.incrementAndGet();
        lock.unlock(threadID);
    }

    public int getCount() {
        return count.get();
    }
}

public class Bakery {
    public static void main(String[] args) throws InterruptedException {
        int threadLimit = 16;
        for(int threadCount=1;threadCount<=threadLimit;threadCount++){
        
            int numThreads = threadCount;
            int totalIterations = 10000000;
            int eachThreadLimit = (int)totalIterations/numThreads;
            BakeryLock lock = new BakeryLock(numThreads);
            Counter counter = new Counter(lock);
            AtomicLong turnAroundTime = new AtomicLong(0);

            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; ++i) {
                final int threadID = i;
                threads[i] = new Thread(() -> {
                    int count = 0;
                    while (count < eachThreadLimit) {
                        long intialTime = System.nanoTime();
                        counter.increment(threadID);
                        long finalTime = System.nanoTime();
                        turnAroundTime.addAndGet(finalTime-intialTime);
                        count += 1;
                    }
                });
            }

            long startTime = System.nanoTime();
            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
            long endTime = System.nanoTime();

            System.out.println("Turn around time  " + (turnAroundTime.get())/ (numThreads*1e6) + " ms");

            double totalTimeTaken = (endTime - startTime) / 1e6;


            System.out.println("Total Time for program with "+ threadCount + " threads: "  + totalTimeTaken + " ms");
            System.out.println("Throughput with "+ threadCount + " threads: "  + counter.getCount()/totalTimeTaken);
        }
    }
}
