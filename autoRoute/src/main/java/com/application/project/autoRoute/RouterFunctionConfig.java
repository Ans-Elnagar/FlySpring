package com.application.project.autoRoute;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.reactive.function.server.RouterFunctions.Builder;

import com.application.project.annotation.PathVariableAnnotation;
import com.application.project.entity.PersonEntity;
import com.application.project.repository.*;

import jakarta.annotation.PostConstruct;

import org.springframework.web.reactive.function.server.*;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@EnableAutoConfiguration
@EntityScan(basePackageClasses=PersonEntity.class)
@EnableJpaRepositories(basePackageClasses = PersonRepo.class)
@ComponentScan(basePackageClasses = PersonService.class)
@EnableTransactionManagement
public class RouterFunctionConfig {


    @Autowired
    private  AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Bean
    public RouterFunction<ServerResponse> routerFunction(){
        ClassLoader classLoader = getClass().getClassLoader();

        URL sring =classLoader.getResource("");
        String path =sring.getPath();
        String srcPth ="com/application/project/myapi";

        File[] files = new File(path + srcPth).listFiles();

        System.out.println("FILE in Sample "+files.toString());

        return RouterFunctions.route()
        .path("/route",builder->{
            registerRouter(files, builder,"");
        })
        .build();
    }

    private String getFileName(String filename){
        return FilenameUtils.removeExtension(filename);
    }

    private void registerRouter(File[] files, Builder builder, String directory){
        // Get non-empty folders
        List<File> currentFolders = Stream.of(files)
            .filter(file -> file.isDirectory() && file.listFiles().length > 0 )
            .collect(Collectors.toList());

        for(File folder: currentFolders){
            String subDirectory = directory + "/" + folder.getName();
            System.out.println("Directory for myapi: " + folder.getName());
            System.out.println("directory: " + subDirectory);
            registerRouter(folder.listFiles(), builder, subDirectory);
        }

        // Get the files in the current folder
        List<File> currentFiles = Stream.of(files)
            .filter(File::isFile)
            .collect(Collectors.toList());

        for (File file : currentFiles) {
            try {
                registerFile(directory, builder, file);
            }catch (Exception e) { 
                log.info("==========================>Exception", e.getMessage());
                e.printStackTrace();
            }
        }

    }

    private void registerFile(String directory, Builder builder, File file) throws Exception{
        String fileName = getFileName(file.getName());
        System.out.println("Filename without extention: " + fileName);
        Class<?> clazz = Class.forName("com.application.project.myapi"
            + directory.replace("/", ".") + "." + fileName);
        
        List<Method> methods = Stream.of(clazz.getDeclaredMethods())
            .filter(method -> method.getName().toUpperCase().contains("FLY") 
                        && !method.getName().contains("$"))
            .collect(Collectors.toList());
            
        String endPoint = directory + "/" + fileName.replace("Fly","");
        for(Method method: methods){
            String pathVariable = "";
            System.out.println("Methods in the class: " + method.getName());
            Method classMethod = clazz.getDeclaredMethod(method.getName(), ArkRequest.class);
            if(classMethod.isAnnotationPresent(PathVariableAnnotation.class)){
                PathVariableAnnotation annotation = classMethod
                    .getAnnotation(PathVariableAnnotation.class);
                for(String p:annotation.name()){
                    log.info(fileName+" pathVariables:{}", p);
                    pathVariable += "/" + p;
                }
            }

            String apiType =  method.getName().toUpperCase().replace("FLY","");
            log.info("APItype:{}",apiType);
            String path = endPoint + pathVariable;
            switch(apiType){
                case "GET" -> builder.GET(path, req -> 
                    invokeMethod(apiType, req, clazz, classMethod));
                case "POST" -> builder.POST(path, req ->
                    invokeMethod(apiType, req, clazz, classMethod));
                case "PATCH" -> builder.PATCH(path, req ->
                    invokeMethod(apiType, req, clazz, classMethod));
                case "PUT" -> builder.PUT(path, req ->
                    invokeMethod(apiType, req, clazz, classMethod));
            }
        }
    }

    private Mono<ServerResponse> invokeMethod(String apiType, ServerRequest req, Class<?> clazz, Method classMethod){
        try{
            Object instance =clazz.getDeclaredConstructor().newInstance();
            autowireCapableBeanFactory.autowireBean(instance);
            return (Mono<ServerResponse>) classMethod.invoke(instance, new ArkRequest(req));
        }catch(Exception e){
            e.printStackTrace();
            return ServerResponse.ok().body(Mono.just("Exception "+apiType),String.class);
        }
    }

    // @PostConstruct
    // public void init(){
    //     // autowireCapableBeanFactory.initializeBean(JpaRepository.class, "personRepo");
    // //    autowireCapableBeanFactory.autowire(PersonRepo.class, 0, false);
    // // autowireCapableBeanFactory.createBean(PersonRepo.class, 1, true);
    //    autowireCapableBeanFactory.initializeBean(PersonService.class, "personService");
    //    autowireCapableBeanFactory.autowire(PersonService.class, 1, false);
    //     // wire stuff here
    // }
    
}
