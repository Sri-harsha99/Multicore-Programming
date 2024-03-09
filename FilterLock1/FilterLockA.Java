import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicIntegerArray;

class FilterLock {
    private AtomicIntegerArray level;
    private AtomicIntegerArray victim;
    private int numThreads;

    public FilterLock(int numThreads) {
        this.numThreads = numThreads;
        level = new AtomicIntegerArray(numThreads*4);
        victim = new AtomicIntegerArray(numThreads*4);
    }

    public void lock(int threadID) {
        for (int i = 1; i < numThreads; ++i) {
            level.set(threadID*4, i);
            victim.set(i, threadID*4);

            for (int j = 0; j < numThreads; ++j) {
                if (j != threadID) {
                    while ((level.get(j*4) >= i*4) && (victim.get(i*4) == threadID*4)) {
                        // spin
                    }
                }
            }
        }
    }

    public void unlock(int threadID) {
        level.set(threadID*4, 0);
    }
}

class Counter {
    private FilterLock lock;
    private AtomicInteger count;

    public Counter(FilterLock lock) {
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

public class FilterLockA {
    public static void main(String[] args) throws InterruptedException {
        int threadLimit = 16;
        for(int threadCount=1; threadCount<=threadLimit; threadCount++){
            int numThreads = threadCount;
            int totalIterations = 10000000;
            int eachThreadLimit = (int)totalIterations/numThreads;
            FilterLock lock = new FilterLock(numThreads);
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
