package yangyd.chelidonium.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

class SimpleResponse<T> {
  private final CountDownLatch latch = new CountDownLatch(1);
  private final AtomicReference<ResponseEntity<T>> result = new AtomicReference<>();

  // Can't make SimpleResponse extend from Callable as Spring MVC
  // requires parameterized types being returned from controllers
  final Callable<ResponseEntity<T>> handle = this::take;

  void ok() {
    fullfill(HttpStatus.OK);
  }

  void notFound() {
    fullfill(HttpStatus.NOT_FOUND);
  }

  void conflict() {
    fullfill(HttpStatus.CONFLICT);
  }

  void reject() {
    fullfill(HttpStatus.BAD_REQUEST);
  }

  void error() {
    fullfill(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  void fullfill(T body) {
    fullfill(HttpStatus.OK, body);
  }

  void fullfill(HttpHeaders headers) {
    fullfill(HttpStatus.OK, headers);
  }

  void fullfill(HttpHeaders headers, T body) {
    fullfill(HttpStatus.OK, headers, body);
  }

  void fullfill(HttpStatus status) {
    offer(new ResponseEntity<>(status));
  }

  void fullfill(HttpStatus status, HttpHeaders headers) {
    offer(new ResponseEntity<>(headers, status));
  }

  void fullfill(HttpStatus status, T body) {
    offer(new ResponseEntity<>(body, status));
  }

  void fullfill(HttpStatus status, HttpHeaders headers, T body) {
    offer(new ResponseEntity<>(body, headers, status));
  }

  private void offer(ResponseEntity<T> responseEntity) {
    result.set(responseEntity);
    latch.countDown();
  }

  private ResponseEntity<T> take() {
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
    return result.get();
  }

}
