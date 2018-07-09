import com.sun.org.apache.bcel.internal.generic.MethodGen;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

/* Quick sort
 * This could be viewed as a 2-phase sorting.
 * 1. each slave independently sort a segment
 * 2. merge sorted segments together (single thread is sufficient, even more efficient (O(NlgK)))
 * */
public class QuickSort {
    public static void main(String[] args) {
        int length = 3010000;
        int[] arr = Utils.randomArrayGenerator(length);

        // parallel version
        long startTime, endTime;
        startTime = System.nanoTime();
        sort(arr);
        endTime = System.nanoTime();

        assert Utils.isSorted(arr);
        System.out.println(Runtime.getRuntime().availableProcessors() + " CPUs:\t" + (endTime - startTime) / 1000000 + " ms");

        arr = Utils.randomArrayGenerator(length);
        startTime = System.nanoTime();
        sort(arr, 1);
        endTime = System.nanoTime();

        assert Utils.isSorted(arr);
        System.out.println("Single CPU:\t" + (endTime - startTime) / 1000000 + " ms");
    }

    static void sort(int[] arr) {
        int numOfSlaves = Runtime.getRuntime().availableProcessors();
        sort(arr, numOfSlaves);
    }

    static void sort(int[] arr, int numOfSlaves) {
        QuickSortSlave[] slaves = quickSortSlaveScheduler(arr, numOfSlaves);
        if (numOfSlaves > 1) mergeKSortedArray(arr, slaves);
    }

    /* merge slaves.arr (sorted already) into arr */
    static void mergeKSortedArray(int[] arr, QuickSortSlave[] slaves) {
        PriorityQueue<SlaveStruct> pq = new PriorityQueue<>(new Comparator<SlaveStruct>() {
            @Override
            public int compare(SlaveStruct o1, SlaveStruct o2) {
                return o1.value - o2.value;
            }
        });
        for (int i = 0; i < slaves.length; i++) {
            pq.offer(new SlaveStruct(i, 0, slaves[i].arr[0]));
        }

        int k = 0;
        SlaveStruct curr;
        while (!pq.isEmpty()) {
            curr = pq.poll();
            arr[k++] = curr.value;
            curr.slaveIndex++;
            if (curr.slaveIndex < slaves[curr.slaveId].arr.length) pq.offer(curr);
        }
    }

    static QuickSortSlave[] quickSortSlaveScheduler(int[] arr, int numOfSlaves) {
        int n = arr.length;
        int start = 0, avgSegementLength = n / numOfSlaves;

        QuickSortSlave[] slaves = new QuickSortSlave[numOfSlaves];
        for (int i = 0; i < numOfSlaves; i++) {
            slaves[i] = new QuickSortSlave(arr, start, Math.min(start + avgSegementLength, n-1));
            slaves[i].start();

            start += avgSegementLength + 1;
        }
        try {
            for (int i = 0; i < numOfSlaves; i++) {
                slaves[i].join();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return slaves;
    }

    /* Slave is a single-thread worker */
    static class QuickSortSlave extends Thread {
        int[] arr;

        public QuickSortSlave(int[] arr, int start, int end) {
            this.arr = new int[end - start + 1];
            System.arraycopy(arr, start, this.arr, 0, end - start + 1);
        }

        @Override
        public void run() {
            sort(arr, 0, arr.length - 1);
        }

        public void sort(int[] arr, int start, int end) {
            if (start >= end) return;
            int p = partition(arr, start, end);
            sort(arr, start, p-1);
            sort(arr, p+1, end);
        }

        public int partition(int[] arr, int start, int end) {
            int i = start - 1, j = start, pivot = arr[end];
            while (j < end) {
                if (arr[j] < pivot) {
                    swap(arr, ++i, j);
                }
                j++;
            }
            swap(arr, ++i, end);
            return i;
        }

        public void swap(int[] arr, int i, int j) {
            int tempt = arr[i];
            arr[i] = arr[j];
            arr[j] = tempt;
        }
    }

    static class SlaveStruct {
        int slaveId, slaveIndex, value;
        public SlaveStruct(int slaveId, int slaveIndex, int value) {
            this.slaveId = slaveId;
            this.slaveIndex = slaveIndex;
            this.value = value;
        }
    }

    static class Utils {
        public static int[] randomArrayGenerator(int length) {
            Random rand = new Random(1);
            int[] arr = new int[length];
            for (int i = 0; i < length; i++) arr[i] = rand.nextInt();
            return arr;
        }

        public static boolean isSorted(int[] arr) {
            int n = arr.length;
            for (int i = 0; i < n-1; i++) {
                if (arr[i] > arr[i+1]) return false;
            }
            return true;
        }
    }
}
