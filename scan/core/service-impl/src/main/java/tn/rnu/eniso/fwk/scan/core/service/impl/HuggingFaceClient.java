package tn.rnu.eniso.fwk.scan.core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client for interacting with Hugging Face Inference API.
 */
@Slf4j
@Component
public class HuggingFaceClient {

    private final WebClient webClient;
    private final String apiToken;
    private final String apiUrl;
    private final boolean enabled;
    private final int maxTokens;
    private final double temperature;
    private final ObjectMapper objectMapper;

    public HuggingFaceClient(
            WebClient.Builder webClientBuilder,
            @Value("${huggingface.api.token:}") String apiToken,
            @Value("${huggingface.api.url:https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.2}") String apiUrl,
            @Value("${huggingface.api.enabled:true}") boolean enabled,
            @Value("${huggingface.api.timeout:30000}") int timeout,
            @Value("${huggingface.api.max-tokens:500}") int maxTokens,
            @Value("${huggingface.api.temperature:0.7}") double temperature,
            ObjectMapper objectMapper) {

        this.apiToken = apiToken;
        this.apiUrl = apiUrl;
        this.enabled = enabled;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.objectMapper = objectMapper;

        this.webClient = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public boolean isAvailable() {
        return enabled && apiToken != null && !apiToken.isEmpty();
    }

    public String generateResponse(String prompt) {
        if (!enabled || apiToken == null || apiToken.isEmpty()) {
            log.warn("Hugging Face API is disabled or token is missing. Using fallback.");
            return generateFallbackResponse(prompt);
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "inputs", prompt,
                    "parameters", Map.of(
                            "max_new_tokens", maxTokens,
                            "temperature", temperature,
                            "return_full_text", false));

            String responseBody = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            return parseResponse(responseBody);

        } catch (WebClientResponseException e) {
            log.error("Hugging Face API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return generateFallbackResponse(prompt);
        } catch (Exception e) {
            log.warn("Error calling Hugging Face API: {} - using fallback response", e.getMessage());
            return generateFallbackResponse(prompt);
        }
    }

    private String parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.isArray() && root.size() > 0) {
                return root.get(0).path("generated_text").asText();
            } else if (root.isObject() && root.has("generated_text")) {
                return root.get("generated_text").asText();
            }
            return responseBody;
        } catch (Exception e) {
            log.error("Error parsing AI response", e);
            return "Error parsing AI response";
        }
    }

    /**
     * Build a prompt for alert analysis
     */
    public String buildAlertAnalysisPrompt(String alertDetails) {
        return String.format("""
                You are an advanced Cybersecurity AI Assistant. Analyze the following intrusion detection alert.

                ALERT DATA:
                %s

                RESPONSE FORMAT (Markdown):
                ### 🛡️ Threat Analysis
                [Detailed technical explanation of what is happening]

                ### ⚠️ Severity & Impact
                [Assessment of severity and potential business impact]

                ### 🛠️ Recommended Remediation
                1. [Actionable step 1]
                2. [Actionable step 2]
                3. [Actionable step 3]

                ### 🔍 Investigation Commands
                ```bash
                [Provide 2-3 relevant Linux/Network commands to investigate this]
                ```

                Keep the tone professional, concise, and actionable.
                """, alertDetails);
    }

    /**
     * Build a prompt for security summary
     */
    public String buildSecuritySummaryPrompt(String alertsSummary) {
        return String.format(
                """
                        You are a CISO-level AI Security Consultant. Provide a strategic summary of the following security events.

                        EVENTS SUMMARY:
                        %s

                        RESPONSE FORMAT (Markdown):
                        ### 📊 Executive Summary
                        [High-level overview of the security posture]

                        ### 🚨 Top Security Concerns
                        * **[Concern 1]**: [Brief explanation]
                        * **[Concern 2]**: [Brief explanation]

                        ### 🎯 Strategic Recommendations
                        1. [Strategic recommendation 1]
                        2. [Strategic recommendation 2]

                        ### 🔮 Threat Intelligence
                        [Brief insight on observed patterns]
                        """,
                alertsSummary);
    }

    /**
     * Generate intelligent fallback response when Hugging Face API is unavailable
     */
    private String generateFallbackResponse(String prompt) {
        log.info("Generating smart fallback AI response");

        String promptLower = prompt.toLowerCase();
        StringBuilder response = new StringBuilder();

        // Extract specific details from the prompt for dynamic advice
        String sourceIp = extractValue(prompt, "Source IP: ([\\d\\.]+)");
        String destIp = extractValue(prompt, "Destination IP: ([\\d\\.]+)");
        String signature = extractValue(prompt, "Signature: (.+)");
        String port = extractValue(prompt, "Source IP: .*\\(Port: (\\d+)\\)");
        if (port.equals("Unknown")) {
            port = extractValue(prompt, "Destination IP: .*\\(Port: (\\d+)\\)");
        }

        // Detect context
        boolean isPortScan = promptLower.contains("port scan") || promptLower.contains("nmap")
                || promptLower.contains("scanning");
        boolean isDos = promptLower.contains("flood") || promptLower.contains("dos") || promptLower.contains("denial");
        boolean isSqlInjection = promptLower.contains("sql") || promptLower.contains("injection")
                || promptLower.contains("union select");
        boolean isSsh = promptLower.contains("ssh") || promptLower.contains("login") || promptLower.contains("brute");

        response.append("### 🛡️ AI Security Analysis\n\n");

        if (isPortScan) {
            response.append("**Threat Detected**: Network Reconnaissance (Port Scanning)\n\n");
            response.append("The detected activity indicates a systematic attempt by **" + sourceIp
                    + "** to probe open ports on your network. ");
            response.append(
                    "This is typically a precursor to a targeted attack, where an adversary maps out your attack surface.\n\n");

            response.append("### ⚠️ Impact Assessment\n");
            response.append("* **Severity**: MEDIUM to HIGH\n");
            response.append("* **Risk**: Exposure of vulnerable services on host **" + destIp
                    + "** and potential exploitation.\n\n");

            response.append("### 🛠️ Remediation Steps\n");
            response.append("1. **Block Source**: Immediately block the source IP **" + sourceIp
                    + "** at the firewall level.\n");
            response.append("2. **Review Rules**: Ensure only necessary ports are open to the internet on **" + destIp
                    + "**.\n");
            response.append(
                    "3. **Enable IPS**: Activate Intrusion Prevention System to drop scan packets automatically.\n\n");

            response.append("### 🔍 Investigation\n");
            response.append("```bash\n");
            response.append("# Check current connections from source\n");
            response.append("netstat -an | grep " + sourceIp + "\n");
            response.append("# Analyze firewall logs\n");
            response.append("grep " + sourceIp + " /var/log/syslog\n");
            response.append("```");

        } else if (isDos) {
            response.append("**Threat Detected**: Denial of Service (DoS) Attempt\n\n");
            response.append("High-volume traffic detected from **" + sourceIp + "** targeting **" + destIp + "**. ");
            response.append(
                    "This pattern suggests a SYN Flood or UDP Flood attack aimed at exhausting system resources.\n\n");

            response.append("### ⚠️ Impact Assessment\n");
            response.append("* **Severity**: HIGH to CRITICAL\n");
            response.append(
                    "* **Risk**: Service unavailability for **" + destIp + "** and potential system crash.\n\n");

            response.append("### 🛠️ Remediation Steps\n");
            response.append("1. **Rate Limiting**: Implement strict rate limiting on incoming connections from **"
                    + sourceIp + "**.\n");
            response.append("2. **Block IP**: Add **" + sourceIp + "** to the blocklist immediately.\n");
            response.append("3. **Upstream Filtering**: Contact ISP if traffic saturates the link.\n\n");

            response.append("### 🔍 Investigation\n");
            response.append("```bash\n");
            response.append("# Monitor traffic in real-time\n");
            response.append("tcpdump -i eth0 -n src " + sourceIp + "\n");
            response.append("```");

        } else if (isSqlInjection) {
            response.append("**Threat Detected**: SQL Injection Attempt\n\n");
            response.append("Malicious SQL syntax detected in HTTP request parameters from **" + sourceIp + "**. ");
            response.append("The attacker is attempting to manipulate database queries on **" + destIp + "**.\n\n");

            response.append("### ⚠️ Impact Assessment\n");
            response.append("* **Severity**: CRITICAL\n");
            response.append("* **Risk**: Data breach, unauthorized access, and data loss on **" + destIp + "**.\n\n");

            response.append("### 🛠️ Remediation Steps\n");
            response.append("1. **Block Source**: Block **" + sourceIp + "** at the WAF or firewall.\n");
            response.append("2. **Input Validation**: Ensure all user inputs are sanitized and parameterized.\n");
            response.append("3. **Audit Logs**: Check database logs for any successful query executions.\n\n");

            response.append("### 🔍 Investigation\n");
            response.append("```bash\n");
            response.append("# Search access logs for suspicious patterns\n");
            response.append("grep -i \"union select\" /var/log/nginx/access.log | grep " + sourceIp + "\n");
            response.append("```");

        } else if (isSsh) {
            response.append("**Threat Detected**: SSH Brute-Force/Login Attempt\n\n");
            response.append("Repeated failed SSH login attempts from **" + sourceIp + "** detected. ");
            response.append(
                    "This indicates an adversary is trying to gain unauthorized access to **" + destIp + "**.\n\n");

            response.append("### ⚠️ Impact Assessment\n");
            response.append("* **Severity**: HIGH\n");
            response.append("* **Risk**: Unauthorized system access, privilege escalation, and data exfiltration.\n\n");

            response.append("### 🛠️ Remediation Steps\n");
            response.append("1. **Block Source**: Block **" + sourceIp + "** at the firewall.\n");
            response.append("2. **Disable Password Auth**: Enforce key-based authentication for SSH.\n");
            response.append("3. **Rate Limit SSH**: Implement fail2ban to block repeated failed logins.\n\n");

            response.append("### 🔍 Investigation\n");
            response.append("```bash\n");
            response.append("# Check SSH authentication logs\n");
            response.append("grep \"Failed password\" /var/log/auth.log | grep " + sourceIp + "\n");
            response.append("```");
        } else {
            response.append("**Threat Detected**: Anomalous Network Activity\n\n");
            response.append("Traffic patterns from **" + sourceIp + "** to **" + destIp
                    + "** deviate from established baselines. ");
            response.append("Signature: **" + signature + "**.\n\n");

            response.append("### ⚠️ Impact Assessment\n");
            response.append("* **Severity**: Requires Manual Triage\n");
            response.append("* **Risk**: Undetermined. Caution advised.\n\n");

            response.append("### 🛠️ Remediation Steps\n");
            response.append("1. **Isolate**: Temporarily quarantine the affected host **" + destIp + "**.\n");
            response.append("2. **Analyze Payload**: Review the full packet capture for this alert.\n");
            response.append("3. **Update Signatures**: Ensure IDS signatures are up to date.\n");
        }

        response.append("\n\n*Generated by AI Security Assistant (Fallback Mode)*");

        return response.toString();
    }

    private String extractValue(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Unknown";
    }
}
