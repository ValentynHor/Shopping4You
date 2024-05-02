package at.bovt.catalogue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CatalogueApp {

    public static void main(String[] args) {
        SpringApplication.run(CatalogueApp.class, args);
    }

}
