package org.example.bc_maps_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@SpringBootApplication
public class BcMapsSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BcMapsSystemApplication.class, args);
    }

}
