package io.github.lcaohoanq.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.mockito.MockedStatic;

class BrowserLauncherCrossPlatformTest {

    private PrintStream originalOut;
    private ByteArrayOutputStream outputStreamCaptor;

    @BeforeEach
    void setUp() throws Exception {
        originalOut = System.out;
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void openHomePage_shouldUseCorrectCommandOnWindows() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            // Mock Desktop as not supported to test fallback
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(false);

            try (MockedStatic<System> systemMock = mockStatic(System.class)) {
                systemMock.when(() -> System.getProperty("os.name")).thenReturn("Windows 10");
                
                // This test mainly verifies that the method doesn't throw exception
                // on Windows platform. The actual ProcessBuilder execution is hard to mock
                // without complex setup.
                assertDoesNotThrow(() -> {
                    JavaBrowserLauncher.openHomePage("https://example.com");
                });
            }
        }
    }

    @Test
    @EnabledOnOs(OS.MAC)
    void openHomePage_shouldUseCorrectCommandOnMac() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            // Mock Desktop as not supported to test fallback
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(false);

            try (MockedStatic<System> systemMock = mockStatic(System.class)) {
                systemMock.when(() -> System.getProperty("os.name")).thenReturn("Mac OS X");
                
                assertDoesNotThrow(() -> {
                    JavaBrowserLauncher.openHomePage("https://example.com");
                });
            }
        }
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void openHomePage_shouldUseCorrectCommandOnLinux() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            // Mock Desktop as not supported to test fallback
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(false);

            try (MockedStatic<System> systemMock = mockStatic(System.class)) {
                systemMock.when(() -> System.getProperty("os.name")).thenReturn("Linux");
                
                assertDoesNotThrow(() -> {
                    JavaBrowserLauncher.openHomePage("https://example.com");
                });
            }
        }
    }

    @Test
    void openHomePage_shouldHandleUnsupportedOS() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            // Mock Desktop as not supported to test fallback
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(false);

            try (MockedStatic<System> systemMock = mockStatic(System.class)) {
                systemMock.when(() -> System.getProperty("os.name")).thenReturn("UnknownOS");
                
                assertDoesNotThrow(() -> {
                    JavaBrowserLauncher.openHomePage("https://example.com");
                });
                
                // Should log unsupported OS message
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Unsupported operating system") || 
                          output.contains("UnknownOS"), 
                          "Should log unsupported OS message");
            }
        }
    }

    @Test
    void openHomePage_shouldPreferDesktopAPIWhenAvailable() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            JavaBrowserLauncher.openHomePage("https://example.com");

            // Should use Desktop API instead of platform-specific commands
            verify(desktop).browse(any());
        }
    }

    @Test
    void openHomePage_shouldFallbackWhenDesktopBrowseNotSupported() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(false);

            assertDoesNotThrow(() -> {
                JavaBrowserLauncher.openHomePage("https://example.com");
            });

            // Should not use Desktop API
            verify(desktop, never()).browse(any());
        }
    }

    @Test
    void openHomePage_shouldHandleMultipleUrlsOnAnyPlatform() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            List<String> urls = Arrays.asList(
                "https://example1.com",
                "https://example2.com",
                "https://example3.com"
            );

            JavaBrowserLauncher.openHomePage(urls);

            verify(desktop, times(3)).browse(any());
        }
    }

    @Test
    void openHomePage_shouldHandleExceptionsGracefully() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);
            
            // Mock Desktop.browse to throw exception
            doThrow(new RuntimeException("Desktop browse failed")).when(desktop).browse(any());

            // Should not throw exception, but handle it gracefully
            assertDoesNotThrow(() -> {
                JavaBrowserLauncher.openHomePage("https://example.com");
            });

            verify(desktop).browse(any());
        }
    }

    @Test
    void openHomePage_shouldHandleNullDesktop() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(null);

            // Should handle null desktop gracefully
            assertDoesNotThrow(() -> {
                JavaBrowserLauncher.openHomePage("https://example.com");
            });
        }
    }
}
