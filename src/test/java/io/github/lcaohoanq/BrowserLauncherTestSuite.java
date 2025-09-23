/**
 * Copyright (c) 2025 lcaohoanq. All rights reserved.
 *
 * This software is the confidential and proprietary information of lcaohoanq.
 * You shall not disclose such confidential information and shall use it only in
 * accordance with the terms of the license agreement you entered into with lcaohoanq.
 */
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
