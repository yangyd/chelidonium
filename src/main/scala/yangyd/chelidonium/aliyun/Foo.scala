package yangyd.chelidonium.aliyun

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

object Foo {
  implicit def runnable(fn: ⇒ Unit): Runnable = new Runnable { override def run(): Unit = fn }

  val stpe = Executors.newScheduledThreadPool(5)

//  def main(args: Array[String]): Unit = {
//    println("start")
//    new Foo(stpe).schedule()
//  }
}

class Foo(stpe: ScheduledExecutorService) {
  import Foo._
  def foo(): Unit = {
    println("this is a pen")
    println("I'm a boy")
  }

  def schedule(): Unit = {
    stpe.scheduleAtFixedRate(foo, 5, 5, TimeUnit.SECONDS)
  }
}
