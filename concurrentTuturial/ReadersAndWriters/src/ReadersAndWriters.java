import javax.xml.soap.SAAJMetaFactory;
import java.util.concurrent.Semaphore;

public class ReadersAndWriters {
    public static final Semaphore accessToResource = new Semaphore(1, true);
    public static final Semaphore readCntMutex = new Semaphore(1, true);
    public static final Semaphore waitingQueue = new Semaphore(1, true);
    public static String content = "planets";
    public static int numOfReaders;

    static class Reader extends Thread {
        void read() {
            try {
                waitingQueue.acquire();

                // update readers count
                readCntMutex.acquire();
                numOfReaders++;
                if (numOfReaders == 1) {
                    accessToResource.acquire();
                }
                readCntMutex.release();

                // start to process
                waitingQueue.release();

                // read content, not a critical section
                System.out.println("Read content:\t" + content);

                readCntMutex.acquire();
                numOfReaders--;
                if (numOfReaders <= 0) {
                    accessToResource.release();
                }
                readCntMutex.release();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            read();
        }
    }

    static class Writer extends Thread {
        String text;

        Writer(String text) {
            this.text = text;
        }

        void write() {
            try {
                waitingQueue.acquire();

                accessToResource.acquire();

                // start processing
                waitingQueue.release();

                // critical section
                content = text;
                System.out.println("Content changed:\t" + content);
                // end of critical section

                accessToResource.release();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            write();
        }
    }

    public static void main(String[] args) {
        Reader t1 = new Reader();
        Reader t2 = new Reader();
        Writer t3 = new Writer("stars");
        Reader t4 = new Reader();
        Writer t5 = new Writer("restaurant at the end of universe");
        Reader t6 = new Reader();
        Reader t7 = new Reader();
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
        t7.start();
    }
}
