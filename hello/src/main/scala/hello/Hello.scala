package hello

import akka.actor._
import akka.http._

class HelloService extends Actor with Endpoint {
    self.dispatcher = Endpoint.Dispatcher;

    def hook(uri : String) = true
    def provide(uri : String) = Actor.actorOf[HelloActor].start

    override def preStart = Actor.registry.actorsFor(classOf[RootEndpoint]).head ! Endpoint.Attach(hook, provide)

    def receive = handleHttpRequest
}

class HelloActor extends Actor {
    def receive = {
        case get : Get => get OK "it works"
        case other : RequestMethod => other NotAllowed "unsupported request"
    }
}
