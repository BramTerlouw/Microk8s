package nl.rotterdam.monitor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class MonitorController {

    @Value("${APP_NAME:defaultApp}")
    private String appName;

    @Value("${APP_VERSION:0.0.1-SNAPSHOT}")
    private String appVersion;

    private String mode = "success";

    // This would be returned by the new "PrometheusFormatter"
    @GetMapping(value = "/metrics", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getMetrics() {

        switch (mode.toLowerCase()) {
            case "success":
                return "# HELP ping_check_state State of the ping_check (0=SUCCESS, 1=DISABLED, 2=UNKNOWN, 3=WARNING, 4=CRITICAL)\n" +
                        "# TYPE ping_check_state gauge\n" +
                        String.format("ping_check_state{application=\"%s\", version=\"%s\", name=\"Maakt SMTP verbinding\", details=\"\"} 0\n", appName, appVersion) +
                        String.format("ping_check_state{application=\"%s\", version=\"%s\", name=\"BetaalModuleService\", details=\"\"} 0\n", appName, appVersion) +
                        String.format("ping_check_state{application=\"%s\", version=\"%s\", name=\"CMS vertalingen en\", details=\"\"} 0\n", appName, appVersion);
            case "warning":
                return "# HELP ping_check_state State of the ping_check (0=SUCCESS, 1=DISABLED, 2=UNKNOWN, 3=WARNING, 4=CRITICAL)\n" +
                        "# TYPE ping_check_state gauge\n" +
                        String.format("ping_check_state{application=\"%s\", version=\"%s\", name=\"Maakt SMTP verbinding\", details=\"Takes a while...\"} 3\n", appName, appVersion) +
                        String.format("ping_check_state{application=\"%s\", version=\"%s\", name=\"BetaalModuleService\", details=\"\"} 0\n", appName, appVersion) +
                        String.format("ping_check_state{application=\"%s\", version=\"%s\", name=\"CMS vertalingen en\", details=\"\"} 0\n", appName, appVersion);
            case "mixed":
                return "# HELP ping_check_state State of the ping_check (0=SUCCESS, 1=DISABLED, 2=UNKNOWN, 3=WARNING, 4=CRITICAL)\n" +
                        "# TYPE ping_check_state gauge\n" +
                        String.format("ping_check_state{application=\"%s\", version=\"%s\", name=\"Maakt SMTP verbinding\", details=\"Takes a while...\"} 3\n", appName, appVersion) +
                        String.format("ping_check_state{application=\"%s\", version=\"%s\", name=\"BetaalModuleService\", details=\"Timeout exceeded\"} 4\n", appName, appVersion) +
                        String.format("ping_check_state{application=\"%s\", version=\"%s\", name=\"CMS vertalingen en\", details=\"\"} 0\n", appName, appVersion);
            case "ignore_crit":
                return "# HELP ping_check_state State of the ping_check (0=SUCCESS, 1=DISABLED, 2=UNKNOWN, 3=WARNING, 4=CRITICAL)\n" +
                        "# TYPE ping_check_state gauge\n" +
                        String.format("ping_check_state{application=\"%s\", version=\"%s\", name=\"Maakt SMTP verbinding\", details=\"\"} 0\n", appName, appVersion) +
                        String.format("ping_check_state{application=\"%s\", version=\"%s\", name=\"CMS vertalingen en\", details=\"Voor functioneel beheer\"} 4\n", appName, appVersion) +
                        String.format("ping_check_state{application=\"%s\", version=\"%s\", name=\"BetaalModuleService\", details=\"Timeout exceeded\"} 4\n", appName, appVersion);
            default:
                return "# HELP ping_check_state State of the ping_check (0=SUCCESS, 1=DISABLED, 2=UNKNOWN, 3=WARNING, 4=CRITICAL)\n" +
                        "# TYPE ping_check_state gauge\n";
        }
    }

    // This would be returned by the MonitorXmlFormatter, is used by the exporter
    @GetMapping(value = "/statusPing", produces = MediaType.APPLICATION_XML_VALUE)
    public String getStatusPingXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <statusResponse>
                    <application>%s</application>
                    <version>%s</version>
                    <checks>
                        <check state="WARNING">
                            <name>Maakt SMTP verbinding</name>
                            <details>Takes a while...</details>
                        </check>
                        <check state="CRITICAL">
                            <name>BetaalModuleService</name>
                            <details>Timeout exceeded</details>
                        </check>
                        <check state="CRITICAL">
                            <name>CMS vertalingen en</name>
                            <details>Voor functioneel beheer</details>
                        </check>
                    </checks>
                </statusResponse>
                """.formatted(appName, appVersion);
    }

    // This is strictly for demo purposes
    @GetMapping(value = "/set-mode")
    public String setMode(@RequestParam("mode") String newMode) {
        switch (newMode.toLowerCase()) {
            case "success":
            case "warning":
            case "mixed":
            case "ignore_crit":
                mode = newMode.toLowerCase();
                return "Mode set to: " + mode;
            default:
                return "Invalid state. Use 'success', 'warning', or 'mixed'.";
        }
    }
}