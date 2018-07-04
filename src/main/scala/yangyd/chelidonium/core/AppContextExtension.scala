package yangyd.chelidonium.core

import akka.actor.{Actor, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import org.springframework.context.ApplicationContext

trait AppContextAware {
  _: Actor => // self type (Cake pattern), restrict the type to which this trait can apply
  def appContext: ApplicationContext = AppContextExtension(context.system).applicationContext
}

class AppContextExtensionImpl extends Extension {
  @volatile var applicationContext: ApplicationContext = _
}

object AppContextExtension extends ExtensionId[AppContextExtensionImpl] with ExtensionIdProvider {
  override def lookup(): ExtensionId[_ <: Extension] = AppContextExtension
  override def createExtension(system: ExtendedActorSystem): AppContextExtensionImpl = new AppContextExtensionImpl
}
