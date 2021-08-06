package scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class ConfigProperties {

    private Integer cacheTTLMins = 60;
    private CacheJdbcConfig cacheJdbcConfig;
    private CacheSqliteConfig cacheSqliteConfig;
    private List<JobsConfig> jobsConfigList;
    private Boolean inMemoryCache = false;

    public List<JobsConfig> getJobsConfigList() {
        return jobsConfigList;
    }

    public void setJobsConfigList(List<JobsConfig> jobsConfigList) {
        this.jobsConfigList = jobsConfigList;
    }

    public CacheJdbcConfig getCacheJdbcConfig() {
        return cacheJdbcConfig;
    }

    public void setCacheJdbcConfig(CacheJdbcConfig cacheJdbcConfig) {
        this.cacheJdbcConfig = cacheJdbcConfig;
    }

    public Integer getCacheTTLMins() {
        return cacheTTLMins;
    }

    public void setCacheTTLMins(Integer cacheTTLMins) {
        this.cacheTTLMins = cacheTTLMins;
    }

    public Boolean getInMemoryCache() {
        return inMemoryCache;
    }

    public void setInMemoryCache(Boolean inMemoryCache) {
        this.inMemoryCache = inMemoryCache;
    }

    public CacheSqliteConfig getCacheSqliteConfig() {
        return cacheSqliteConfig;
    }

    public void setCacheSqliteConfig(CacheSqliteConfig cacheSqliteConfig) {
        this.cacheSqliteConfig = cacheSqliteConfig;
    }

    public class JobsConfig {
        String name;
        String type;
        HashMap config;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public HashMap getConfig() {
            return config;
        }

        public void setConfig(HashMap config) {
            this.config = config;
        }
    }

    public class CacheJdbcConfig {
        String driver = null;
        String url = null;
        String password = null;
        String user = null;
        String databaseName = null;

        public String getDriver() {
            return driver;
        }

        public void setDriver(String driver) {
            this.driver = driver;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }
    }

    public class CacheSqliteConfig {
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Integer getTimeoutMillis() {
            return timeoutMillis;
        }

        public void setTimeoutMillis(Integer timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
        }

        public Integer getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(Integer batchSize) {
            this.batchSize = batchSize;
        }

        String path = null;
        Integer timeoutMillis = null;
        Integer batchSize = null;
    }

}
