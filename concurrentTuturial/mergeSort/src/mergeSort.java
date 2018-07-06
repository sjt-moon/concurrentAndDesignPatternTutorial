import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.Lock;

public class mergeSort {
    public static int[] merge(int[] a, int[] b) {
        if (a == null || a.length < 1) return b;
        if (b == null || b.length < 1) return a;
        int m = a.length, n = b.length, i = 0, j = 0, k = 0;
        int[] mergedArray = new int[m + n];
        while (i < m && j < n) {
            mergedArray[k++] = a[i] < b[j] ? a[i++] : b[j++];
        }
        while (i < m) mergedArray[k++] = a[i++];
        while (j < n) mergedArray[k++] = b[j++];
        return mergedArray;
    }

    /* static, otherwise generating a compiler error: this cannot be referenced from a static context
    * the reason is that, to create a inner class instance (non-static), Java implicitly constructs its
    * parent class as well. However, in a static context like main(), there is no such parent instance.
    * */
    static class mergeSortSlave extends Thread {
        int[] arr;

        public mergeSortSlave(int[] arr, int start, int end) {
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
        Random rand = new Random();
        int[] arr = new int[100000];
        for (int i = 0; i < arr.length; i++) arr[i] = rand.nextInt(50000);

        // call slaves to sort in parallel
        long startTime = System.nanoTime();
        int n = arr.length, m = n / 2;
        Thread slave1 = new mergeSortSlave(arr, 0, m);
        Thread slave2 = new mergeSortSlave(arr, m + 1, n - 1);
        try {
            slave1.start();
            slave2.start();
            slave1.join();
            slave2.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        arr = merge(((mergeSortSlave) slave1).arr, ((mergeSortSlave) slave2).arr);

        long endTime = System.nanoTime();
        System.out.println("Bi-thread: " + (endTime - startTime) / 1000 + " ms");

        // single slave
        rand = new Random();
        for (int i = 0; i < arr.length; i++) arr[i] = rand.nextInt(50000);

        startTime = System.nanoTime();
        Thread slave3 = new mergeSortSlave(arr, 0, arr.length - 1);
        try {
            slave3.start();
            slave3.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        endTime = System.nanoTime();
        System.out.println("Single thread: " + (endTime - startTime) / 1000 + " ms");
    }
}
