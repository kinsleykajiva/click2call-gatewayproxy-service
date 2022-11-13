package org.zihub.routingservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.zihub.routingservice.utils.Utils;

@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
@Slf4j
public class RoutingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoutingServiceApplication.class, args);
	}
	@Bean
	ApplicationRunner applicationRunner(Environment environment) {
		return args -> {
			Utils.BaseUrl = environment.getProperty("app.baseurl");
			Utils.BaseFilesUrl = environment.getProperty("app.files.baseurl");
			Utils.WEBSITE_BaseUrl = environment.getProperty("app.website-baseurl");
			log.error("XX-Setup ENV <:::::::> " + environment.getProperty("message-from-application-properties"));
			log.error("XX-Setup ENV <:::::app.baseurl::::> " + environment.getProperty("app.baseurl"));
			log.error("XX-Setup ENV <:::::app.baseurl::::> " + Utils.BaseUrl);
			log.error("XX-Setup ENV <:::::app.BaseFilesUrl::::> " + Utils.BaseFilesUrl);
		};
	}
}
