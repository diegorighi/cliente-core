package br.com.vanessa_mudanca.cliente_core.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.sql.DataSource;

/**
 * AWS Secrets Manager Configuration
 *
 * CRITICAL: This configuration replaces hardcoded credentials in application.yml
 *
 * How it works:
 * 1. Application starts in staging/prod environment
 * 2. This config reads the secret name from spring.cloud.aws.secretsmanager.secret-name
 * 3. AWS SDK fetches the secret using IAM role (no credentials hardcoded!)
 * 4. Secret JSON is parsed and injected into DataSource
 *
 * Secrets JSON format (must match Terraform output):
 * {
 *   "host": "cliente-core-prod.abc123.us-east-1.rds.amazonaws.com",
 *   "port": 5432,
 *   "database": "vanessa_mudanca_clientes",
 *   "username": "app_user",
 *   "password": "secure-password-from-secrets-manager",
 *   "url": "jdbc:postgresql://host:port/database"
 * }
 *
 * Environment Requirements:
 * - AWS_REGION environment variable (e.g., us-east-1)
 * - IAM role with secretsmanager:GetSecretValue permission
 * - Secret exists in AWS Secrets Manager with correct name
 *
 * Local Development:
 * - Profile: dev (uses application-dev.yml with localhost credentials)
 * - This config is DISABLED in dev profile
 *
 * Production/Staging:
 * - Profile: prod or staging
 * - This config is ENABLED
 * - Fetches credentials from AWS Secrets Manager
 *
 * @see <a href="../../terraform/secrets-manager/main.tf">Terraform configuration</a>
 */
@Configuration
@Profile({"prod", "staging"})
@ConditionalOnProperty(name = "spring.cloud.aws.secretsmanager.enabled", havingValue = "true", matchIfMissing = true)
public class AwsSecretsManagerConfig {

    private static final Logger log = LoggerFactory.getLogger(AwsSecretsManagerConfig.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates AWS Secrets Manager client with default credentials provider
     *
     * Credentials chain (in order):
     * 1. Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
     * 2. System properties
     * 3. IAM role for ECS task or EC2 instance (RECOMMENDED for production)
     * 4. ~/.aws/credentials file
     *
     * CRITICAL: NEVER hardcode credentials here! Use IAM roles.
     */
    @Bean
    public SecretsManagerClient secretsManagerClient() {
        String region = System.getenv("AWS_REGION");
        if (region == null || region.isEmpty()) {
            region = "us-east-1"; // Default fallback
            log.warn("AWS_REGION not set, using default: {}", region);
        }

        log.info("Initializing AWS Secrets Manager client in region: {}", region);

        return SecretsManagerClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Creates DataSource from AWS Secrets Manager credentials
     *
     * This bean REPLACES the default DataSource created by Spring Boot's auto-configuration
     *
     * Secret name format: cliente-core/{environment}/database
     * - Example: cliente-core/prod/database
     * - Example: cliente-core/staging/database
     *
     * @throws RuntimeException if secret cannot be fetched or parsed
     */
    @Bean
    @Primary
    public DataSource dataSource(SecretsManagerClient secretsManagerClient) {
        String secretName = System.getenv("AWS_SECRETS_NAME");
        if (secretName == null || secretName.isEmpty()) {
            // Fallback to profile-based name
            String profile = System.getProperty("spring.profiles.active", "prod");
            secretName = "cliente-core/" + profile + "/database";
            log.warn("AWS_SECRETS_NAME not set, using default: {}", secretName);
        }

        log.info("Fetching database credentials from AWS Secrets Manager: {}", secretName);

        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
            String secretString = response.secretString();

            if (secretString == null || secretString.isEmpty()) {
                throw new IllegalStateException("Secret is empty: " + secretName);
            }

            // Parse JSON secret
            JsonNode secretJson = objectMapper.readTree(secretString);

            String url = secretJson.get("url").asText();
            String username = secretJson.get("username").asText();
            String password = secretJson.get("password").asText();

            log.info("Successfully fetched database credentials from Secrets Manager");
            log.info("Database URL: {}", maskUrl(url)); // Mask password in URL if present

            // Build DataSource with fetched credentials
            return DataSourceBuilder.create()
                    .url(url)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();

        } catch (Exception e) {
            log.error("Failed to fetch database credentials from AWS Secrets Manager: {}", secretName, e);
            throw new RuntimeException("Cannot initialize DataSource - Secrets Manager error", e);
        }
    }

    /**
     * Masks password in JDBC URL for logging
     *
     * Example:
     * Input:  jdbc:postgresql://host:5432/db?password=secret123
     * Output: jdbc:postgresql://host:5432/db?password=***
     */
    private String maskUrl(String url) {
        if (url == null) {
            return "null";
        }
        return url.replaceAll("password=([^&]+)", "password=***");
    }
}
