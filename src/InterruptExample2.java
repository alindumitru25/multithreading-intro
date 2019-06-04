import java.math.BigInteger;

public class InterruptExample2 {
    public static void main(String[] args) {
        Thread thread = new Thread(new LongComputationTask(new BigInteger("20000"), new BigInteger("100000000")));

        // if we set daemon thread to true, the program will not wait for the thread, it will simply close it.
        // Daemon threads are useful when we do some background tasks, or operations on third party libraries that we don't necessarily
        // need to wait for.
        // thread.setDaemon(true);
        thread.start();

        // even if we call interrupt the thread will not interrupt for large numbers. It will simply stall and the process will never end.
        thread.interrupt();
    }

    private static class LongComputationTask implements Runnable {
        private BigInteger base;
        private BigInteger power;

        public LongComputationTask(BigInteger base, BigInteger power) {
            this.base = base;
            this.power = power;
        }

        @Override
        public void run() {
            System.out.println(base + "^" + "=" + pow(base, power));
        }

        private BigInteger pow(BigInteger base, BigInteger power) {
            BigInteger result = BigInteger.ONE;
            for (BigInteger i = BigInteger.ZERO; i.compareTo(power) != 0; i = i.add(BigInteger.ONE)) {
                // this solves the stall problem, checks every time to see if the thread is interrupted.
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Prematurly exit");
                    return BigInteger.ZERO;
                }

                result = result.multiply(base);
            }

            return result;
        }
    }
}
