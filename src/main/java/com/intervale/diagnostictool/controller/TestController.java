package com.intervale.diagnostictool.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    @GetMapping("/css/style.css")
    @ResponseBody
    public ResponseEntity<byte[]> getCss() throws Exception {
        logger.info("Trying to load CSS file");
        Resource resource = new ClassPathResource("static/css/style.css");
        byte[] bytes = Files.readAllBytes(Paths.get(resource.getURI()));
        return ResponseEntity.ok().contentType(MediaType.valueOf("text/css")).body(bytes);
    }

    @GetMapping("/test-resources")
    @ResponseBody
    public String testResources() {
        return "Test resources are working! ";
    }
    
    @GetMapping("/test")
    public String testPage() {
        logger.info("Test page requested");
        return "test";
    }
    
    @GetMapping("/test-simple")
    @ResponseBody
    public String testSimple() {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Simple Test</title>
            <!-- Bootstrap 5 CSS -->
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
            <!-- Bootstrap Icons -->
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
            <style>
                body { padding: 20px; }
            </style>
        </head>
        <body>
            <div class="container mt-5">
                <h1 class="text-primary">Bootstrap Test</h1>
                
                <div class="alert alert-success mt-4">
                    <i class="bi bi-check-circle-fill me-2"></i>
                    This is a success alert with a Bootstrap icon
                </div>
                
                <button class="btn btn-primary me-2">
                    <i class="bi bi-check-lg me-1"></i> Primary Button
                </button>
                
                <button class="btn btn-outline-secondary">
                    <i class="bi bi-x-lg me-1"></i> Secondary Button
                </button>
                
                <div class="mt-3">
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" id="testCheck">
                        <label class="form-check-label" for="testCheck">
                            Test checkbox
                        </label>
                    </div>
                </div>
            </div>
            
            <!-- Bootstrap JS Bundle with Popper -->
            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
            
            <script>
                document.addEventListener('DOMContentLoaded', function() {
                    console.log('Bootstrap version:', bootstrap.Tooltip.VERSION);
                });
            </script>
        </body>
        </html>
        """;
    }
}
