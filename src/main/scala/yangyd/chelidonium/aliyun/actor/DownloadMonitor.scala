package yangyd.chelidonium.aliyun.actor

import akka.actor.{Actor, ActorLogging}
import yangyd.chelidonium.aliyun.actor.DownloadMonitor.DownloadLog

object DownloadMonitor {
  val name = "monitor"
  case class DownloadLog(msg: String)
}

class DownloadMonitor extends Actor with ActorLogging {
  override def receive: Receive = {
    case DownloadLog(msg) ⇒
      log.info("# " + msg + s" [${sender.path}]")
  }
}
