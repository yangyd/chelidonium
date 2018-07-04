package yangyd.chelidonium;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SpringBootApplication
class Chelidonium {
  @Bean(name = "pooled")
  ExecutorService executorService() {
    return Executors.newCachedThreadPool();
  }

  @Bean(name = "stpe")
  ScheduledExecutorService scheduledExecutorService() {
    return Executors.newScheduledThreadPool(5);
  }
}
