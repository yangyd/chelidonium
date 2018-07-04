package yangyd.chelidonium.aliyun.actor

import java.util.concurrent.atomic.AtomicReference

import akka.actor.{Actor, ActorLogging, Cancellable, IndirectActorProducer}
import com.aliyun.oss.OSSException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import yangyd.chelidonium.aliyun.actor.BucketDownloader.DownloadTask
import yangyd.chelidonium.core.{DataTransport, Overwatch}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object AliyunWorker {

  import com.aliyun.oss.OSSErrorCode._

  // No retry for these errors
  val fatalErrors = Set(NO_SUCH_KEY, NO_SUCH_BUCKET, ACCESS_DENIED, INVALID_ACCESS_KEY_ID)

  // at least 10k byte/s
  val THRESHOLD: Long = 1024 * 10 * 30

  case class Report(task: DownloadTask)

  case class Fatal(task: DownloadTask)

  case class Check(overwatch: Overwatch)

}

class AliyunWorker(applicationContext: ApplicationContext) extends IndirectActorProducer {
  override def actorClass: Class[_ <: Actor] = classOf[AliyunWorkerActor]

  override def produce(): Actor = applicationContext.getBean(classOf[AliyunWorkerActor])
}

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class AliyunWorkerActor @Autowired()(dataTransport: DataTransport) extends Actor with ActorLogging {

  import AliyunWorker._
  import context.{dispatcher, system}

  override def receive: Receive = {
    case Check(overwatch) ⇒ overwatch.check()

    case task@DownloadTask(bucket, key, file, offset, size) ⇒
      val _sender = sender()
      val _checker = new AtomicReference[Cancellable]()

      def done(m: AnyRef): Unit = {
        system.scheduler.scheduleOnce(15.seconds, _sender, m)
        _checker.get() match {
          case c: Cancellable ⇒ c.cancel()
        }
      }

      Future {
        bucket.readFile(key, offset)
      } flatMap {
        val overwatch = new Overwatch(THRESHOLD)
        _checker.set(system.scheduler.schedule(30.seconds, 30.seconds, self, Check(overwatch)))
        dataTransport.transfer(file, _, Some(overwatch))
      } onComplete {
        case Success(count) ⇒
          log.info(s"Finished downloading '$file', with $count bytes written")
          done(Report(task))

        case Failure(e) ⇒ e match {
          case oe: OSSException if fatalErrors contains oe.getErrorCode ⇒
            log.error(s"Unable to download file '$key': ${oe.getMessage}")
            done(Fatal(task))
          case se: Overwatch.StopException ⇒
            log.warning("Download of '{}' aborted due to slow connection.", key)
            done(Report(task))
          case _ ⇒
            log.warning("Download of '{}' interrupted: {}", key, e.getMessage)
            done(Report(task))
        }
      }
  }

}
