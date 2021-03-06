package yangyd.chelidonium.core

import java.io.{File, FileOutputStream, InputStream, OutputStream}
import java.util.concurrent.ExecutorService

import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component
import resource.managed

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

object DataTransport {
  val BUF_SIZE: Int = 16 * 1024

  private def transfer0(outputStream: OutputStream, inputStream: InputStream, overwatch: Option[Overwatch]): Long = {
    var sum = -1L
    for (in ← managed(inputStream); out ← managed(outputStream)) {
      val buf = new Array[Byte](BUF_SIZE)

      @tailrec def pump(sum: Long): Long = in.read(buf) match {
        case -1 ⇒ sum
        case n ⇒
          for {ow ← overwatch} ow report n
          out.write(buf, 0, n)
          pump(sum + n)
      }

      sum = pump(0)
    } // end for-managed
    sum
  }
}

@Component
class DataTransport @Autowired()(@Qualifier("pooled") executorService: ExecutorService) {
  import DataTransport._

  implicit private val executionContext: ExecutionContext = ExecutionContext.fromExecutor(executorService)
  //  private val scheduler = ExecutionContextScheduler(executionContext)

  /**
    * Pipe data from the inputStream to the file. Data will be appended to the end of the file.
    *
    * @param file        source
    * @param inputStream destination
    * @return Observable[Long]
    */
  def transfer(file: File, inputStream: InputStream, overwatch: Option[Overwatch]): Future[Long] =
    transfer(new FileOutputStream(file, true), inputStream, overwatch)

  def transfer(outputStream: OutputStream, inputStream: InputStream, overwatch: Option[Overwatch]): Future[Long] =
    Future {transfer0(outputStream, inputStream, overwatch)}

}
