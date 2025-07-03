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

class BrowserLauncherProcessorAdvancedTest {

    private BrowserLauncherProcessor processor;
    private PrintStream originalOut;
    private ByteArrayOutputStream outputStreamCaptor;

    @BeforeEach
    void setUp() throws Exception {
        processor = new BrowserLauncherProcessor();
        
        originalOut = System.out;
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        System.clearProperty("spring.profiles.active");
    }

    @Test
    void run_shouldHandleMultipleActiveProfiles() throws Exception {
        // Set multiple active profiles
        System.setProperty("spring.profiles.active", "prod,staging,docker");
        
        ApplicationArguments args = mock(ApplicationArguments.class);
        
        // Create a mock stack trace that includes our test class
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement[] stackTrace = {
                new StackTraceElement("com.example.Main", "main", "Main.java", 10)
            };
            when(currentThread.getStackTrace()).thenReturn(stackTrace);

            try (MockedStatic<Class> classMock = mockStatic(Class.class)) {
                Class<?> mockClass = TestClassWithExcludedProfiles.class;
                classMock.when(() -> Class.forName("com.example.Main")).thenReturn(mockClass);

                processor.run(args);

                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Skipping browser launch due to profile exclusion"), 
                          "Should skip when active profile is in exclude list");
            }
        }
    }

    @Test
    void run_shouldHandleEmptyActiveProfiles() throws Exception {
        // Set empty active profiles
        System.setProperty("spring.profiles.active", "");
        
        ApplicationArguments args = mock(ApplicationArguments.class);
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement[] stackTrace = {
                new StackTraceElement("com.example.Main", "main", "Main.java", 10)
            };
            when(currentThread.getStackTrace()).thenReturn(stackTrace);

            try (MockedStatic<Class> classMock = mockStatic(Class.class)) {
                Class<?> mockClass = TestClassWithSimpleAnnotation.class;
                classMock.when(() -> Class.forName("com.example.Main")).thenReturn(mockClass);

                try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
                    Desktop desktop = mock(Desktop.class);
                    desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
                    desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
                    when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

                    processor.run(args);

                    // Should open browser since no active profiles to exclude
                    try {
                        verify(desktop).browse(any());
                    } catch (Exception e) {
                        // Handle potential IOException from browse method
                    }
                }
            }
        }
    }

    @Test
    void run_shouldHandleClassNotFound() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement[] stackTrace = {
                new StackTraceElement("com.example.NonExistentClass", "main", "Main.java", 10)
            };
            when(currentThread.getStackTrace()).thenReturn(stackTrace);

            try (MockedStatic<Class> classMock = mockStatic(Class.class)) {
                classMock.when(() -> Class.forName("com.example.NonExistentClass"))
                        .thenThrow(new ClassNotFoundException("Class not found"));

                // Should not throw exception, just handle gracefully
                assertDoesNotThrow(() -> processor.run(args));
            }
        }
    }

    @Test
    void run_shouldHandleClassWithoutAnnotation() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement[] stackTrace = {
                new StackTraceElement("com.example.Main", "main", "Main.java", 10)
            };
            when(currentThread.getStackTrace()).thenReturn(stackTrace);

            try (MockedStatic<Class> classMock = mockStatic(Class.class)) {
                Class<?> mockClass = TestClassWithoutAnnotation.class;
                classMock.when(() -> Class.forName("com.example.Main")).thenReturn(mockClass);

                // Should not throw exception, just handle gracefully
                assertDoesNotThrow(() -> processor.run(args));
            }
        }
    }

    @Test
    void run_shouldHandleAsyncAnnotation() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement[] stackTrace = {
                new StackTraceElement("com.example.Main", "main", "Main.java", 10)
            };
            when(currentThread.getStackTrace()).thenReturn(stackTrace);

            try (MockedStatic<Class> classMock = mockStatic(Class.class)) {
                Class<?> mockClass = TestClassWithAsyncAnnotation.class;
                classMock.when(() -> Class.forName("com.example.Main")).thenReturn(mockClass);

                try (MockedStatic<JavaBrowserLauncher> launcherMock = mockStatic(JavaBrowserLauncher.class)) {
                    processor.run(args);

                    // Should call async method
                    launcherMock.verify(() -> JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(
                        anyString(), anyString()));
                }
            }
        }
    }

    @Test
    void run_shouldHandleSyncAnnotationWithHealthCheck() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement[] stackTrace = {
                new StackTraceElement("com.example.Main", "main", "Main.java", 10)
            };
            when(currentThread.getStackTrace()).thenReturn(stackTrace);

            try (MockedStatic<Class> classMock = mockStatic(Class.class)) {
                Class<?> mockClass = TestClassWithSyncHealthCheck.class;
                classMock.when(() -> Class.forName("com.example.Main")).thenReturn(mockClass);

                try (MockedStatic<JavaBrowserLauncher> launcherMock = mockStatic(JavaBrowserLauncher.class)) {
                    processor.run(args);

                    // Should call sync method with health check
                    launcherMock.verify(() -> JavaBrowserLauncher.doHealthCheckThenOpenHomePage(
                        anyString(), anyString()));
                }
            }
        }
    }

    @Test
    void run_shouldHandlePartialProfileMatch() throws Exception {
        // Set profile that partially matches exclude list
        System.setProperty("spring.profiles.active", "production");
        
        ApplicationArguments args = mock(ApplicationArguments.class);
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement[] stackTrace = {
                new StackTraceElement("com.example.Main", "main", "Main.java", 10)
            };
            when(currentThread.getStackTrace()).thenReturn(stackTrace);

            try (MockedStatic<Class> classMock = mockStatic(Class.class)) {
                Class<?> mockClass = TestClassWithExcludedProfiles.class; // excludes "prod"
                classMock.when(() -> Class.forName("com.example.Main")).thenReturn(mockClass);

                try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
                    Desktop desktop = mock(Desktop.class);
                    desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
                    desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
                    when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

                    processor.run(args);

                    // Should open browser since "production" != "prod"
                    try {
                        verify(desktop).browse(any());
                    } catch (Exception e) {
                        // Handle potential IOException from browse method
                    }
                }
            }
        }
    }

    // Test classes for processor validation
    @BrowserLauncher(url = "https://example.com")
    static class TestClassWithSimpleAnnotation {
    }

    @BrowserLauncher(
        url = "https://example.com",
        excludeProfiles = {"prod", "staging"}
    )
    static class TestClassWithExcludedProfiles {
    }

    @BrowserLauncher(
        url = "https://example.com",
        healthCheck = "http://localhost:8080/health",
        async = true
    )
    static class TestClassWithAsyncAnnotation {
    }

    @BrowserLauncher(
        url = "https://example.com",
        healthCheck = "http://localhost:8080/health",
        async = false
    )
    static class TestClassWithSyncHealthCheck {
    }

    static class TestClassWithoutAnnotation {
    }
}
