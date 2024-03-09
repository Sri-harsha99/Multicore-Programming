import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

class PetersonsLock {
    private AtomicBoolean[] flag;
    private AtomicInteger victim;

    public PetersonsLock(int n) {
        flag = new AtomicBoolean[n];
        for (int i = 0; i < n; i++) {
            flag[i] = new AtomicBoolean(false);
        }
        victim = new AtomicInteger();
    }

    public void lock(int i) {
        flag[i].set(true);
        victim.set(i);
        for (int j = 0; j < flag.length; j++) {
            if (j != i) {
                while (flag[j].get() && victim.get() == i) {
                    // spin
                }
            }
        }
    }

    public void unlock(int i) {
        flag[i].set(false);
    }
}

class FilterLock2 {
    private PetersonsLock[] level;

    public FilterLock2(int n) {
        level = new PetersonsLock[n];
        for (int i = 0; i < n; i++) {
            level[i] = new PetersonsLock(n);
        }
    }

    public void lock() {
        for (int i = 0; i < level.length; i++) {
            level[i].lock(i);
        }
    }

    public void unlock() {
        for (int i = level.length - 1; i >= 0; i--) {
            level[i].unlock(i);
        }
    }
}

class Counter {
    private FilterLock2 lock;
    private AtomicInteger count;

    public Counter(FilterLock2 lock) {
        this.lock = lock;
        this.count = new AtomicInteger();
    }

    public void increment(int threadID) {
        lock.lock();
        count.incrementAndGet();
        lock.unlock();
    }

    public int getCount() {
        return count.get();
    }
}

public class FilterLockB {
    public static void main(String[] args) throws InterruptedException {
        int threadLimit = 16;
        for(int threadCount=1;threadCount<=threadLimit;threadCount++){
            int numThreads = threadCount;
            int totalIterations = 10000000;
            int eachThreadLimit = (int)totalIterations/numThreads;
            FilterLock2 lock = new FilterLock2(numThreads);
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
                        count+=1;
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
