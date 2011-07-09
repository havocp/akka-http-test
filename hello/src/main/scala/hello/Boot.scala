package hello

import akka.actor._
import akka.actor.Actor._
import akka.config._
import akka.config.Supervision._
import akka.http._

class Boot {
    val factory = SupervisorFactory(
        SupervisorConfig(
            OneForOneStrategy(List(classOf[Exception]), 3, 100),
            Supervise(
                actorOf[RootEndpoint],
                Permanent) ::
                Supervise(
                    actorOf[HelloService],
                    Permanent)
                    :: Nil))
    factory.newInstance.start
}
