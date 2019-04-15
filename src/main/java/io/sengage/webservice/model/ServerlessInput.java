package io.sengage.webservice.model;

import java.util.Map;

import lombok.Data;

@Data
public class ServerlessInput {

    private String resource;
    private String path;
    private String httpMethod;
    private Map<String, String> headers;
    private Map<String, String> queryStringParameters;
    private Map<String, String> pathParameters;
    private Map<String, String> stageVariables;
    private String body;
    private RequestContext requestContext;
    private Boolean isBase64Encoded;


    @Data
    public static class RequestContext {
        private String accountId;
        private String resourceId;
        private String stage;
        private String requestId;
        private Map<String, String> identity;
        private String resourcePath;
        private String httpMethod;
        private String apiId;
    }
}