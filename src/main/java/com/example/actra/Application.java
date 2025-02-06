package com.example.actra;

import com.example.actra.service.CsvService;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * The entry point of the Spring Boot application.
 * <p>
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@SpringBootApplication
@Theme(value = "actra-webapp")
@Push // Needed for async updates
@EnableAsync
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {

        ConfigurableApplicationContext appContext = SpringApplication.run(Application.class, args);

        CsvService service = appContext.getBean(CsvService.class);


    }
}
