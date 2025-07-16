package com.tournament.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la solicitud de creación de torneos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTournamentRequest {

    @NotBlank(message = "El nombre del torneo es obligatorio")
    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    private String name;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    @NotNull(message = "El tipo de juego es obligatorio")
    private Long gameTypeId;

    @NotNull(message = "El organizador es obligatorio")
    private Long organizerId;

    @NotNull(message = "Debe especificar si el torneo es gratuito")
    private Boolean isFree;

    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @DecimalMax(value = "9999.99", message = "El precio no puede exceder 9999.99")
    private BigDecimal price;

    @NotNull(message = "El número máximo de participantes es obligatorio")
    @Min(value = 2, message = "Debe tener al menos 2 participantes")
    @Max(value = 1000, message = "No puede exceder 1000 participantes")
    private Integer maxParticipants;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Future(message = "La fecha de inicio debe ser futura")
    private LocalDateTime startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Future(message = "La fecha de fin debe ser futura")
    private LocalDateTime endDate;

    @DecimalMin(value = "0.0", message = "La comisión no puede ser negativa")
    @DecimalMax(value = "100.0", message = "La comisión no puede exceder 100%")
    private BigDecimal commissionPercentage;

    /**
     * Valida que la fecha de fin sea posterior a la fecha de inicio
     * @return true si las fechas son válidas
     */
    public boolean isValidDateRange() {
        return startDate != null && endDate != null && endDate.isAfter(startDate);
    }

    /**
     * Valida que el precio sea requerido para torneos pagados
     * @return true si el precio es válido
     */
    public boolean isValidPrice() {
        if (Boolean.TRUE.equals(isFree)) {
            return price == null || price.compareTo(BigDecimal.ZERO) == 0;
        } else {
            return price != null && price.compareTo(BigDecimal.ZERO) > 0;
        }
    }
} 