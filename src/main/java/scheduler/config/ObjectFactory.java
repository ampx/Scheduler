package scheduler.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import scheduler.service.ExecutorManager;
import scheduler.service.RequestManager;
import scheduler.service.Scheduler;
import scheduler.util.table.dao.InMemoryTableSource;
import scheduler.util.table.dao.JdbcTableSource;
import scheduler.util.table.dao.TableSource;

import javax.naming.ConfigurationException;
import java.util.List;

@Configuration
public class ObjectFactory {

    @Autowired
    ConfigProperties configProperties;

    @Bean
    @Autowired
    public RequestManager requestManager(TableSource tableSource, Scheduler scheduler, ExecutorManager executorManager) throws ConfigurationException {
        RequestManager requestManager = new RequestManager(configProperties.getCacheTTLMins());
        requestManager.setCacheManager(tableSource);
        requestManager.setJobManager(executorManager);
        requestManager.setScheduler(scheduler);
        return requestManager;
    }

    @Bean
    public TableSource getTableSource() throws ConfigurationException {
        TableSource tableSource;
        tableSource = getJdbcTableSource();
        if (tableSource == null) {
            tableSource = new InMemoryTableSource();
        }
        return tableSource;
    }

    @Bean
    @Qualifier("externalExecutorManager")
    public ExecutorManager getJobManager(ExecutorManager externalExecutorManager) {
        ExecutorManager executorManager = new ExecutorManager();
        executorManager.setExternalExecutorManager(externalExecutorManager);
        List<ConfigProperties.JobsConfig> jobsConfigList = configProperties.getJobsConfigList();
        if (jobsConfigList != null && jobsConfigList.size() > 0){
            for (ConfigProperties.JobsConfig config : jobsConfigList) {
                executorManager.addExecutor(config.getName(), config.getType(), config.getConfig());
            }
        }
        return executorManager;
    }

    @Bean
    @Autowired
    public Scheduler getScheduler(TableSource tableSource, ExecutorManager executorManager) throws ConfigurationException {
        Scheduler scheduler = new Scheduler();
        scheduler.setCacheManager(tableSource);
        scheduler.setJobManager(executorManager);
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
