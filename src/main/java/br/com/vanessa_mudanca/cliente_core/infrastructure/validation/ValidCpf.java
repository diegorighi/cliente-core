package br.com.vanessa_mudanca.cliente_core.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para validação de CPF (Cadastro de Pessoas Físicas).
 *
 * Valida formato, sequências inválidas e dígitos verificadores.
 *
 * Exemplo de uso:
 * <pre>
 * public record CreateClientePFRequest(
 *     {@literal @}ValidCpf(message = "CPF inválido")
 *     String cpf
 * ) {}
 * </pre>
 *
 * @see CpfValidator
 * @since 1.0.0
 */
@Documented
@Constraint(validatedBy = CpfValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCpf {

    String message() default "CPF inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
