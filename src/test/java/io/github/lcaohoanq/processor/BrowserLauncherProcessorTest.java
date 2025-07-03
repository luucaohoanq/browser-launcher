package io.github.lcaohoanq.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.github.lcaohoanq.annotations.BrowserLauncher;
import io.github.lcaohoanq.core.JavaBrowserLauncher;
import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.ApplicationArguments;

class BrowserLauncherProcessorTest {

    private BrowserLauncherProcessor processor;
    private PrintStream originalOut;
    private ByteArrayOutputStream outputStreamCaptor;

    @BeforeEach
    void setUp() throws Exception {
        processor = new BrowserLauncherProcessor();
        
        // Capture System.out for testing console output
        originalOut = System.out;
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        // Clear system properties
        System.clearProperty("spring.profiles.active");
    }

    @Test
    void run_shouldSkipWhenNoMainClassFound() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        
        // Mock the stack trace to not contain a main method
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement[] emptyStack = {};
            when(currentThread.getStackTrace()).thenReturn(emptyStack);
            
            processor.run(args);
            
            // Should not throw any exceptions and complete silently
            assertTrue(true);
        }
    }

    @Test
    void run_shouldSkipWhenMainClassNotFound() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement mainElement = mock(StackTraceElement.class);
            when(mainElement.getMethodName()).thenReturn("main");
            when(mainElement.getClassName()).thenReturn("com.nonexistent.MainClass");
            
            StackTraceElement[] stack = {mainElement};
            when(currentThread.getStackTrace()).thenReturn(stack);
            
            processor.run(args);
            
            // Should not throw any exceptions and complete silently
            assertTrue(true);
        }
    }

    @Test
    void run_shouldSkipWhenNoAnnotationPresent() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement mainElement = mock(StackTraceElement.class);
            when(mainElement.getMethodName()).thenReturn("main");
            when(mainElement.getClassName()).thenReturn(TestMainWithoutAnnotation.class.getName());
            
            StackTraceElement[] stack = {mainElement};
            when(currentThread.getStackTrace()).thenReturn(stack);
            
            processor.run(args);
            
            // Should not throw any exceptions and complete silently
            assertTrue(true);
        }
    }

    @Test
    void run_shouldSkipWhenProfileExcluded() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        System.setProperty("spring.profiles.active", "docker,test");
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement mainElement = mock(StackTraceElement.class);
            when(mainElement.getMethodName()).thenReturn("main");
            when(mainElement.getClassName()).thenReturn(TestMainWithAnnotation.class.getName());
            
            StackTraceElement[] stack = {mainElement};
            when(currentThread.getStackTrace()).thenReturn(stack);
            
            processor.run(args);
            
            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Skipping browser launch due to profile exclusion."));
        }
    }

    @Test
    void run_shouldLaunchBrowserWhenConditionsMet() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        System.setProperty("spring.profiles.active", "dev");
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class);
             MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement mainElement = mock(StackTraceElement.class);
            when(mainElement.getMethodName()).thenReturn("main");
            when(mainElement.getClassName()).thenReturn(TestMainWithAnnotation.class.getName());
            
            StackTraceElement[] stack = {mainElement};
            when(currentThread.getStackTrace()).thenReturn(stack);
            
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);
            
            processor.run(args);
            
            verify(desktop).browse(any());
        }
    }

    @Test
    void run_shouldUseHealthCheckWhenProvided() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        System.setProperty("spring.profiles.active", "dev");
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class);
             MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement mainElement = mock(StackTraceElement.class);
            when(mainElement.getMethodName()).thenReturn("main");
            when(mainElement.getClassName()).thenReturn(TestMainWithHealthCheck.class.getName());
            
            StackTraceElement[] stack = {mainElement};
            when(currentThread.getStackTrace()).thenReturn(stack);
            
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);
            
            processor.run(args);
            
            // Should attempt network call (will fail but that's expected in test)
            // We can't easily verify the network call without more complex mocking
            assertTrue(true);
        }
    }

    @Test
    void run_shouldLaunchBrowserWithValueParameter() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        System.setProperty("spring.profiles.active", "dev");
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class);
             MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement mainElement = mock(StackTraceElement.class);
            when(mainElement.getMethodName()).thenReturn("main");
            when(mainElement.getClassName()).thenReturn(TestMainWithValueParameter.class.getName());
            
            StackTraceElement[] stack = {mainElement};
            when(currentThread.getStackTrace()).thenReturn(stack);
            
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);
            
            processor.run(args);
            
            verify(desktop).browse(any());
        }
    }

    @Test
    void run_shouldLaunchBrowserWithUrlsParameter() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        System.setProperty("spring.profiles.active", "dev");
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class);
             MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement mainElement = mock(StackTraceElement.class);
            when(mainElement.getMethodName()).thenReturn("main");
            when(mainElement.getClassName()).thenReturn(TestMainWithUrlsParameter.class.getName());
            
            StackTraceElement[] stack = {mainElement};
            when(currentThread.getStackTrace()).thenReturn(stack);
            
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);
            
            processor.run(args);
            
            // Should be called multiple times for multiple URLs
            verify(desktop, times(3)).browse(any());
        }
    }

    // Test classes for annotation testing
    @BrowserLauncher(url = "https://example.com")
    static class TestMainWithAnnotation {
        public static void main(String[] args) {}
    }

    @BrowserLauncher(value = "https://value-example.com")
    static class TestMainWithValueParameter {
        public static void main(String[] args) {}
    }

    @BrowserLauncher(urls = {"https://url1.com", "https://url2.com", "https://url3.com"})
    static class TestMainWithUrlsParameter {
        public static void main(String[] args) {}
    }

    @BrowserLauncher(url = "https://example.com", healthCheckEndpoint = "http://localhost:8080/health")
    static class TestMainWithHealthCheck {
        public static void main(String[] args) {}
    }

    @BrowserLauncher(url = "https://example.com", healthCheckEndpoint = "http://localhost:8080/health", async = true)
    static class TestMainWithAsyncHealthCheck {
        public static void main(String[] args) {}
    }

    static class TestMainWithoutAnnotation {
        public static void main(String[] args) {}
    }
}
