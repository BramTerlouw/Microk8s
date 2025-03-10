package nl.rotterdam.exporter;

public interface IPrometheusFormatter {
    String formatToPrometheus(String xmlResponse);
}