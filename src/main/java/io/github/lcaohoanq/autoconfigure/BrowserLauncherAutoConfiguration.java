package io.github.lcaohoanq.autoconfigure;

import io.github.lcaohoanq.processor.BrowserLauncherProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for Browser Launcher library.
 * This ensures the BrowserLauncherProcessor is automatically registered
 * when the library is on the classpath.
 */
@Configuration
@ConditionalOnClass(BrowserLauncherProcessor.class)
public class BrowserLauncherAutoConfiguration {

    @Bean
    public BrowserLauncherProcessor browserLauncherProcessor() {
        return new BrowserLauncherProcessor();
    }
}
