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
        Method urlMethod = BrowserLauncher.class.getDeclaredMethod("url");
        Method healthCheckMethod = BrowserLauncher.class.getDeclaredMethod("healthCheck");
        Method excludeProfilesMethod = BrowserLauncher.class.getDeclaredMethod("excludeProfiles");
        Method asyncMethod = BrowserLauncher.class.getDeclaredMethod("async");

        assertNotNull(urlMethod, "url() method should exist");
        assertNotNull(healthCheckMethod, "healthCheck() method should exist");
        assertNotNull(excludeProfilesMethod, "excludeProfiles() method should exist");
        assertNotNull(asyncMethod, "async() method should exist");
    }

    @Test
    void annotation_shouldHaveCorrectReturnTypes() throws NoSuchMethodException {
        Method urlMethod = BrowserLauncher.class.getDeclaredMethod("url");
        Method healthCheckMethod = BrowserLauncher.class.getDeclaredMethod("healthCheck");
        Method excludeProfilesMethod = BrowserLauncher.class.getDeclaredMethod("excludeProfiles");
        Method asyncMethod = BrowserLauncher.class.getDeclaredMethod("async");

        assertEquals(String.class, urlMethod.getReturnType(), 
                    "url() should return String");
        assertEquals(String.class, healthCheckMethod.getReturnType(), 
                    "healthCheck() should return String");
        assertEquals(String[].class, excludeProfilesMethod.getReturnType(), 
                    "excludeProfiles() should return String[]");
        assertEquals(boolean.class, asyncMethod.getReturnType(), 
                    "async() should return boolean");
    }

    @Test
    void annotation_shouldHaveCorrectDefaultValues() throws Exception {
        Method healthCheckMethod = BrowserLauncher.class.getDeclaredMethod("healthCheck");
        Method excludeProfilesMethod = BrowserLauncher.class.getDeclaredMethod("excludeProfiles");
        Method asyncMethod = BrowserLauncher.class.getDeclaredMethod("async");

        // Check default values
        assertEquals("", healthCheckMethod.getDefaultValue(), 
                    "healthCheck() should have empty string as default");
        assertArrayEquals(new String[]{"docker", "test", "zimaos"}, 
                         (String[]) excludeProfilesMethod.getDefaultValue(), 
                         "excludeProfiles() should have correct default values");
        assertEquals(false, asyncMethod.getDefaultValue(), 
                    "async() should have false as default");
    }

    @Test
    void annotation_urlMethodShouldNotHaveDefault() throws NoSuchMethodException {
        Method urlMethod = BrowserLauncher.class.getDeclaredMethod("url");
        assertNull(urlMethod.getDefaultValue(), 
                  "url() method should not have a default value");
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
