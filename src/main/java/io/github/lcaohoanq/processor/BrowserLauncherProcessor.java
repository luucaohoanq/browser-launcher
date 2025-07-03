package io.github.lcaohoanq.processor;


import io.github.lcaohoanq.annotations.BrowserLauncher;
import io.github.lcaohoanq.core.JavaBrowserLauncher;
import java.util.Arrays;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
public class BrowserLauncherProcessor implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        // Lấy main class (nơi chứa annotation)
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String mainClassName = Arrays.stream(stackTrace)
            .filter(e -> "main".equals(e.getMethodName()))
            .findFirst()
            .map(StackTraceElement::getClassName)
            .orElse(null);

        if (mainClassName == null) {
            return;
        }

        try {
            Class<?> mainClass = Class.forName(mainClassName);
            BrowserLauncher annotation = AnnotationUtils.findAnnotation(mainClass,
                                                                        BrowserLauncher.class);

            if (annotation != null) {
                String[] activeProfiles = System.getProperty("spring.profiles.active", "")
                    .split(",");

                boolean excluded = Arrays.stream(annotation.excludeProfiles())
                    .anyMatch(p -> Arrays.asList(activeProfiles).contains(p));

                if (!excluded) {
                    String healthCheck = annotation.healthCheck();
                    String url = annotation.url();

                    if (healthCheck.isEmpty()) {
                        JavaBrowserLauncher.openHomePage(url);
                    } else {
                        if (annotation.async()) {
                            JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(healthCheck, url);
                        } else {
                            JavaBrowserLauncher.doHealthCheckThenOpenHomePage(healthCheck, url);
                        }
                    }
                } else {
                    System.out.println("Skipping browser launch due to profile exclusion.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
