package yangyd.chelidonium.aliyun

import java.util.concurrent._
import java.util.{List ⇒ JList, Map ⇒ JMap}

import akka.actor.{ActorRef, Props}
import javax.annotation.{PostConstruct, PreDestroy}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import yangyd.chelidonium.aliyun.actor.DownloadManager.{BucketTask, Rejected, Started, WorkOrder}
import yangyd.chelidonium.aliyun.actor.{DownloadManager, DownloadMonitor}
import yangyd.chelidonium.core.{ActorSystemBean, DirService}

import scala.collection.JavaConverters._
import scala.concurrent.duration._

@Component
class DownloadService @Autowired()(actorSystemBean: ActorSystemBean,
                                   dirService: DirService,
                                   scheduled: ScheduledExecutorService)
{
  private lazy val downloadManager: ActorRef =
    actorSystemBean.actorOf(Props(classOf[DownloadManager]), "download-manager")
  private lazy val inbox = actorSystemBean.createInbox()

  private var started: Seq[Started] = Nil
  private var rejected: Seq[Rejected] = Nil
  private var completed: Seq[BucketTask] = Nil
  private var checker: ScheduledFuture[_] = _

  @PostConstruct
  def init(): Unit = {
    actorSystemBean.actorOf(Props(classOf[DownloadMonitor]), DownloadMonitor.name)
    checker = scheduled.scheduleAtFixedRate(() ⇒ checkMail(), 30, 10, TimeUnit.SECONDS)
  }

  @PreDestroy
  def cleanUp(): Unit = {
    checker.cancel(false)
  }

  private def checkMail(): Unit = {
    try {
      inbox.receive(5.seconds) match {
        case s: Started ⇒ synchronized {
          started :+= s
        }
        case j: Rejected ⇒ synchronized {
          rejected :+= j
        }
        case b: BucketTask ⇒ synchronized {
          completed :+= b
        }
      }
    } catch {
      case _: TimeoutException ⇒ ()
    }
  }

  def download(dir: String, bucket: AliyunBucket, files: JMap[String, Long]): Unit = {
    val fileSet = files.entrySet().asScala map { entry ⇒ entry.getKey → entry.getValue }
    inbox.send(downloadManager, WorkOrder(bucket, BucketTask(dirService.mkdir(dir), fileSet.toSeq)))
  }

  def startedTasks: JList[Started] = started.asJava

  def rejectedTasks: JList[Rejected] = rejected.asJava

  def completedTasks: JList[BucketTask] = completed.asJava
}
