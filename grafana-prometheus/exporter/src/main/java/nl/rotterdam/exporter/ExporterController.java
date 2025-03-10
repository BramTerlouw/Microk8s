package nl.rotterdam.exporter;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;
import java.util.List;

@RestController
public class ExporterController {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String STATUS_PING_URL = "http://localhost:8080/statusPing";
    private final IPrometheusFormatter formatter = new PrometheusFormatter();

    @GetMapping(value = "/metrics", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getMetrics() {
        String xmlResponse = restTemplate.getForObject(STATUS_PING_URL, String.class);
        return formatter.formatToPrometheus(xmlResponse);
    }
}