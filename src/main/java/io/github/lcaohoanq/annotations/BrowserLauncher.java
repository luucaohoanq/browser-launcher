package io.github.lcaohoanq.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BrowserLauncher {
    String url();
    String healthCheck() default "";
    String[] excludeProfiles() default {"docker", "test", "zimaos"};
    boolean async() default false;
}

