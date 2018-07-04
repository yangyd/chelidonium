package yangyd.chelidonium.aliyun.actor

import java.io.{File, FilenameFilter}

import akka.actor.{Actor, ActorLogging}
import yangyd.chelidonium.aliyun.AliyunBucket

import scala.util.Random

object DownloadManager {
  case class BucketTask(destDir: File, files: Seq[(String, Long)])
  case class WorkOrder(bucket: AliyunBucket, task: BucketTask)
  case class Rejected(bucket: String, task: BucketTask)
  case class Started(bucket: String, task: BucketTask)

  implicit def filter(p: (String) ⇒ Boolean): FilenameFilter = new FilenameFilter {
    override def accept(dir: File, name: String): Boolean = p(name)
  }
}

class DownloadManager extends Actor with ActorLogging {
  import DownloadManager._
  override def receive: Receive = {
    case WorkOrder(bucket, task) ⇒
      val actorName = s"${bucket.bucketName()}-dl-${Random.nextInt(100000)}"
      // put a lock file to prevent conflict
      if (task.destDir.isDirectory && !locked(task.destDir) &&
          new File(task.destDir, actorName + ".lock").createNewFile()) {
        context.actorOf(BucketDownloader.props(task, bucket), actorName)
        sender() ! Started(bucket.bucketName, task)
      } else {
        log.warning(s"Task [{} -> {}] rejected", bucket.bucketName(), task.destDir)
        sender() ! Rejected(bucket.bucketName, task)
      }

    case t: BucketTask ⇒
      new File(t.destDir, sender.path.name + ".lock").delete()
      context.parent forward t // completed task
  } // end receive

  private def locked(dir: File) = dir.listFiles((f:String) ⇒ f endsWith ".lock").length > 0
}
