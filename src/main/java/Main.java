import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import route.HttpServer;

public class Main extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(Main.class.getName());
    }

    @Override
    public void start(Future<Void> startFuture) {
        //发布httpServer
        vertx.deployVerticle(HttpServer.class.getName());

    }
}
