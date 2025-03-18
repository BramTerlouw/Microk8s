package nl.rotterdam.middleware;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/webhook")
public class MiddlewareController {

    @Value("${WEBHOOK_URL:default}")
    private String webhook_url;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/print")
    public void handlePrint(@RequestBody JsonNode req) {
        System.out.println("Received Grafana Alert JSON: " + req.toPrettyString());
    }

    @PostMapping("/alert")
    public ResponseEntity<String> handleAlert(@RequestBody JsonNode req) {
        try {
            System.out.println("Received Grafana Alert: " + req.toString());

            JsonNode teamsMessage = transformGrafanaToAdaptiveCard(req);
            sendToTeams(teamsMessage);
            return ResponseEntity.ok("Alert processed successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing alert.");
        }
    }

    private JsonNode transformGrafanaToAdaptiveCard(JsonNode grafanaPayload) {
        try {
            // Extract fields from Grafana JSON
            String title = grafanaPayload.get("title").asText();
            String message = grafanaPayload.get("message").asText();
            String status = grafanaPayload.get("status").asText();
            String externalUrl = grafanaPayload.get("externalURL").asText();

            // Extract first alert object
            JsonNode alert = grafanaPayload.get("alerts").get(0);
            String alertName = alert.get("labels").get("alertname").asText();
            String instance = alert.get("labels").get("instance").asText();
            String summary = alert.get("annotations").get("summary").asText();
            String startsAt = alert.get("startsAt").asText();
            String silenceUrl = alert.get("silenceURL").asText();

            // Properly escape newlines in message
            String escapedMessage = message.replace("\n", "\\n").replace("\"", "\\\"");
            String escapedSummary = summary.replace("\n", "\\n").replace("\"", "\\\"");

            // Convert status to a user-friendly format
            String severityColor = status.equalsIgnoreCase("firing") ? "Attention" : "Good";

            // Build Adaptive Card JSON
            String adaptiveCardJson = String.format("""
            {
                "type": "message",
                "attachments": [
                    {
                        "contentType": "application/vnd.microsoft.card.adaptive",
                        "content": {
                            "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
                            "type": "AdaptiveCard",
                            "version": "1.4",
                            "body": [
                                {
                                    "type": "TextBlock",
                                    "size": "Large",
                                    "weight": "Bolder",
                                    "color": "%s",
                                    "text": "%s"
                                },
                                {
                                    "type": "TextBlock",
                                    "text": "%s",
                                    "wrap": true
                                },
                                {
                                    "type": "TextBlock",
                                    "text": "Alert Name: %s",
                                    "weight": "Bolder"
                                },
                                {
                                    "type": "TextBlock",
                                    "text": "Instance: %s"
                                },
                                {
                                    "type": "TextBlock",
                                    "text": "Status: %s",
                                    "weight": "Bolder"
                                },
                                {
                                    "type": "TextBlock",
                                    "text": "Started At: %s",
                                    "spacing": "Small"
                                },
                                {
                                    "type": "TextBlock",
                                    "text": "%s",
                                    "wrap": true
                                },
                                {
                                    "type": "ActionSet",
                                    "actions": [
                                        {
                                            "type": "Action.OpenUrl",
                                            "title": "View Alert in Grafana",
                                            "url": "%s"
                                        },
                                        {
                                            "type": "Action.OpenUrl",
                                            "title": "Silence Alert",
                                            "url": "%s"
                                        }
                                    ]
                                }
                            ]
                        }
                    }
                ]
            }
            """, severityColor, title, escapedSummary, alertName, instance, status, startsAt, escapedMessage, externalUrl, silenceUrl);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(adaptiveCardJson);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Adaptive Card JSON", e);
        }
    }


    private boolean sendToTeams(JsonNode teamsMessage) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(webhook_url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(teamsMessage.toString(), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                System.out.println("DEBUG: " + response.getCode());
                return response.getCode() == 200;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}