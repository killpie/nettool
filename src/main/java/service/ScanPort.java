package service;

import entity.CheckPort;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import util.JsonParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  端口扫描
 */
@Slf4j
public class ScanPort {

    /* 正则，粗略的匹配下IP还是域名，代码能跑这的，格式什么的都合法 */
    private static final String PATTERN_L2DOMAIN = "\\w*\\.\\w*";
    private static final String PATTERN_IP = "(\\d*\\.){3}\\d*";
    private static final String IP = "ip";
    private static final String DOMAIN = "domain";
    private final String contentType = "content-type";
    private final String applicationJson = "application/json;charset=UTF-8";
    private final static String templateDirectory = "templates/";





    public void scanPort(final RoutingContext context){

        List<CheckPort> checkPortList = new ArrayList<>();
        String param = context.getBodyAsString("UTF-8");
        JsonObject jsonObject = JsonParser.httpCovertJson(param);
        log.info(jsonObject.encodePrettily());


        String domain = jsonObject.getString("domain");
        String flag = checkDomain(domain);
        if (IP.equals(flag)||DOMAIN.equals(flag)){
            String[] ports = jsonObject.getString("port").split(",");

            for (int i = 0; i < ports.length; i++) {
                String port = ports[i];
                CheckPort checkPort = new CheckPort();
                if (port.contains("-")){
                    int start = Integer.parseInt(port.split("-")[0]);
                    int end = Integer.parseInt(port.split("-")[1]);

                    for (int j = start; j <= end; j++) {
                        CheckPort cp = new CheckPort();
                        if (checkPort(j)){
                            cp.setDomain(domain);
                            cp.setPort(j);
                            if (isHostConnectable(domain,j)){
                                cp.setStatus(CheckPort.port_open);
                            }else {
                                cp.setStatus(CheckPort.port_close);
                            }
                            checkPortList.add(cp);
                        }
                    }
                }else {
                    int p = Integer.parseInt(port);
                    if (checkPort(p)){
                        checkPort.setDomain(domain);
                        checkPort.setPort(p);
                        if (isHostConnectable(domain,p)){
                            checkPort.setStatus(CheckPort.port_open);
                        }else {
                            checkPort.setStatus(CheckPort.port_close);
                        }
                        checkPortList.add(checkPort);
                    }
                }
            }
        }

        //添加thymeleaf模板支持
        final ThymeleafTemplateEngine templateEngine = ThymeleafTemplateEngine.create();
        // 定时模板解析器,表示从类加载路径下找模板
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        // 设置后缀为.html文件
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateEngine.getThymeleafTemplateEngine().setTemplateResolver(templateResolver);

        context.put("checkPortList",checkPortList);
        context.put("domain",domain);
        context.put("port",jsonObject.getString("port"));
        templateEngine.render(context,templateDirectory,"index",res->{
            if (res.succeeded()) {
                context.response().putHeader("Content-Type", "text/html").end(res.result());
            } else {
                log.info("响应失败:{}",res.cause());
            }
        });



    }


    public boolean checkPort(int port){
        if (port>=1&&port<=65535){
            return true;
        }else {
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println(isHostConnectable("lovezcy.com",22));
        System.out.println(isHostConnectable("139.199.153.150",22));
    }

    public static boolean isHostConnectable(String host, int port) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port),100);
        } catch (IOException e) {
            log.info("host:{},port:{} 打开失败",host,port);
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                log.info("socket关闭失败");
            }
        }
        return true;
    }


    public static String checkDomain(String param) {
        /* 以IP形式访问时，返回IP */
        Pattern ipPattern = Pattern.compile(PATTERN_IP);
        Matcher matcher = ipPattern.matcher(param);
        if (matcher.find()){
            return "ip";
        }


        Pattern pattern = Pattern.compile(PATTERN_L2DOMAIN);
        matcher = pattern.matcher(param);
        if (matcher.find()){
            return "domain";
        }

        return "null";
    }
}
