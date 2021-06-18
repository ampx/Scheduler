package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import service.JobManager;
import service.RequestManager;
import service.Scheduler;
import util.table.dao.InMemoryTableSource;
import util.table.dao.JdbcTableSource;
import util.table.dao.TableSource;

import javax.naming.ConfigurationException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class ObjectFactory {

    @Autowired
    ConfigProperties configProperties;

    @Bean
    public RequestManager getRequestManager() throws ConfigurationException {
        RequestManager requestManager = new RequestManager(configProperties.getCacheTTLMins());
        requestManager.setCacheManager(getTableSource());
        requestManager.setJobManager(getJobManager());
        requestManager.setScheduler(getScheduler());
        return requestManager;
    }

    public TableSource getTableSource() throws ConfigurationException {
        TableSource tableSource;
        tableSource = getJdbcTableSource();
        if (tableSource == null) {
            tableSource = new InMemoryTableSource();
        }
        return tableSource;
    }

    public JobManager getJobManager() {
        JobManager jobManager = new JobManager();
        List<ConfigProperties.JobsConfig> jobsConfigList = configProperties.getJobsConfigList();
        if (jobsConfigList != null && jobsConfigList.size() > 0){
            for (ConfigProperties.JobsConfig config : jobsConfigList) {
                jobManager.addJob(config.getName(), config.getType(), config.getConfig());
            }
        }
        return jobManager;
    }

    public Scheduler getScheduler() throws ConfigurationException {
        Scheduler scheduler = new Scheduler();
        scheduler.setCacheManager(getTableSource());
        scheduler.setJobManager(getJobManager());
        return scheduler;
    }

    public JdbcTableSource getJdbcTableSource() throws ConfigurationException {
        JdbcTableSource jdbcQuery = null;
        ConfigProperties.CacheJdbcConfig jdbcConfig = configProperties.getCacheJdbcConfig();
        if (jdbcConfig != null &&
            jdbcConfig.getUrl() != null &&
            jdbcConfig.getDriver() != null &&
            jdbcConfig.getDatabaseName() != null) {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName(jdbcConfig.driver);
            ds.setUrl(jdbcConfig.url);
            ds.setUsername(jdbcConfig.user);
            ds.setPassword(jdbcConfig.password);
            JdbcTemplate jdbcTemplate = new JdbcTemplate();
            jdbcTemplate.setDataSource(ds);
            jdbcQuery = new JdbcTableSource();
            jdbcQuery.setDatabaseName(jdbcConfig.databaseName);
            jdbcQuery.setJdbcTemplateObject(jdbcTemplate);
        }
        return jdbcQuery;
    }
}
