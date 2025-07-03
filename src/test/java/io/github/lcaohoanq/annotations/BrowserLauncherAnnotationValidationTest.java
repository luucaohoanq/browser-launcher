package io.github.lcaohoanq.annotations;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class BrowserLauncherAnnotationValidationTest {

    @Test
    void annotation_shouldBeRuntimeRetention() throws Exception {
        Retention retention = BrowserLauncher.class.getAnnotation(Retention.class);
        assertNotNull(retention, "Annotation should have Retention annotation");
        assertEquals(RetentionPolicy.RUNTIME, retention.value(), 
                    "Annotation should have RUNTIME retention policy");
    }

    @Test
    void annotation_shouldTargetTypeOnly() throws Exception {
        Target target = BrowserLauncher.class.getAnnotation(Target.class);
        assertNotNull(target, "Annotation should have Target annotation");
        assertEquals(1, target.value().length, "Annotation should target only one element type");
        assertEquals(ElementType.TYPE, target.value()[0], 
                    "Annotation should target TYPE only");
    }

    @Test
    void annotation_shouldHaveRequiredMethods() throws NoSuchMethodException {
        // Test that all required methods exist
        Method valueMethod = BrowserLauncher.class.getDeclaredMethod("value");
        Method urlMethod = BrowserLauncher.class.getDeclaredMethod("url");
        Method urlsMethod = BrowserLauncher.class.getDeclaredMethod("urls");
        Method healthCheckEndpointMethod = BrowserLauncher.class.getDeclaredMethod("healthCheckEndpoint");
        Method excludeProfilesMethod = BrowserLauncher.class.getDeclaredMethod("excludeProfiles");
        Method asyncMethod = BrowserLauncher.class.getDeclaredMethod("async");

        assertNotNull(valueMethod, "value() method should exist");
        assertNotNull(urlMethod, "url() method should exist");
        assertNotNull(urlsMethod, "urls() method should exist");
        assertNotNull(healthCheckEndpointMethod, "healthCheckEndpoint() method should exist");
        assertNotNull(excludeProfilesMethod, "excludeProfiles() method should exist");
        assertNotNull(asyncMethod, "async() method should exist");
    }

    @Test
    void annotation_shouldHaveCorrectReturnTypes() throws NoSuchMethodException {
        Method valueMethod = BrowserLauncher.class.getDeclaredMethod("value");
        Method urlMethod = BrowserLauncher.class.getDeclaredMethod("url");
        Method urlsMethod = BrowserLauncher.class.getDeclaredMethod("urls");
        Method healthCheckEndpointMethod = BrowserLauncher.class.getDeclaredMethod("healthCheckEndpoint");
        Method excludeProfilesMethod = BrowserLauncher.class.getDeclaredMethod("excludeProfiles");
        Method asyncMethod = BrowserLauncher.class.getDeclaredMethod("async");

        assertEquals(String.class, valueMethod.getReturnType(), 
                    "value() should return String");
        assertEquals(String.class, urlMethod.getReturnType(), 
                    "url() should return String");
        assertEquals(String[].class, urlsMethod.getReturnType(), 
                    "urls() should return String[]");
        assertEquals(String.class, healthCheckEndpointMethod.getReturnType(), 
                    "healthCheckEndpoint() should return String");
        assertEquals(String[].class, excludeProfilesMethod.getReturnType(), 
                    "excludeProfiles() should return String[]");
        assertEquals(boolean.class, asyncMethod.getReturnType(), 
                    "async() should return boolean");
    }

    @Test
    void annotation_shouldHaveCorrectDefaultValues() throws Exception {
        Method valueMethod = BrowserLauncher.class.getDeclaredMethod("value");
        Method urlMethod = BrowserLauncher.class.getDeclaredMethod("url");
        Method urlsMethod = BrowserLauncher.class.getDeclaredMethod("urls");
        Method healthCheckEndpointMethod = BrowserLauncher.class.getDeclaredMethod("healthCheckEndpoint");
        Method excludeProfilesMethod = BrowserLauncher.class.getDeclaredMethod("excludeProfiles");
        Method asyncMethod = BrowserLauncher.class.getDeclaredMethod("async");

        // Check default values
        assertEquals("", valueMethod.getDefaultValue(), 
                    "value() should have empty string as default");
        assertEquals("", urlMethod.getDefaultValue(), 
                    "url() should have empty string as default");
        assertArrayEquals(new String[]{}, (String[]) urlsMethod.getDefaultValue(), 
                    "urls() should have empty array as default");
        assertEquals("", healthCheckEndpointMethod.getDefaultValue(), 
                    "healthCheckEndpoint() should have empty string as default");
        assertArrayEquals(new String[]{"docker", "test", "zimaos"}, 
                         (String[]) excludeProfilesMethod.getDefaultValue(), 
                         "excludeProfiles() should have correct default values");
        assertEquals(false, asyncMethod.getDefaultValue(), 
                    "async() should have false as default");
    }

    @Test
    void annotation_urlMethodShouldHaveEmptyStringDefault() throws NoSuchMethodException {
        Method urlMethod = BrowserLauncher.class.getDeclaredMethod("url");
        assertEquals("", urlMethod.getDefaultValue(), 
                  "url() method should have empty string as default value");
    }

    @Test
    void annotation_shouldBeApplicableToClasses() throws Exception {
        // Test that annotation can be applied to classes
        assertTrue(TestClassWithAnnotation.class.isAnnotationPresent(BrowserLauncher.class), 
                  "Annotation should be applicable to classes");
    }

    @Test
    void annotation_shouldNotBeApplicableToMethods() throws Exception {
        // This is a compile-time check, but we can verify the Target annotation
        Target target = BrowserLauncher.class.getAnnotation(Target.class);
        assertNotNull(target);
        
        // Verify that METHOD is not in the target list
        for (ElementType elementType : target.value()) {
            assertNotEquals(ElementType.METHOD, elementType, 
                          "Annotation should not target methods");
        }
    }

    @Test
    void annotation_shouldNotBeApplicableToFields() throws Exception {
        // This is a compile-time check, but we can verify the Target annotation
        Target target = BrowserLauncher.class.getAnnotation(Target.class);
        assertNotNull(target);
        
        // Verify that FIELD is not in the target list
        for (ElementType elementType : target.value()) {
            assertNotEquals(ElementType.FIELD, elementType, 
                          "Annotation should not target fields");
        }
    }

    @Test
    void annotation_shouldAllowMultipleProfilesInExcludeProfiles() throws Exception {
        BrowserLauncher annotation = TestClassWithMultipleProfiles.class
            .getAnnotation(BrowserLauncher.class);
        
        assertNotNull(annotation);
        String[] profiles = annotation.excludeProfiles();
        assertEquals(4, profiles.length, "Should support multiple exclude profiles");
        assertArrayEquals(new String[]{"docker", "test", "prod", "staging"}, profiles);
    }

    @Test
    void annotation_shouldAllowEmptyExcludeProfiles() throws Exception {
        BrowserLauncher annotation = TestClassWithEmptyProfiles.class
            .getAnnotation(BrowserLauncher.class);
        
        assertNotNull(annotation);
        String[] profiles = annotation.excludeProfiles();
        assertEquals(0, profiles.length, "Should support empty exclude profiles");
    }

    // Test classes for annotation validation
    @BrowserLauncher(url = "https://example.com")
    static class TestClassWithAnnotation {
    }

    @BrowserLauncher(
        url = "https://example.com",
        excludeProfiles = {"docker", "test", "prod", "staging"}
    )
    static class TestClassWithMultipleProfiles {
    }

    @BrowserLauncher(
        url = "https://example.com",
        excludeProfiles = {}
    )
    static class TestClassWithEmptyProfiles {
    }
}
