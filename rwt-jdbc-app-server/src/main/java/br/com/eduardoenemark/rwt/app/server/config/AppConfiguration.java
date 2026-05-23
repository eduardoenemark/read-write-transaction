package br.com.eduardoenemark.rwt.app.server.config;

import br.com.eduardoenemark.rwt.app.server.repository.ProductRepository;
import br.com.eduardoenemark.rwt.app.server.service.ProductService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.val;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;

@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@EnableTransactionManagement
public class AppConfiguration {

    public static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("br.com.eduardoenemark.rwt.app");

    @Bean
    public OpenAPI openApi(PropsConfig.SwaggerConfig swaggerConfig,
                           PropsConfig.SwaggerConfig.ContactConfig contactConfig) {
        return new OpenAPI()
                .info(new Info()
                        .title(swaggerConfig.getTitle())
                        .description(swaggerConfig.getDescription())
                        .version(swaggerConfig.getVersion())
                        .contact(new Contact()
                                .name(contactConfig.getName())
                                .email(contactConfig.getEmail())));
    }

    @Bean
    public RestTemplate restTemplate() {
        class CustomErrorHandler implements ResponseErrorHandler {

            private final ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();

            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return errorHandler.hasError(response);
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                // Handle the error here
                LOGGER.debug("HTTP Status: {}", response.getStatusCode());
            }
        }
        val restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new CustomErrorHandler());
        return restTemplate;
    }

    @Bean
    public ProductService productService(ProductRepository repository) {
        return new ProductService(repository);
    }
}
