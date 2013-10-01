package com.github.styx.domain;

public class Service extends Identifiable {

    private final String provider;
    private final String url;
    private final String description;
    private final String version;

    public Service(String id, String name, String provider, String url, String description, String version) {
        super(id, name);
        this.provider = provider;
        this.url = url;
        this.description = description;
        this.version = version;
    }

    public String getProvider() {
        return provider;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

}
