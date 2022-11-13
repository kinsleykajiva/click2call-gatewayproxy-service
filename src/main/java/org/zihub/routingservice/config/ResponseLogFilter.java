package org.zihub.routingservice.config;

import com.google.common.io.CharStreams;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;


@Component
public class ResponseLogFilter extends ZuulFilter {


    private static Logger log = LoggerFactory.getLogger(ResponseLogFilter.class);

    @Override
    public String filterType() {
        return POST_TYPE;
    }


    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {

        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();

        try (final InputStream responseDataStream = context.getResponseDataStream()) {

            if (responseDataStream == null) {
                log.info("BODY: {}", "");
                return null;
            }


            String responseData = CharStreams.toString(new InputStreamReader(responseDataStream, "UTF-8"));
            log.info("BODY: {}", responseData);

            log.info(String.format("%s request to %s  ,headers %s", request.getMethod(), request.getRequestURL().toString(), request.getHeaderNames().toString()));

            context.setResponseBody(responseData);

        } catch (Exception e) {
            throw new ZuulException(e, INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }

        return null;
    }

}