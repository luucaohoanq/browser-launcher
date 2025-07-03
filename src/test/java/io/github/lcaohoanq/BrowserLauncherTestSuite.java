package io.github.lcaohoanq;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Browser Launcher Test Suite")
@SelectPackages({
    "io.github.lcaohoanq.core",
    "io.github.lcaohoanq.processor", 
    "io.github.lcaohoanq.annotations",
    "io.github.lcaohoanq.integration",
    "io.github.lcaohoanq"
})
public class BrowserLauncherTestSuite {
    // This class remains empty, it is used only as a holder for the above annotations
}
