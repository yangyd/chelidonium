package yangyd.chelidonium.core;

public class Overwatch {
  private final long threshold;

  private boolean stop = false;
  private long sum = 0;

  public Overwatch(long threshold) {
    this.threshold = threshold;
  }

  public synchronized void report(long n) {
    if (stop) {
      throw new StopException();
    }
    sum += n;
  }

  public synchronized void check() {
    if (sum < threshold) {
      stop = true;
    }
    sum = 0;
  }

  public static class StopException extends RuntimeException {
  }

}
