package service;


import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import util.JsonParser;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Nslookup {

    private static final Map<String,Integer> type;
    private static final Map<Integer,String> reverseType;

    static {
         type = new HashMap<>();
        type.put("A",Type.A);
        type.put("CNAME",Type.CNAME);
        type.put("MX",Type.MX);
        type.put("NS",Type.NS);
        type.put("TXT",Type.TXT);

        reverseType = new HashMap<>();
        reverseType.put(Type.A,"A");
        reverseType.put(Type.CNAME,"CNAME");
        reverseType.put(Type.MX,"MX");
        reverseType.put(Type.NS,"NS");
        reverseType.put(Type.TXT,"TXT");
    }


    public void dnsRecord(final RoutingContext context){

        String param = context.getBodyAsString("UTF-8");
        JsonObject jsonObject = JsonParser.httpCovertJson(param);
        log.info(jsonObject.encodePrettily());

        Map<String,String[]> record = new HashMap<>();

        String domain = jsonObject.getString("domain");

        if (domain!=null&&!"".equals(domain)){
            type.forEach((k,v)->{
                record.put(k,nslookup(domain,v));
            });
        }


        log.info(record.toString());

        //添加thymeleaf模板支持
        final ThymeleafTemplateEngine templateEngine = ThymeleafTemplateEngine.create();
        // 定时模板解析器,表示从类加载路径下找模板
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        // 设置后缀为.html文件
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateEngine.getThymeleafTemplateEngine().setTemplateResolver(templateResolver);

        context.put("record",record);
        context.put("domain",domain);
        templateEngine.render(context,"templates/","nslookup",res->{
            if (res.succeeded()) {
                context.response().putHeader("Content-Type", "text/html").end(res.result());
            } else {
                log.info("响应失败:{}",res.cause());
            }
        });
    }



    private String[] nslookup(String domain, int type){
        Lookup lookup = null;
        //查询域名对应的IP地址
        try {
            lookup = new Lookup(domain,type);
        }catch (TextParseException e){
            log.debug("nslookup domain:{},type:{} 异常",domain,reverseType.get(type));
        }

        lookup.run();

        String[] r = null;
        if (lookup.getResult() != Lookup.SUCCESSFUL){
            log.debug("nslookup domain:{},type:{} 异常",domain,reverseType.get(type));
        }else {
            Record[] answers = lookup.getAnswers();
            r = new String[answers.length];

            for (int i = 0; i < r.length; i++) {

                r[i] = answers[i].toString();
            }
        }

        return r;

    }
}
