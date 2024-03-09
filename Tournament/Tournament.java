import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

class PetersonLock {
    private AtomicBoolean[] flag;
    private AtomicInteger victim;

    PetersonLock() {
        flag = new AtomicBoolean[2];
        for (int i = 0; i < 2; ++i) {
            flag[i] = new AtomicBoolean(false);
        }

        victim = new AtomicInteger(-1);
    }

    void enter(int threadID) {
        flag[threadID].set(true);
        victim.set(threadID);

        while (flag[(threadID + 1) % 2].get() && victim.get() == threadID) {
            // spin
        }
    }

    void exit(int threadID) {
        flag[threadID].set(false);
    }
}

class TournamentTreeLock {
    private PetersonLock[] level;

    TournamentTreeLock(int len) {
        level = new PetersonLock[len - 1];
        for (int i = 0; i < len - 1; ++i) {
            level[i] = new PetersonLock();
        }
    }

    void lock(int threadID) {
        int node = threadID + level.length;
        while (node > 0) {
            int parent = (node % 2 == 0) ? ((node - 2) / 2) : ((node - 1) / 2);
            level[parent].enter((node + 1) % 2);
            node = parent;
        }
    }

    void unlock(int threadID) {
        int root = 0;
        int width = level.length+1;
        while (root < level.length) {
            int left = 2 * root + 1;
            int right = 2 * root + 2;
            if (threadID < width / 2) {
                level[root].exit(0);
                root = left;
            } else {
                level[root].exit(1);
                root = right;
                threadID -= width / 2;
            }
            width /= 2;
        }
    }
}

class Counter {
    private TournamentTreeLock lock;
    private AtomicInteger count;

    public Counter(TournamentTreeLock lock) {
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

public class Tournament {
    public static void main(String[] args) throws InterruptedException {
        int threadLimit = 16;

        for(int threadCount=2; threadCount<=threadLimit; threadCount*=2){
            int numThreads = threadCount;

            int totalIterations = 10000000;
            int eachThreadLimit = (int)totalIterations/numThreads;
            TournamentTreeLock lock = new TournamentTreeLock(numThreads);
            Counter counter = new Counter(lock);
            AtomicLong turnAroundTime = new AtomicLong(0);
            
            Thread[] threads = new Thread[numThreads];
            
            for (int i = 0; i < numThreads; i++) {
                final int finalI = i;
            
                threads[i] = new Thread(() -> {
                    int count = 0;
                    while (count < eachThreadLimit) {
                        long intialTime = System.nanoTime();
                        counter.increment(finalI);
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