package com.pcadvisor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.base-dir:./src/main/resources/static}")
    private String uploadBaseDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传图片
        String dir = Paths.get(uploadBaseDir, "images/evaluations")
                .toAbsolutePath().normalize().toString()
                .replace("\\", "/");
        if (!dir.endsWith("/")) dir += "/";
        registry.addResourceHandler("/images/evaluations/**")
                .addResourceLocations("file:" + dir);

        // SPA 回退：非 /api/、非 /images/ 的 GET 请求回退到 index.html，由 Vue Router 处理
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        // 回退到 index.html（SPA）
                        Resource fallback = new ClassPathResource("/static/index.html");
                        if (fallback.exists() && fallback.isReadable()) {
                            return fallback;
                        }
                        return null;
                    }
                });
    }
}
