package com.application.project.myapiTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.application.project.entity.PersonEntity;
import com.application.project.model.User;
import com.application.project.myapi.FlysaveUser;
import com.application.project.repository.PersonRepo;
import com.application.project.repository.PersonService;
import com.flyspring.autoroute.FlyRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

// @ExtendWith(MockitoExtension.class)
@WebFluxTest(FlysaveUser.class)
@ContextConfiguration(classes = {
    FlysaveUser.class,
    PersonService.class
  })
public class FlysaveUserTest {

    @MockBean
    private PersonService userServices;
    
    @MockBean
    private PersonRepo personRepo;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private FlysaveUser flysaveUser;


    @Test
    @WithMockUser(username = "lezter",password = "12345")
    public void FlySaveUserTest() throws Exception{

        PersonEntity person = new PersonEntity();
       person.setName("Lezter Hernandez");
       person.setEmail("lezterwithgod@gmail.com");
        MockServerRequest ms =  MockServerRequest.builder().body(Mono.just(person));
        FlyRequest request = new FlyRequest(ms);
            
        Mono<ServerResponse> response = flysaveUser.flypost(request);

        StepVerifier.create(response).equals(null);
        
    }
    
}
