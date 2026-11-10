package cl.oxman.vehicle_api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoResponse {
    private String vin;
    private String marca;
    private String modelo;
    private String anio;
    private String tipo;
}
