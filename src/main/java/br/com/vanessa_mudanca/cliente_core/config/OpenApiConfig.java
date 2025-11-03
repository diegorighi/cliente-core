package br.com.vanessa_mudanca.cliente_core.config;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI apiInfo() {
		return new OpenAPI()
				.info(new Info()
						.title("Cliente Core API")
						.description("API para gestao de clientes do sistema Va Nessa Mudanca.")
						.version("v1")
						.contact(new Contact()
								.name("Equipe Vanessa Mudanca")
								.email("contato@vanessamudanca.com.br")));
	}

	@Bean
	public OpenApiCustomizer openApiCustomizer() {
		return openApi -> {
			// Configura para ignorar problemas com tipos genéricos em Records
			openApi.getComponents().getSchemas().values().forEach(schema -> {
				if (schema.get$ref() != null && schema.get$ref().contains("PageResponse")) {
					schema.set$ref(null);
				}
			});
		};
	}
}
