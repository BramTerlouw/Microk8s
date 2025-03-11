package nl.rotterdam.monitor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PrometheusFormatter implements IPrometheusFormatter {

    @Override
    public String formatToPrometheus(String xmlResponse) {
        try {
            
            // Map XML response to JAXBContext instance with defined statusResponse class
            JAXBContext jaxbContext = JAXBContext.newInstance(StatusResponse.class);
            StatusResponse statusResponse = (StatusResponse) jaxbContext.createUnmarshaller()
                    .unmarshal(new StringReader(xmlResponse));

            // Define stringbuilder for metrics
            StringBuilder metrics = new StringBuilder();
            metrics.append("# HELP ping_check_state State of the ping_check (0=SUCCESS, 1=DISABLED, 2=UNKNOWN, 3=WARNING, 4=CRITICAL)\n");
            metrics.append("# TYPE ping_check_state gauge\n");

            // Iterate through checks
            for (Check check : statusResponse.getChecks().getCheckList()) {
                
                // Convert the string state to corresponding int value
                int stateValue = convertStateToNumeric(check.getState());

                // Append new ping_check_state Prometheus line
                metrics.append(String.format(
                        "ping_check_state{application=\"%s\", version=\"%s\", name=\"%s\", details=\"%s\"} %d\n",
                        escape(statusResponse.getApplication()),
                        escape(statusResponse.getVersion()),
                        escape(check.getName()),
                        escape(check.getDetails()),
                        stateValue
                ));
            }

            return metrics.toString();
        } catch (Exception e) {
            return "# ERROR: Failed to process XML: " + e.getMessage() + "\n";
        }
    }

    // Convert state String value to numeric value
    private int convertStateToNumeric(String state) {
        return switch (state != null ? state.toUpperCase() : "UNKNOWN") {
            case "SUCCESS" -> 0;
            case "DISABLED" -> 1;
            case "UNKNOWN" -> 2;
            case "WARNING" -> 3;
            case "CRITICAL" -> 4;
            default -> 2; // Unknown as fallback
        };
    }

    private String escape(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");
    }

    @XmlRootElement(name = "statusResponse")
    static class StatusResponse {
        private String application;
        private String version;
        private Checks checks;

        @XmlElement
        public String getApplication() {
            return application;
        }

        public void setApplication(String application) {
            this.application = application;
        }

        @XmlElement
        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        @XmlElement
        public Checks getChecks() {
            return checks;
        }

        public void setChecks(Checks checks) {
            this.checks = checks;
        }
    }

    static class Checks {
        private List<Check> checkList;

        @XmlElement(name = "check")
        public List<Check> getCheckList() {
            return checkList;
        }

        public void setCheckList(List<Check> checkList) {
            this.checkList = checkList;
        }
    }

    static class Check {
        private String state;
        private String name;
        private String details;

        @XmlAttribute
        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        @XmlElement
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlElement
        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }
    }
}