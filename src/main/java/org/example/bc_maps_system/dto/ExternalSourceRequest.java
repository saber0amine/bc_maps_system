package org.example.bc_maps_system.dto;

public class ExternalSourceRequest {

    private String name;
    private String serverUrl;
    private String token;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getServerUrl() { return serverUrl; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}