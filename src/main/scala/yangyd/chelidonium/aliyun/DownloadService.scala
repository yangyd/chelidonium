package yangyd.chelidonium.aliyun

import java.util.concurrent._
import java.util.{List ⇒ JList, Map ⇒ JMap}
import javax.annotation.{PostConstruct, PreDestroy}

import akka.actor.{ActorRef, Props}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component
import yangyd.chelidonium.aliyun.actor.DownloadManager.{BucketTask, Rejected, Started, WorkOrder}
import yangyd.chelidonium.aliyun.actor.{DownloadManager, DownloadMonitor}
import yangyd.chelidonium.core.{ActorSystemBean, DirService}

import scala.collection.JavaConversions._
import scala.concurrent.duration._

object DownloadService {
  implicit def runnable(fn: ⇒ Unit): Runnable = new Runnable { override def run(): Unit = fn }
}

@Component
class DownloadService @Autowired() (actorSystemBean: ActorSystemBean,
                                    dirService: DirService,
                                    @Qualifier("pooled") executor: ExecutorService,
                                    scheduled: ScheduledExecutorService)
{
  lazy val downloadManager: ActorRef = actorSystemBean.actorOf(Props(classOf[DownloadManager]), "dlmgr")
  lazy val inbox = actorSystemBean.createInbox()

  var started: Seq[Started] = Nil
  var rejected: Seq[Rejected] = Nil
  var completed: Seq[BucketTask] = Nil
  var checker: ScheduledFuture[_] = _

  import DownloadService._

  @PostConstruct
  def init(): Unit = {
    actorSystemBean.actorOf(Props(classOf[DownloadMonitor]), DownloadMonitor.name)
    checker = scheduled.scheduleAtFixedRate(checkMail, 30, 10, TimeUnit.SECONDS)
  }

  @PreDestroy
  def cleanUp(): Unit = {
    checker.cancel(false)
  }

  private def checkMail(): Unit = {
    try {
      inbox.receive(5.seconds) match {
        case s: Started ⇒ synchronized { started :+= s }
        case j: Rejected ⇒ synchronized { rejected :+= j }
        case b: BucketTask ⇒ synchronized { completed :+= b }
      }
    } catch {
      case _:TimeoutException ⇒ ()
    }
  }

  def download(dir: String, bucket: AliyunBucket, files: JMap[String, Long]): Unit = {
    inbox.send(downloadManager, WorkOrder(bucket, BucketTask(dirService.mkdir(dir), files.toList)))
  }

  def startedTasks: JList[Started] = started
  def rejectedTasks: JList[Rejected] = rejected
  def completedTasks: JList[BucketTask] = completed
}
