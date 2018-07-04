import java.util.Arrays;
import java.util.concurrent.Semaphore;

class H2OGenerator {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(1);
        HCreator hCreator = new HCreator(semaphore);
        OCreator oCreator = new OCreator(semaphore);
    }
}

class HCreator implements Runnable {
    Semaphore semaphore;

    public HCreator(Semaphore s) {
        this.semaphore = s;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                semaphore.acquire();
                if (SharedMemory.result.length() >= 3) {
                    System.out.println(SharedMemory.result);
                    SharedMemory.result = "";
                }
                if (!SharedMemory.result.equals("HH")) {
                    SharedMemory.result += "H";
                    char[] arr = SharedMemory.result.toCharArray();
                    Arrays.sort(arr);
                    SharedMemory.result = new String(arr);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                semaphore.release();
            }
        }
    }
}

class OCreator implements Runnable {
    Semaphore semaphore;

    public OCreator(Semaphore s) {
        this.semaphore = s;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                semaphore.acquire();
                if (SharedMemory.result.length() >= 3) {
                    System.out.println(SharedMemory.result);
                    SharedMemory.result = "";
                }

                boolean hasO = false;
                for (char c: SharedMemory.result.toCharArray()) {
                    if (c == 'O') {
                        hasO = true;
                        break;
                    }
                }

                if (!hasO) {
                    SharedMemory.result += "O";
                    char[] arr = SharedMemory.result.toCharArray();
                    Arrays.sort(arr);
                    SharedMemory.result = new String(arr);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                semaphore.release();
            }
        }
    }
}

class SharedMemory {
    static String result = "";
}