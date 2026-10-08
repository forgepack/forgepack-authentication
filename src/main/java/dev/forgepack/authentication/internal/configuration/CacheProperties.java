package dev.forgepack.authentication.internal.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Propriedades de configuração da infraestrutura de cache da biblioteca.
 *
 * <p>Permite que projetos consumidores sobrescrevam os valores padrão de TTL,
 * refresh, capacidade inicial e tamanho máximo via {@code application.properties}
 * sob o prefixo {@code forgepack.cache}.</p>
 *
 * <p><strong>Restrição:</strong> {@code refresh} deve ser estritamente menor que {@code ttl};
 * caso contrário, o Caffeine lança {@link IllegalArgumentException} na inicialização.</p>
 *
 * @param ttl             Tempo de expiração após a escrita. Padrão: 1 hora.
 * @param refresh         Tempo para refresh assíncrono antes da expiração. Deve ser &lt; {@code ttl}. Padrão: 45 minutos.
 * @param initialCapacity Capacidade inicial do cache (mínimo: 1). Padrão: 100.
 * @param maximumSize     Tamanho máximo de entradas no cache (mínimo: 1). Padrão: 5000.
 *
 * @author Marcelo Ribeiro Gadelha
 * @since 1.0
 */
@Validated
@ConfigurationProperties(prefix = "forgepack.cache")
public record CacheProperties(
        @NotNull @DefaultValue("1h")   Duration ttl,
        @NotNull @DefaultValue("45m")  Duration refresh,
        @Min(1)  @DefaultValue("100")  int      initialCapacity,
        @Min(1)  @DefaultValue("5000") int      maximumSize
) {
    @ConstructorBinding
    public CacheProperties {
        if (refresh.compareTo(ttl) >= 0) {
            throw new IllegalArgumentException(
                    "forgepack.cache.refresh (" + refresh + ") must be less than forgepack.cache.ttl (" + ttl + ")");
        }
    }

    public CacheProperties() {
        this(
                Duration.ofHours(1),
                Duration.ofMinutes(45),
                100,
                5000
        );
    }
}