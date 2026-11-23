package com.microbms.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication public class AttendanceApplication {
 public static void main(String[] args){SpringApplication.run(AttendanceApplication.class,args);}
 @org.springframework.context.annotation.Bean java.time.Clock utcClock(){return java.time.Clock.systemUTC();}
}
