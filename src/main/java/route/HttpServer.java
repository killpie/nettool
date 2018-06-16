package route;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import service.Nslookup;
import service.ScanPort;

@Slf4j
public class HttpServer extends AbstractVerticle {

    final static String templateDirectory = "templates/";


    @Override
    public void start(Future<Void> startFuture){
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route().handler(CookieHandler.create());
        router.route("/static/*").handler(StaticHandler.create());

        //添加thymeleaf模板支持
        final ThymeleafTemplateEngine templateEngine = ThymeleafTemplateEngine.create();
        // 定时模板解析器,表示从类加载路径下找模板
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        // 设置后缀为.html文件
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateEngine.getThymeleafTemplateEngine().setTemplateResolver(templateResolver);


        router.route("/*").handler(context -> {
            log.info("url: {}", context.request().absoluteURI());
            log.info("param: {}", context.getBodyAsString());
            log.info("StatusMessage: {}", context.response().getStatusMessage());
            log.info("getStatusCode: {}", context.response().getStatusCode());
            log.info("pathParams: {}", context.pathParams());
            log.info("cookies: {}", context.cookies());
            context.next();
        });

        router.route("/").handler(context->{
            templateEngine.render(context,templateDirectory,"index",res->{
                if (res.succeeded()) {
                    context.response().putHeader("Content-Type", "text/html").end(res.result());
                } else {
                    log.info("响应失败:{}",res.cause());
                }

            });
        });


        //端口扫描
        router.post("/scanport").handler(new ScanPort()::scanPort);
        //nslookup
        router.route("/nslookup").handler(new Nslookup()::dnsRecord);


        int port = 12345;
        vertx.createHttpServer().requestHandler(router::accept).listen(port,asy->{
            String flag = "";
            if (asy.succeeded()){
                flag = "成功";
            }else {
                flag = "失败";
            }

            log.info("httpServer 创建{} 端口号为{}", flag, port);
        });
    }


}
