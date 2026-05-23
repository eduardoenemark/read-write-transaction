//package br.com.eduardoenemark.rwt.app.server;
//
//import br.com.eduardoenemark.rwt.app.server.config.TestDataSourceConfiguration;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
//import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
//import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//import org.springframework.web.client.RestTemplate;
//
//@SpringBootApplication(
//    exclude = {
//        DataSourceAutoConfiguration.class,
//        DataSourceTransactionManagerAutoConfiguration.class,
//        HibernateJpaAutoConfiguration.class,
//        JtaAutoConfiguration.class
//    }
//)
//@Import(TestDataSourceConfiguration.class)
//@EnableTransactionManagement
//public class TestAppServerApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(TestAppServerApplication.class, args);
//    }
//
//    @Bean
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }
//}
