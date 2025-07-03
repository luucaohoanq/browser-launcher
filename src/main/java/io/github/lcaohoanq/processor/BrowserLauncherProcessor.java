package io.github.lcaohoanq.processor;

import io.github.lcaohoanq.annotations.BrowserLauncher;
import io.github.lcaohoanq.core.JavaBrowserLauncher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
public class BrowserLauncherProcessor implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        // Get main class (where annotation is located)
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String mainClassName = Arrays.stream(stackTrace)
            .filter(e -> "main".equals(e.getMethodName()))
            .findFirst()
            .map(StackTraceElement::getClassName)
            .orElse(null);

        if (mainClassName == null) {
            System.out.println("Could not find main class for browser launcher");
            return;
        }

        try {
            Class<?> mainClass = Class.forName(mainClassName);
            BrowserLauncher annotation = AnnotationUtils.findAnnotation(mainClass, BrowserLauncher.class);

            if (annotation != null) {
                String[] activeProfiles = System.getProperty("spring.profiles.active", "")
                    .split(",");

                boolean excluded = Arrays.stream(annotation.excludeProfiles())
                    .anyMatch(p -> Arrays.asList(activeProfiles).contains(p));

                if (!excluded) {
                    // Smart URL extraction: prioritize value() > url() > urls()
                    List<String> urlList = new ArrayList<>();
                    
                    // Check value() first (primary single URL)
                    if (!annotation.value().isEmpty()) {
                        urlList.add(annotation.value());
                    }
                    // Check url() second (alternative single URL)
                    else if (!annotation.url().isEmpty()) {
                        urlList.add(annotation.url());
                    }
                    // Check urls() last (multiple URLs)
                    else if (annotation.urls().length > 0) {
                        urlList.addAll(Arrays.asList(annotation.urls()));
                    }
                    
                    if (urlList.isEmpty()) {
                        System.out.println("No URLs specified in @BrowserLauncher annotation");
                        return;
                    }
                    
                    String[] urls = urlList.toArray(new String[0]);
                    String healthCheckEndpoint = annotation.healthCheckEndpoint();

                    // Convert array to list for JavaBrowserLauncher
                    List<String> urlsAsList = Arrays.asList(urls);

                    if (healthCheckEndpoint.isEmpty()) {
                        // No health check, open URLs directly
                        if (urls.length == 1) {
                            JavaBrowserLauncher.openHomePage(urls[0]);
                        } else {
                            JavaBrowserLauncher.openHomePage(urlsAsList);
                        }
                    } else {
                        // With health check
                        if (annotation.async()) {
                            // For multiple URLs with health check async, we'll open them one by one
                            for (String url : urls) {
                                JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(healthCheckEndpoint, url);
                            }
                        } else {
                            // For multiple URLs with health check sync, we'll open them one by one
                            for (String url : urls) {
                                JavaBrowserLauncher.doHealthCheckThenOpenHomePage(healthCheckEndpoint, url);
                            }
                        }
                    }
                    
                    System.out.println("Browser launcher executed for " + urls.length + " URL(s)");
                } else {
                    System.out.println("Skipping browser launch due to profile exclusion: " + 
                        Arrays.toString(activeProfiles));
                }
            } else {
                System.out.println("No @BrowserLauncher annotation found on main class");
            }
        } catch (Exception e) {
            System.err.println("Error in browser launcher processor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
