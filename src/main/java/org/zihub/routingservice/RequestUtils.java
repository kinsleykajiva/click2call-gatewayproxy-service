package org.zihub.routingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.context.RequestContext;
import org.zihub.routingservice.pojos.ValidateAuthReq;
import okhttp3.*;

public class RequestUtils {


   static final String  BASE_URL = "http://localhost:8050/";

    private static  OkHttpClient client = new OkHttpClient();

    public static ValidateAuthReq validateReq(final String jwt)  {
        RequestBody formBody = new FormBody.Builder()
                .add("jwt", jwt)
                .build();
        Request request = new Request.Builder()
                .url(BASE_URL + "auth-service/users/validate-access" )
                .post(formBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String res = response.body().string()  ;

                ObjectMapper om = new ObjectMapper();

                return om.readValue(res, ValidateAuthReq.class);
            }

            return  null;
        }catch (Exception e) {
            System.out.println(e.getMessage()); ;
            return  null;
        }
    }

    /**
     * Reports an error message given a response body and code.
     *
     * @param body
     * @param code
     */
    private void setFailedRequest(String body, int code) {
        // log.debug("Reporting error ({}): {}", code, body);
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setResponseStatusCode(code);
        if (ctx.getResponseBody() == null) {
            ctx.setResponseBody(body);
            ctx.setSendZuulResponse(false);
        }
    }

}
