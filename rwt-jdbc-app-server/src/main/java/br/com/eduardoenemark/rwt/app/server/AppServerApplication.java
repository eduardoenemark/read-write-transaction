package br.com.eduardoenemark.rwt.app.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(
        scanBasePackages = {
                "br.com.eduardoenemark.rwt.app.server.config",
                "br.com.eduardoenemark.rwt.core.config.routing",
                "br.com.eduardoenemark.rwt.app.server.repository",
                "br.com.eduardoenemark.rwt.app.server.service",
                "br.com.eduardoenemark.rwt.app.server.resource"},
        exclude = {DataSourceAutoConfiguration.class})
public class AppServerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(AppServerApplication.class, args);
    }
}
