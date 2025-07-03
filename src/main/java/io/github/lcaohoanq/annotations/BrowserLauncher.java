package io.github.lcaohoanq.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BrowserLauncher {
    /**
     * Single URL to open (convenience method for single URL)
     * @return the URL to open
     */
    String value() default "";
    
    /**
     * Single URL to open (alternative to value())
     * @return the URL to open
     */
    String url() default "";
    
    /**
     * Multiple URLs to open
     * @return array of URLs to open
     */
    String[] urls() default {};
    
    /**
     * Health check endpoint to verify before opening browser
     * @return the health check URL
     */
    String healthCheckEndpoint() default "";
    
    /**
     * Spring profiles where browser should NOT open
     * @return array of profile names to exclude
     */
    String[] excludeProfiles() default {"docker", "test", "zimaos"};
    
    /**
     * Whether to open browser asynchronously
     * @return true for async operation
     */
    boolean async() default false;
}

