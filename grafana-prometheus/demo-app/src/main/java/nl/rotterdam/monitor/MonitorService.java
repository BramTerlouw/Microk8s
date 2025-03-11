package nl.rotterdam.monitor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MonitorService {

    @Value("${APP_NAME:defaultApp}")
    private String appName;

    @Value("${APP_VERSION:0.0.1-SNAPSHOT}")
    private String appVersion;

    private String mode = "success";

    public String getStatusPingXml() {
        switch (mode.toLowerCase()) {
            case "success":
                return """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <statusResponse>
                            <application>%s</application>
                            <version>%s</version>
                            <checks>
                                <check state="SUCCESS">
                                    <name>Maakt SMTP verbinding</name>
                                    <details></details>
                                </check>
                                <check state="SUCCESS">
                                    <name>BetaalModuleService</name>
                                    <details></details>
                                </check>
                                <check state="SUCCESS">
                                    <name>CMS vertalingen en</name>
                                    <details></details>
                                </check>
                            </checks>
                        </statusResponse>
                        """.formatted(appName, appVersion);
            case "warning":
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
                                <check state="SUCCESS">
                                    <name>BetaalModuleService</name>
                                    <details></details>
                                </check>
                                <check state="SUCCESS">
                                    <name>CMS vertalingen en</name>
                                    <details></details>
                                </check>
                            </checks>
                        </statusResponse>
                        """.formatted(appName, appVersion);
            case "mixed":
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
                                <check state="SUCCESS">
                                    <name>CMS vertalingen en</name>
                                    <details></details>
                                </check>
                            </checks>
                        </statusResponse>
                        """.formatted(appName, appVersion);
            case "ignore_crit":
                return """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <statusResponse>
                            <application>%s</application>
                            <version>%s</version>
                            <checks>
                                <check state="SUCCESS">
                                    <name>Maakt SMTP verbinding</name>
                                    <details></details>
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
            default:
                return """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <statusResponse>
                            <application>%s</application>
                            <version>%s</version>
                            <checks></checks>
                        </statusResponse>
                        """.formatted(appName, appVersion);
        }
    }

    public void setMode(String newMode) {
        this.mode = newMode.toLowerCase();
    }

    public String getMode() {
        return mode;
    }
}