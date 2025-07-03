package com.example.demo;

import io.github.lcaohoanq.processor.BrowserLauncherProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @GetMapping("/debug/beans")
    public String debugBeans() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available beans:\n");
        
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            if (beanName.contains("browser") || beanName.contains("Browser")) {
                sb.append("Found browser-related bean: ").append(beanName).append("\n");
            }
        }
        
        // Check if BrowserLauncherProcessor bean exists
        try {
            BrowserLauncherProcessor processor = applicationContext.getBean(BrowserLauncherProcessor.class);
            sb.append("BrowserLauncherProcessor bean found: ").append(processor != null).append("\n");
        } catch (Exception e) {
            sb.append("BrowserLauncherProcessor bean NOT found: ").append(e.getMessage()).append("\n");
        }
        
        return sb.toString();
    }
}
