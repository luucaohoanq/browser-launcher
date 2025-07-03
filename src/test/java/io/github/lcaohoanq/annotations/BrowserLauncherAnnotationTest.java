package io.github.lcaohoanq.annotations;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Test;

class BrowserLauncherAnnotationTest {

    @Test
    void annotation_shouldHaveCorrectRetentionPolicy() throws Exception {
        Retention retention = BrowserLauncher.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void annotation_shouldHaveCorrectTarget() throws Exception {
        Target target = BrowserLauncher.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertEquals(1, target.value().length);
        assertEquals(ElementType.TYPE, target.value()[0]);
    }

    @Test
    void annotation_shouldHaveCorrectDefaultValues() throws Exception {
        // Test default values by creating a test class with minimal annotation
        BrowserLauncher annotation = TestClassWithMinimalAnnotation.class
            .getAnnotation(BrowserLauncher.class);
        
        assertNotNull(annotation);
        assertEquals("https://example.com", annotation.url());
        assertEquals("", annotation.healthCheck());
        assertArrayEquals(new String[]{"docker", "test", "zimaos"}, annotation.excludeProfiles());
        assertFalse(annotation.async());
    }

    @Test
    void annotation_shouldAllowCustomValues() throws Exception {
        BrowserLauncher annotation = TestClassWithCustomAnnotation.class
            .getAnnotation(BrowserLauncher.class);
        
        assertNotNull(annotation);
        assertEquals("https://custom.com", annotation.url());
        assertEquals("http://localhost:8080/health", annotation.healthCheck());
        assertArrayEquals(new String[]{"prod", "staging"}, annotation.excludeProfiles());
        assertTrue(annotation.async());
    }

    @Test
    void annotation_shouldSupportEmptyExcludeProfiles() throws Exception {
        BrowserLauncher annotation = TestClassWithEmptyExcludes.class
            .getAnnotation(BrowserLauncher.class);
        
        assertNotNull(annotation);
        assertEquals("https://example.com", annotation.url());
        assertEquals("", annotation.healthCheck());
        assertArrayEquals(new String[]{}, annotation.excludeProfiles());
        assertFalse(annotation.async());
    }

    // Test classes for annotation testing
    @BrowserLauncher(url = "https://example.com")
    static class TestClassWithMinimalAnnotation {
    }

    @BrowserLauncher(
        url = "https://custom.com",
        healthCheck = "http://localhost:8080/health",
        excludeProfiles = {"prod", "staging"},
        async = true
    )
    static class TestClassWithCustomAnnotation {
    }

    @BrowserLauncher(
        url = "https://example.com",
        excludeProfiles = {}
    )
    static class TestClassWithEmptyExcludes {
    }
}
