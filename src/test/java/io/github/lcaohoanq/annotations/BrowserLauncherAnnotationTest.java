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
        assertEquals("", annotation.value());
        assertEquals("https://example.com", annotation.url());
        assertArrayEquals(new String[]{}, annotation.urls());
        assertEquals("", annotation.healthCheckEndpoint());
        assertArrayEquals(new String[]{"docker", "test", "zimaos"}, annotation.excludeProfiles());
        assertFalse(annotation.async());
    }

    @Test
    void annotation_shouldAllowCustomValues() throws Exception {
        BrowserLauncher annotation = TestClassWithCustomAnnotation.class
            .getAnnotation(BrowserLauncher.class);
        
        assertNotNull(annotation);
        assertEquals("", annotation.value());
        assertEquals("https://custom.com", annotation.url());
        assertArrayEquals(new String[]{}, annotation.urls());
        assertEquals("http://localhost:8080/health", annotation.healthCheckEndpoint());
        assertArrayEquals(new String[]{"prod", "staging"}, annotation.excludeProfiles());
        assertTrue(annotation.async());
    }

    @Test
    void annotation_shouldSupportValueParameter() throws Exception {
        BrowserLauncher annotation = TestClassWithValueParameter.class
            .getAnnotation(BrowserLauncher.class);
        
        assertNotNull(annotation);
        assertEquals("https://value.com", annotation.value());
        assertEquals("", annotation.url());
        assertArrayEquals(new String[]{}, annotation.urls());
    }

    @Test
    void annotation_shouldSupportUrlsParameter() throws Exception {
        BrowserLauncher annotation = TestClassWithUrlsParameter.class
            .getAnnotation(BrowserLauncher.class);
        
        assertNotNull(annotation);
        assertEquals("", annotation.value());
        assertEquals("", annotation.url());
        assertArrayEquals(new String[]{"https://url1.com", "https://url2.com", "https://url3.com"}, annotation.urls());
    }

    @Test
    void annotation_shouldSupportEmptyExcludeProfiles() throws Exception {
        BrowserLauncher annotation = TestClassWithEmptyExcludes.class
            .getAnnotation(BrowserLauncher.class);
        
        assertNotNull(annotation);
        assertEquals("", annotation.value());
        assertEquals("https://example.com", annotation.url());
        assertArrayEquals(new String[]{}, annotation.urls());
        assertEquals("", annotation.healthCheckEndpoint());
        assertArrayEquals(new String[]{}, annotation.excludeProfiles());
        assertFalse(annotation.async());
    }

    // Test classes for annotation testing
    @BrowserLauncher(url = "https://example.com")
    static class TestClassWithMinimalAnnotation {
    }

    @BrowserLauncher(
        url = "https://custom.com",
        healthCheckEndpoint = "http://localhost:8080/health",
        excludeProfiles = {"prod", "staging"},
        async = true
    )
    static class TestClassWithCustomAnnotation {
    }

    @BrowserLauncher(value = "https://value.com")
    static class TestClassWithValueParameter {
    }

    @BrowserLauncher(urls = {"https://url1.com", "https://url2.com", "https://url3.com"})
    static class TestClassWithUrlsParameter {
    }

    @BrowserLauncher(
        url = "https://example.com",
        excludeProfiles = {}
    )
    static class TestClassWithEmptyExcludes {
    }
}
