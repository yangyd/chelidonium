package yangyd.chelidonium.aliyun.actor

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import yangyd.chelidonium.aliyun.AliyunBucket
import yangyd.chelidonium.aliyun.actor.DownloadManager.BucketTask
import yangyd.chelidonium.aliyun.actor.DownloadMonitor.DownloadLog
import yangyd.chelidonium.core.AppContextAware

import scala.collection.mutable

object BucketDownloader {

  case class DownloadTask(bucket: AliyunBucket, key: String, file: File, offset: Long, size: Long)

  case class DownloadComplete(key: String)

  case class WorkerAvailable(worker: ActorRef)

  val CONCURRENCY = 2

  def props(bucketTask: BucketTask, bucket: AliyunBucket) = Props(classOf[BucketDownloader], bucketTask, bucket)
}

class BucketDownloader(bucketTask: BucketTask, bucket: AliyunBucket)
  extends Actor with AppContextAware with ActorLogging {

  import AliyunWorker._
  import BucketDownloader._

  private val monitor = context.actorSelection(s"/user/${DownloadMonitor.name}")

  monitor ! DownloadLog(s"Task submitted for ${bucket.bucketName}, ${bucketTask.files.length} files")

  private val rawTasks = bucketTask.files map {
    case (file, size) ⇒ DownloadTask(bucket, file, new File(bucketTask.destDir, file), 0L, size)
  }

  private val existFiles = rawTasks collect {
    case t: DownloadTask if t.file.exists() ⇒ t
  }

  existFiles foreach { t ⇒
    if (t.file.length() > t.size) {
      throw new IllegalStateException(s"File exists with inconsistent size: ${t.file}")
    } else {
      checkFile(t)
    }
  }

  private val tasks = mutable.Stack.empty ++ (rawTasks diff existFiles)

  private var finished: Set[String] = Set.empty
  private var idleWorkers: Set[ActorRef] = Set.empty

  // each worker initiates and watches over an actual download thread.
  // We use limited number of workers to control download concurrency.
  1 to CONCURRENCY map { i ⇒
    context.actorOf(Props(classOf[AliyunWorker], appContext), s"worker-$i")
  } foreach {
    self ! WorkerAvailable(_)
  }

  override def receive: Receive = {
    case WorkerAvailable(worker) ⇒
      if (tasks.isEmpty) {
        idleWorkers += worker
      } else {
        assign(worker, tasks.pop())
      }

    // A worker reports back, put it in pool and check if download is complete
    case Report(task) ⇒
      checkFile(task)
      self ! WorkerAvailable(sender())

    // fatal error (e.g. remote file not exist), skip the task
    case Fatal(task) ⇒
      monitor ! DownloadLog(s"File '${task.bucket.bucketName()}/${task.key}' is unable to download, skipping")
      self ! WorkerAvailable(sender())

    // checker reports an unfinished download, push it to the task stack
    case task: DownloadTask ⇒
      tasks.push(task)
      for (worker ← idleWorkers) {
        if (tasks.nonEmpty) {
          assign(worker, tasks.pop())
          idleWorkers -= worker
        }
      }

    // checker reports a finished download
    case DownloadComplete(key) ⇒
      finished += key
      monitor ! DownloadLog(s"Download completed for '${bucket.bucketName()}/$key'")
      if (finished.size == bucketTask.files.length) { // all finished
        context.parent ! bucketTask
        monitor ! DownloadLog(s"All task finished, stopping")
        context.stop(self)
      }

  } // end receive

  private def assign(worker: ActorRef, task: DownloadTask): Unit = {
    monitor ! DownloadLog(s"assigning '${bucket.bucketName()}/${task.key}'(${task.offset}/${task.size}) to ${worker.path.name}")
    worker ! task
  }

  private def checkFile(task: DownloadTask): Unit = {
    monitor ! DownloadLog(s"Download stopped for '${bucket.bucketName()}/${task.key}', pending integrity check")
    context.actorOf(Props(classOf[FileChecker])) ! task
  }
}
