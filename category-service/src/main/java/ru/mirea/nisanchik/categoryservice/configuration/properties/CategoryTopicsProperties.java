package ru.mirea.nisanchik.categoryservice.configuration.properties;


import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "category-service.kafka.topics")
public record CategoryTopicsProperties (
    @NotBlank String categoryChange,
    @NotBlank String categoryDelete
) {

}
