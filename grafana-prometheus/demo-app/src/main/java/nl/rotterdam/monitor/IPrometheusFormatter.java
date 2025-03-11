package nl.rotterdam.monitor;

public interface IPrometheusFormatter {
    String formatToPrometheus(String xmlResponse);
}