package io.sengage.webservice.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerlessOutput {
    private Integer statusCode;
    private Map<String, String> headers;
    private String body;
}