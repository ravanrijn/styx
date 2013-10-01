package com.github.styx.domain;

public class Instance {

    private final String id;
    private final InstanceState state;
    private final String since;
    private final String consoleIp;
    private final int consolePort;

    public Instance(String id, InstanceState state, String since, String consoleIp, int consolePort) {
        this.id = id;
        this.state = state;
        this.since = since;
        this.consoleIp = consoleIp;
        this.consolePort = consolePort;
    }

    public String getId() {
        return id;
    }

    public InstanceState getState() {
        return state;
    }

    public String getSince() {
        return since;
    }

    public String getConsoleIp() {
        return consoleIp;
    }

    public int getConsolePort() {
        return consolePort;
    }

}
