package io.github.lcaohoanq.processor;

import io.github.lcaohoanq.annotations.BrowserLauncher;
import io.github.lcaohoanq.core.JavaBrowserLauncher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

public class BrowserLauncherProcessor implements ApplicationRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        // System.out.println("BrowserLauncherProcessor started - analyzing application for @BrowserLauncher annotation");
        
        // Get main class from Spring Boot application context
        String mainClassName = null;
        try {
            // Look for the main application class in the context
            // The main class is typically the one with @SpringBootApplication
            String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
            for (String beanName : beanNames) {
                Object bean = applicationContext.getBean(beanName);
                Class<?> beanClass = bean.getClass();
                
                // Handle CGLIB proxies - get the original class
                if (beanClass.getName().contains("$$")) {
                    // This is a CGLIB proxy, get the superclass
                    beanClass = beanClass.getSuperclass();
                }
                
                // Check if this is likely the main application class
                if (beanClass.getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class) != null) {
                    mainClassName = beanClass.getName();
                    break;
                }
            }
        } catch (Exception e) {
            // System.out.println("Could not get main class from Spring context, trying stack trace approach: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Fallback to stack trace approach if Spring context approach fails
        if (mainClassName == null) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            mainClassName = Arrays.stream(stackTrace)
                .filter(element -> "main".equals(element.getMethodName()))
                .findFirst()
                .map(StackTraceElement::getClassName)
                .orElse(null);
        }

        // System.out.println("Main class detected: " + mainClassName);
        
        if (mainClassName == null) {
            System.out.println("Could not find main class for browser launcher");
            return;
        }

        try {
            Class<?> mainClass = Class.forName(mainClassName);
            BrowserLauncher annotation = AnnotationUtils.findAnnotation(mainClass, BrowserLauncher.class);

            // System.out.println("Annotation found: " + (annotation != null));
            
            if (annotation != null) {
                String[] activeProfiles = System.getProperty("spring.profiles.active", "")
                    .split(",");
                
                // System.out.println("Active profiles: " + Arrays.toString(activeProfiles));
                // System.out.println("Excluded profiles: " + Arrays.toString(annotation.excludeProfiles()));

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
                        System.err.println("No URLs specified in @BrowserLauncher annotation");
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
                    
                    // System.out.println("Browser launcher executed for " + urls.length + " URL(s)");
                } else {
                    // System.out.println("Skipping browser launch due to profile exclusion: " + Arrays.toString(activeProfiles));
                }
            } else {
                // System.out.println("No @BrowserLauncher annotation found on main class");
            }
        } catch (Exception e) {
            System.err.println("Error in browser launcher processor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
