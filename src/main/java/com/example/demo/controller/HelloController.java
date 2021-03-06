package com.example.demo.controller;

import com.example.demo.service.IStudentService;
import com.example.demo.thread.AbstractTracerRunnable;
import com.example.demo.thread.TracerThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping
public class HelloController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    TracerThreadPoolExecutor threadPoolExecutor = TracerThreadPoolExecutor.newWithCurrentMdc(1, 1, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(20));

    TracerThreadPoolExecutor threadPoolExecutor1 = TracerThreadPoolExecutor.newWithInheritedMdc(1, 1, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(20));

    @Value(value = "${envName}")
    private String envName;

    @Resource
    private IStudentService studentService;

    @GetMapping(value = "/envName")
    private String envName() {
        return envName;
    }


    @GetMapping(value = "/hello")
    public String hello() {
        LOGGER.info("my first spring-boot-docker application");

        threadPoolExecutor.execute(()-> {
            LOGGER.info("threadPoolExecutor executor");
        });

        threadPoolExecutor1.execute(()-> {
            LOGGER.info("threadPoolExecutor1 executor");
        });

        studentService.save();

        new Thread(new AbstractTracerRunnable() {
            @Override
            public void runWithMDC() {
                LOGGER.info("MDCRunnable execute");
            }
        }).start();

        LOGGER.info("controller over");
        return "my first spring-boot-docker application";
    }

    @GetMapping(value = "/list")
    public List<String> list() throws Exception {
        LOGGER.info("list method.thread id = {}", Thread.currentThread().getId());
        return studentService.findAll().get();
    }


    @GetMapping(value = "/thread")
    public String thread() {
        threadPoolTaskExecutor.execute(new AbstractTracerRunnable() {
            @Override
            public void runWithMDC() {
                LOGGER.info("thread method.thread id = {}", Thread.currentThread().getId());
                studentService.log();
            }
        });
        return "ok";
    }
}
