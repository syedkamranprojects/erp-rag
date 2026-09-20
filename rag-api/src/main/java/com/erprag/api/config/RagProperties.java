package com.erprag.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    private Storage storage = new Storage();
    private Admin admin = new Admin();
    private Jwt jwt = new Jwt();
    private Chunking chunking = new Chunking();
    private Retrieval retrieval = new Retrieval();
    private ChatMemory chatMemory = new ChatMemory();
    private Db db = new Db();
    private Llm llm = new Llm();
    private Embedding embedding = new Embedding();
    private Cors cors = new Cors();

    public Storage getStorage() {
        return storage;
    }

    public Admin getAdmin() {
        return admin;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public Chunking getChunking() {
        return chunking;
    }

    public Retrieval getRetrieval() {
        return retrieval;
    }

    public ChatMemory getChatMemory() {
        return chatMemory;
    }

    public Db getDb() {
        return db;
    }

    public Llm getLlm() {
        return llm;
    }

    public Embedding getEmbedding() {
        return embedding;
    }

    public Cors getCors() {
        return cors;
    }

    public static class Cors {
        private java.util.List<String> allowedOrigins = java.util.List.of("http://localhost:5173");

        public java.util.List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(java.util.List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Storage {
        private String basePath;

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }

    public static class Admin {
        private String apiKey;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    public static class Jwt {
        private String secret;
        private int expiryMinutes;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public int getExpiryMinutes() {
            return expiryMinutes;
        }

        public void setExpiryMinutes(int expiryMinutes) {
            this.expiryMinutes = expiryMinutes;
        }
    }

    public static class Chunking {
        private int maxSegmentSizeChars = 1000;
        private int maxOverlapChars = 100;

        public int getMaxSegmentSizeChars() {
            return maxSegmentSizeChars;
        }

        public void setMaxSegmentSizeChars(int maxSegmentSizeChars) {
            this.maxSegmentSizeChars = maxSegmentSizeChars;
        }

        public int getMaxOverlapChars() {
            return maxOverlapChars;
        }

        public void setMaxOverlapChars(int maxOverlapChars) {
            this.maxOverlapChars = maxOverlapChars;
        }
    }

    public static class Retrieval {
        private int maxResults = 5;
        private double minScore = 0.6;

        public int getMaxResults() {
            return maxResults;
        }

        public void setMaxResults(int maxResults) {
            this.maxResults = maxResults;
        }

        public double getMinScore() {
            return minScore;
        }

        public void setMinScore(double minScore) {
            this.minScore = minScore;
        }
    }

    public static class ChatMemory {
        private int maxMessages = 20;

        public int getMaxMessages() {
            return maxMessages;
        }

        public void setMaxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
        }
    }

    public static class Db {
        private String host;
        private int port;
        private String database;
        private String user;
        private String password;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Llm {
        private String provider = "openai";
        @NestedConfigurationProperty
        private ProviderSection openai = new ProviderSection();
        @NestedConfigurationProperty
        private ProviderSection anthropic = new ProviderSection();
        @NestedConfigurationProperty
        private ProviderSection azureOpenai = new ProviderSection();
        @NestedConfigurationProperty
        private ProviderSection ollama = new ProviderSection();

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public ProviderSection getOpenai() {
            return openai;
        }

        public ProviderSection getAnthropic() {
            return anthropic;
        }

        public ProviderSection getAzureOpenai() {
            return azureOpenai;
        }

        public ProviderSection getOllama() {
            return ollama;
        }
    }

    public static class Embedding {
        private String provider = "openai";
        @NestedConfigurationProperty
        private ProviderSection openai = new ProviderSection();
        @NestedConfigurationProperty
        private ProviderSection azureOpenai = new ProviderSection();
        @NestedConfigurationProperty
        private ProviderSection ollama = new ProviderSection();

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public ProviderSection getOpenai() {
            return openai;
        }

        public ProviderSection getAzureOpenai() {
            return azureOpenai;
        }

        public ProviderSection getOllama() {
            return ollama;
        }
    }

    public static class ProviderSection {
        private String apiKey;
        private String model;
        private String endpoint;
        private String baseUrl;
        private String deploymentName;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getDeploymentName() {
            return deploymentName;
        }

        public void setDeploymentName(String deploymentName) {
            this.deploymentName = deploymentName;
        }
    }
}
