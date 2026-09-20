package com.erprag.core.config;

public record DbConfig(
        String host,
        int port,
        String database,
        String user,
        String password
) {
    public String jdbcUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + database;
    }
}
