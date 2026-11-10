
package cl.oxman.vehicle_api.service;

import cl.oxman.vehicle_api.dto.VehiculoResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class VehiculoService {

    // Cliente HTTP para comunicarnos con la API externa
    private final RestClient restClient = RestClient.create(
            "https://vpic.nhtsa.dot.gov/api"
    );

    public VehiculoResponse consultarVehiculo(String vin, int anio) {

        Map<?, ?> respuesta;

        // 1. CONSULTAR LA API EXTERNA
        try {
            respuesta = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/vehicles/DecodeVinValues/{vin}")
                            .queryParam("format", "json")
                            .queryParam("modelyear", anio)
                            .build(vin))
                    .retrieve()
                    .body(Map.class);

        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Error al consultar la API externa",
                    e
            );
        }

        // 2. VALIDAR LA RESPUESTA
        if (respuesta == null ||
                !(respuesta.get("Results") instanceof List<?> resultados) ||
                resultados.isEmpty() ||
                !(resultados.get(0) instanceof Map<?, ?> datos)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Respuesta invalida del proveedor"
            );
        }

        // 3. EXTRAER LOS DATOS
        String marca = obtenerTexto(datos.get("Make"));

        if (marca.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontraron datos del vehiculo"
            );
        }

        // 4. CONSTRUIR NUESTRO DTO UTILIZANDO LOMBOK
        return VehiculoResponse.builder()
                .vin(vin)
                .marca(marca)
                .modelo(obtenerTexto(datos.get("Model")))
                .anio(obtenerTexto(datos.get("ModelYear")))
                .tipo(obtenerTexto(datos.get("VehicleType")))
                .build();
    }

    private String obtenerTexto(Object valor) {
        return valor == null ? "" : valor.toString();
    }
}
