package nl.rotterdam.monitor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MonitorController {

    private final MonitorService monitorService;
    private final IPrometheusFormatter formatter;

    public MonitorController(MonitorService monitorService, IPrometheusFormatter formatter) {
        this.monitorService = monitorService;
        this.formatter = formatter;
    }

    @GetMapping(value = "/metrics", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getMetrics() {
        String xmlResponse = monitorService.getStatusPingXml();
        return formatter.formatToPrometheus(xmlResponse);
    }

    @GetMapping(value = "/statusPing", produces = MediaType.APPLICATION_XML_VALUE)
    public String getStatusPingXml() {
        return monitorService.getStatusPingXml();
    }

    @GetMapping(value = "/set-mode")
    public String setMode(@RequestParam("mode") String newMode) {
        switch (newMode.toLowerCase()) {
            case "success":
            case "warning":
            case "mixed":
            case "ignore_crit":
                monitorService.setMode(newMode);
                return "Mode set to: " + monitorService.getMode();
            default:
                return "Invalid state. Use 'success', 'warning', 'mixed', or 'ignore_crit'.";
        }
    }
}