import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

/* Parallel merge sort.
*
* With 4 cores working in parallel, multi-threads merge sort outperforms single-thread one when more than 3 million elements are sorted.
* */
public class mergeSort {
    public static int[] randomArrayGenerator(int length) {
        Random rand = new Random(1);
        int[] arr = new int[length];
        for (int i = 0; i < length; i++) arr[i] = rand.nextInt();
        return arr;
    }

    /* assign tasks to different CPUs
    *
    * default using all the CPUs
    * you could also have certain number of slaves
    *
    * Attention: input arr has been sorted
    * */
    public static void parallelSort(int[] arr) {
        int numOfSlaves = Runtime.getRuntime().availableProcessors();
        parallelSort(arr, numOfSlaves);
    }

    public static void parallelSort(int[] arr, int numOfSlaves) {
        // get number of cores (number of slaves)
        System.out.println(numOfSlaves + " slaves created");

        // assign tasks to different slaves
        int n = arr.length, arrayLengthPerSlave = n / numOfSlaves, startIndex = 0, endIndex = startIndex + arrayLengthPerSlave;
        MergeSortSlave[] slaves = new MergeSortSlave[numOfSlaves];
        for (int i = 0; i < numOfSlaves; i++) {
            slaves[i] = new MergeSortSlave(arr, startIndex, Math.min(n - 1, endIndex));
            slaves[i].start();
            startIndex = endIndex + 1;
            endIndex += arrayLengthPerSlave;
        }
        try {
            for (int i = 0; i < numOfSlaves; i++) slaves[i].join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        // merge results back to the original array (i.e. parameter arr)
        PriorityQueue<SlaveIndex> pq = new PriorityQueue<>(new Comparator<SlaveIndex>() {
            @Override
            public int compare(SlaveIndex o1, SlaveIndex o2) {
                return o1.value - o2.value;
            }
        });
        for (int i = 0; i < numOfSlaves; i++) {
            pq.offer(new SlaveIndex(i, 0, slaves[i].arr[0]));
        }
        int i = 0;
        while (!pq.isEmpty()) {
            SlaveIndex si = pq.poll();
            arr[i++] = si.value;
            si.slaveIndex++;
            if (si.slaveIndex < slaves[si.slaveId].arr.length) {
                pq.offer(si);
            }
        }
    }

    public static boolean assertSorted(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            if (arr[i] > arr[i+1]) {
                return false;
            }
        }
        return true;
    }

    /* mergeSortSlave: single thread slave
    *
    * Each slave maintains a local copy of original array segment, that local copy will be sorted.
    * To make the original array to be sorted, you need to merge all these local copies (implemented
    * in parallelSort()).
    *
    * static, otherwise generating a compiler error: this cannot be referenced from a static context
    * the reason is that, to create a inner class instance (non-static), Java implicitly constructs its
    * parent class as well. However, in a static context like main(), there is no such parent instance.
    * */
    static class MergeSortSlave extends Thread {
        int[] arr;

        public MergeSortSlave(int[] arr, int start, int end) {
            this.arr = new int[end - start + 1];
            System.arraycopy(arr, start, this.arr, 0, end - start + 1);
        }

        /* merge arr[start:mid] and arr[mid+1:end], still store merged results on arr*/
        public void merge(int[] arr, int start, int mid, int end) {
            int[] arrToBeMerged = new int[end - start + 1];
            System.arraycopy(arr, start, arrToBeMerged, 0, end - start + 1);
            int i = 0, j = 0, k = 0;
            while (i < mid - start + 1 && j < end - mid) {
                arr[k++] = arrToBeMerged[i] < arrToBeMerged[j] ? arrToBeMerged[i++] : arrToBeMerged[j++];
            }
            while (i < mid - start + 1) {
                arr[k++] = arrToBeMerged[i++];
            }
            while (j < end - mid) {
                arr[k++] = arrToBeMerged[j++];
            }
        }

        public void sort(int[] arr, int start, int end) {
            if (start >= end) return;
            int mid = start + (end - start) / 2;
            sort(arr, start, mid);
            sort(arr, mid + 1, end);
            merge(arr, start, mid, end);
        }

        @Override
        public void run() {
            sort(arr, 0, arr.length - 1);
        }
    }

    public static void main(String[] args) {
        // create random array
        int length = 3010000;
        int[] arr = randomArrayGenerator(length);

        // call slaves to sort in parallel
        long startTime = System.nanoTime();
        parallelSort(arr);
        long endTime = System.nanoTime();
        System.out.println("Multi-thread:\t" + (endTime - startTime) / 1000000 + " ms");

        assert assertSorted(arr);

        // single slave
        arr = randomArrayGenerator(length);
        startTime = System.nanoTime();
        parallelSort(arr, 1);
        endTime = System.nanoTime();
        System.out.println("Single thread:\t" + (endTime - startTime) / 1000000 + " ms");

        assert assertSorted(arr);
    }
}

/* To merge k sorted array, a priority queue is needed in which (slave id, slave index) pairs are stored. */
class SlaveIndex {
    int slaveId, slaveIndex, value;
    public SlaveIndex(int slaveId, int slaveIndex, int value) {
        this.slaveId = slaveId;
        this.slaveIndex = slaveIndex;
        this.value = value;
    }
}