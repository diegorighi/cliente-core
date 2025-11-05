package br.com.vanessa_mudanca.cliente_core.infrastructure.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

/**
 * Inicializador da tabela DynamoDB para cache.
 * <p>
 * Cria a tabela `cliente-core-cache` automaticamente no startup se não existir.
 * Útil para ambiente de desenvolvimento (DynamoDB Local).
 * </p>
 *
 * <h3>Table Schema:</h3>
 * <pre>
 * Table: cliente-core-cache
 * Partition Key: cacheKey (String)
 * TTL Attribute: expirationTime (Number - Unix timestamp)
 * Billing Mode: PAY_PER_REQUEST (Free Tier friendly)
 * </pre>
 *
 * <h3>AWS Free Tier:</h3>
 * <ul>
 *   <li>25 GB storage (permanente)</li>
 *   <li>25 WCU = 25 escritas/segundo</li>
 *   <li>25 RCU = 100 leituras/segundo (4 KB cada)</li>
 * </ul>
 *
 * <p>
 * <b>Nota:</b> Este componente só é carregado quando `cache.backend=dynamodb`.
 * Nos testes (test/integration profiles), o cache é desabilitado (`cache.backend: none`)
 * e este inicializador não será carregado.
 * </p>
 */
@Component
@ConditionalOnProperty(name = "cache.backend", havingValue = "dynamodb")
public class DynamoDbTableInitializer {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbTableInitializer.class);

    private final DynamoDbClient dynamoDbClient;

    @Value("${cache.dynamodb.table-name:cliente-core-cache}")
    private String tableName;

    private static final String ATTR_CACHE_KEY = "cacheKey";
    private static final String ATTR_EXPIRATION_TIME = "expirationTime";

    public DynamoDbTableInitializer(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Cria tabela DynamoDB após aplicação estar pronta.
     * <p>
     * Verifica se tabela existe antes de criar (idempotente).
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void createTableIfNotExists() {
        try {
            // Verificar se tabela já existe
            DescribeTableRequest describeRequest = DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build();

            dynamoDbClient.describeTable(describeRequest);
            log.info("DynamoDB cache table already exists: {}", tableName);

        } catch (ResourceNotFoundException e) {
            // Tabela não existe - criar
            createTable();

        } catch (Exception e) {
            log.error("Error checking DynamoDB table existence", e);
        }
    }

    /**
     * Cria tabela DynamoDB com schema de cache.
     */
    private void createTable() {
        try {
            log.info("Creating DynamoDB cache table: {}", tableName);

            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName(ATTR_CACHE_KEY)
                                    .keyType(KeyType.HASH)  // Partition key
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName(ATTR_CACHE_KEY)
                                    .attributeType(ScalarAttributeType.S)  // String
                                    .build()
                    )
                    .billingMode(BillingMode.PAY_PER_REQUEST)  // Free Tier friendly
                    .build();

            dynamoDbClient.createTable(request);

            // Aguardar tabela ficar ativa
            waitForTableActive();

            // Configurar TTL
            enableTtl();

            log.info("DynamoDB cache table created successfully: {}", tableName);

        } catch (InterruptedException e) {
            // Restore interrupted status
            Thread.currentThread().interrupt();
            log.error("Table creation interrupted", e);
        } catch (Exception e) {
            log.error("Error creating DynamoDB table", e);
        }
    }

    /**
     * Aguarda tabela DynamoDB ficar ativa.
     */
    private void waitForTableActive() throws InterruptedException {
        log.info("Waiting for table to become active...");

        for (int i = 0; i < 30; i++) {  // Max 30 tentativas (30 segundos)
            try {
                DescribeTableRequest request = DescribeTableRequest.builder()
                        .tableName(tableName)
                        .build();

                DescribeTableResponse response = dynamoDbClient.describeTable(request);

                if (response.table().tableStatus() == TableStatus.ACTIVE) {
                    log.info("Table is now active");
                    return;
                }

                Thread.sleep(1000);  // Aguardar 1 segundo

            } catch (InterruptedException e) {
                // Restore interrupted status
                Thread.currentThread().interrupt();
                log.warn("Table status check interrupted: {}", e.getMessage());
                break;  // Exit loop if interrupted
            } catch (Exception e) {
                log.warn("Error checking table status: {}", e.getMessage());
            }
        }

        log.warn("Table did not become active within 30 seconds");
    }

    /**
     * Habilita TTL (Time-To-Live) na tabela DynamoDB.
     * <p>
     * TTL deleta itens expirados automaticamente (background job).
     * Economia de custo: deletions via TTL são gratuitas.
     * </p>
     */
    private void enableTtl() {
        try {
            UpdateTimeToLiveRequest request = UpdateTimeToLiveRequest.builder()
                    .tableName(tableName)
                    .timeToLiveSpecification(
                            TimeToLiveSpecification.builder()
                                    .enabled(true)
                                    .attributeName(ATTR_EXPIRATION_TIME)
                                    .build()
                    )
                    .build();

            dynamoDbClient.updateTimeToLive(request);

            log.info("TTL enabled on attribute: {}", ATTR_EXPIRATION_TIME);

        } catch (Exception e) {
            log.error("Error enabling TTL", e);
        }
    }
}
