package org.zihub.routingservice.config;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zihub.routingservice.RequestUtils;
import org.zihub.routingservice.dbaccess.GeneralLog;
import org.zihub.routingservice.repositories.GeneralLogRepository;
import org.zihub.routingservice.services.OTPService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.MalformedURLException;
import java.net.URL;

import static org.zihub.routingservice.utils.Utils.tokenDecoder;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
@Component @Slf4j
public class PreFilter extends ZuulFilter {
    @Value("${app.secrete}")
    private String SecreteKey;

    @Autowired
    public GeneralLogRepository generalLogRepository;



    @Override
    public Object run() throws ZuulException {
        log.error("going through pre filter");
        String ipAddress = "" ;
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        HttpServletResponse response = ctx.getResponse();
        String requestUrl = request.getRequestURL().toString();
        log.error(String.format("requestUrl::::%s", requestUrl));

        var generalLog = new GeneralLog();

        if (request.getHeader("Authorization") != null) {
            try {

                var decoded=  tokenDecoder(request,SecreteKey);
                int userId = decoded.getUserId();
                int role = decoded.getRole();
                int companyId = decoded.getCompanyId();
                String fullName = decoded.getFullName();

                ctx.addZuulRequestHeader("userId", String.valueOf(userId));
                ctx.addZuulRequestHeader("role", String.valueOf(role));
                ctx.addZuulRequestHeader("companyId", String.valueOf(companyId));
                ctx.addZuulRequestHeader("fullName", String.valueOf(fullName));
                generalLog.setUserId(userId);
                ipAddress =  request.getHeader("X-FORWARDED-FOR");
                if (ipAddress == null) {
                    ipAddress = request.getRemoteAddr();
                }


            }catch (Exception e){
                log.error("Error on Decoding" , e);
                ctx.addZuulRequestHeader("reject", "yes");
                response.setContentType(APPLICATION_JSON_VALUE);
                response.setStatus(FORBIDDEN.value());
            }
        } else {
            // let's test if it's accessing secured routes
            try {
                URL url1 = new URL(requestUrl);

                if (url1.getPath().contains("/api/v1/secured/") || url1.getPath().contains("/api/v1/users/secured")  ) {

                    log.error("accessing secured without rights ot Auth token" );
                    ctx.addZuulRequestHeader("reject", "yes");

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            ipAddress =  request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
        }
        log.error(String.format("%s request || to %s  ,headers %s", request.getMethod(), request.getRequestURL().toString(), request.getHeaderNames().toString()));


        generalLog.setIpAddress(ipAddress);

        generalLog.setUrl( request.getRequestURL().toString());

        generalLogRepository.save(generalLog);

        return null;
    }


    @Override
    public boolean shouldFilter() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public int filterOrder() {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public String filterType() {
        // TODO Auto-generated method stub
        return "pre";
    }

}
