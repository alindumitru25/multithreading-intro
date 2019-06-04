import java.math.BigInteger;

public class InterruptEx3 {

    public static void main(String[] args) {
        ComplexCalculation calc = new ComplexCalculation();
        try {
            BigInteger res = calc.calculateResult(new BigInteger("30"), new BigInteger("400"),
                    new BigInteger("30"), new BigInteger("500"));
            System.out.println(res);
        } catch(InterruptedException exc) {

        }

    }

    public static class ComplexCalculation {
        public BigInteger calculateResult(BigInteger base1, BigInteger power1, BigInteger base2, BigInteger power2) throws InterruptedException {
            BigInteger result;

            PowerCalculatingThread a = new PowerCalculatingThread(base1, power1);
            PowerCalculatingThread b = new PowerCalculatingThread(base2, power2);

            a.start();
            b.start();

            a.join();
            b.join();

            BigInteger result1 = a.getResult();
            BigInteger result2 = b.getResult();

            result = result1.add(result2);
            return result;
        }

        private static class PowerCalculatingThread extends Thread {
            private BigInteger result = BigInteger.ONE;
            private BigInteger base;
            private BigInteger power;

            public PowerCalculatingThread(BigInteger base, BigInteger power) {
                this.base = base;
                this.power = power;
            }

            @Override
            public void run() {
                this.result = pow(base, power);
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

            public BigInteger getResult() { return result; }
        }
    }
}
