package br.com.vanessa_mudanca.cliente_core.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para validação de CNPJ (Cadastro Nacional da Pessoa Jurídica).
 *
 * Valida formato, sequências inválidas e dígitos verificadores.
 *
 * Exemplo de uso:
 * <pre>
 * public record CreateClientePJRequest(
 *     {@literal @}ValidCnpj(message = "CNPJ inválido")
 *     String cnpj
 * ) {}
 * </pre>
 *
 * @see CnpjValidator
 * @since 1.0.0
 */
@Documented
@Constraint(validatedBy = CnpjValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCnpj {

    String message() default "CNPJ inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
