package yangyd.chelidonium.aliyun.actor

import akka.actor.{Actor, ActorLogging}

class FileChecker() extends Actor with ActorLogging {
  import BucketDownloader._
  override def receive: Receive = {
    case task: DownloadTask ⇒
      val len = task.file.length()
      if (len < task.size) {
        sender() ! task.copy(offset = len)
      } else {
        sender() ! DownloadComplete(task.key)
      }
      context.stop(self)
  }
}
