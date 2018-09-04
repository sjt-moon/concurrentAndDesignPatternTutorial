import sun.misc.Queue;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* Customer & Producer scenario. Customer always buys from the shared queue whereas
 * producer always put new things into that queue. A naive solution is spin-waiting
 * where CPU resources are wasted. A condition variable typically conducts the
 * following behaviours,
 * 1. acquire a lock
 * 2. if (assertion holds) conduct behavior
 * 3. otherwise, wait. In detail, release the lock and sleep the current thread,
 * when resumed via signal or notify functions called from other threads, re-acquire
 * the lock and check the assertion again.
 * ---
 * lock.acquire
 * while (!assertion) {
 *     lock.release
 *     thread.sleep
 *     when resumed, lock.acquire
 * }
 * */

public class conditionVariable {
    static class Product {
        String name;
        int price;

        public Product (String name, int price) {
            this.name = name;
            this.price = price;
        }

        public String toString() {
            return name + "," + price;
        }
    }

    static class BuyAndSell {
        final Lock mutex = new ReentrantLock();
        final Condition isQueueNotFull = mutex.newCondition();
        final Condition isQueueNotEmpty = mutex.newCondition();
        java.util.Queue<Product> queue = new LinkedList<Product>();
        int maxSize;

        public BuyAndSell(int maxSize) {
            this.maxSize = maxSize;
        }

        /* Seller adds a product to the queue, thus customer could buy one */
        public void addProduct(Product product) {
            mutex.lock();
            try {
                while (queue.size() >= maxSize) {
                    /*
                    * await: atomatically release lock & wait 
                    * if signalled to wake up, Program Counter would point to here to re-start this process*/
                    isQueueNotFull.await();
                }
                queue.offer(product);
                System.out.println("Add a new product to the Q: " + product);
                isQueueNotEmpty.signal();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                mutex.unlock();
            }
        }

        /* Customer buys one product listed in the queue */
        public Product pollProduct() {
            mutex.lock();
            try {
                while (queue.size() <= 0) {
                    isQueueNotEmpty.await();
                }
                Product product = queue.poll();
                System.out.println("Buy one product from the Q: " + product.toString());
                isQueueNotFull.signal();
                return product;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                mutex.unlock();
            }
            return null;
        }
    }

    public static void main(String[] args) {
        // list of product details
        String[] productNames = {"nike", "lining", "under armour", "puma", "asics"};
        int[] prices = {1,2,3,4,5};
        Product[] products = new Product[5];
        for (int i = 0; i < prices.length; i++) {
            products[i] = new Product(productNames[i], prices[i]);
        }

        // in which both customer & seller threads conduct buying/producing behaviors
        BuyAndSell buffer = new BuyAndSell(3);

        Thread customerThread = new Thread(){
            @Override
            public void run() {
                for (int i = 0; i < products.length; i++) {
                    buffer.addProduct(products[i]);
                }
            }
        };

        Thread producerThread = new Thread(){
            @Override
            public void run() {
                for (int i = 0; i < products.length; i++) {
                    buffer.pollProduct();
                }
            }
        };

        customerThread.start();
        producerThread.start();

        try {
            customerThread.join();
            producerThread.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
