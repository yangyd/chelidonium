package yangyd.chelidonium.aliyun.actor

import java.io.File

import akka.actor.{Actor, ActorLogging, Props}
import yangyd.chelidonium.aliyun.AliyunBucket
import yangyd.chelidonium.aliyun.actor.FileDownloader.FileTask

object FileDownloader {
  case class FileTask(fileKey: String, file: File, offset: Long)
  def props(task: FileTask, bucket: AliyunBucket) = Props(classOf[FileDownloader], task, bucket)
}

class FileDownloader(task: FileTask, bucket: AliyunBucket) extends Actor with ActorLogging {
  override def receive: Receive = ???
}
