package org.zihub.routingservice;


import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {




    @GetMapping("/info/status")
    public String infoStatus() {
        return "-- OK --";
    }



    @RequestMapping(value = "/info" )
    public String info(){
        return
                new JSONObject ()
                .put( "success",true)
                .put( "message" , "Auth Parent Service")
                .toString();
    }
    @RequestMapping(value = "/" )
    public String index(){
        return
                new JSONObject ()
                .put( "success",true)
                .put( "message" , "routing service is up and running @2")
                .toString();
    }
}
