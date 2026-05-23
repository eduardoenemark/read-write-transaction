package br.com.eduardoenemark.rwt.app.server.resource;

import br.com.eduardoenemark.rwt.app.server.AppServerApplication;
import br.com.eduardoenemark.rwt.app.server.config.BaseTestConfiguration;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AppServerApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConectivityResourceTest extends BaseTestConfiguration {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void testPingEndpoint() throws Exception {
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong!"));
    }
}