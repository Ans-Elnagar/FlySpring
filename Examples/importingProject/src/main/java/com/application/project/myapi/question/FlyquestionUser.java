package com.application.project.myapi.question;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.flyspring.autoroute.FlyRequest;
import com.flyspring.autoroute.annotations.PathVariableAnnotation;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class FlyquestionUser {

    @PathVariableAnnotation(name ="{msg}")
    public Mono<ServerResponse> flyget(FlyRequest request) throws Exception{
        try {
            log.info("get question API");
            String msg = request.getPathVariable("msg");
            log.info("msg: " + msg);
            return ServerResponse.ok().body(Mono.just(msg),String.class);
           
            
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.ok().body(Mono.just("Catch an exception"),String.class);
        }
    }
    
}
