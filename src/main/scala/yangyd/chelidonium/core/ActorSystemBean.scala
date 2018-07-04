package yangyd.chelidonium.core

import akka.actor.{ActorRef, ActorSystem, Inbox, Props}
import javax.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class ActorSystemBean @Autowired()(applicationContext: ApplicationContext) {
  private val system = ActorSystem("chelidonium")
  AppContextExtension(system).applicationContext = applicationContext

  @PreDestroy
  def shutdown(): Unit = system.terminate()

  def createInbox(): Inbox = Inbox.create(system)

  def actorOf(props: Props, name: String): ActorRef = system.actorOf(props, name)
}
