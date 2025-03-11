package nl.rotterdam.monitor;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletConfig {

    @Bean
    public ServletRegistrationBean<PrometheusFormatServlet> prometheusFormatServlet() {
        return new ServletRegistrationBean<>(new PrometheusFormatServlet(), "/prometheus");
    }
}
