package config;

import com.fasterxml.jackson.databind.ObjectMapper;
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

public class ObjectFactory {

    HashMap<String, Object> config;

    public ObjectFactory(String configurationFile) throws ConfigurationException {
        try {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            if (configurationFile == null) {
                configurationFile = "src/main/resources/test/scheduler.json";
                configurationFile = "test/scheduler.json";
            }
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(configurationFile);
            config = new ObjectMapper().readValue(inputStream, HashMap.class);
        } catch (Exception e) {
            throw new ConfigurationException("Initial configuration failed... Check configuration. Caused by:" + e);
        }
    }

    RequestManager requestManager;
    TableSource tableSource;
    JobManager jobManager;
    Scheduler scheduler;
    JdbcTableSource jdbcQuery;

    public RequestManager getRequestManager() throws ConfigurationException {
        if (requestManager == null) {
            if (config.containsKey("requestManager") && ((HashMap)config.get("requestManager")).containsKey("cacheTTLMins")) {
                requestManager = new RequestManager((Integer) ((HashMap)config.get("requestManager")).get("cacheTTLMins"));
            } else {
                requestManager = new RequestManager();
            }
            requestManager.setCacheManager(getTableSource());
            requestManager.setJobManager(getJobManager());
            requestManager.setScheduler(getScheduler());
        }
        return requestManager;
    }

    public TableSource getTableSource() throws ConfigurationException {
        if (tableSource == null) {
            if (config.containsKey("jdbcCache")) {
                tableSource = getJdbcTableSource();
            } else {
                tableSource = new InMemoryTableSource();
            }
        }
        return tableSource;
    }

    public JobManager getJobManager(){
        if (jobManager == null){
            jobManager = new JobManager();
            HashMap<String, Object> jobsMap = (HashMap<String, Object>) config.get("jobs");
            for (String key: jobsMap.keySet()){
                HashMap<String, Object> jobMap = (HashMap<String, Object>) jobsMap.get(key);
                jobManager.addJob(key, jobMap);
            }
        }
        return jobManager;
    }

    public Scheduler getScheduler() throws ConfigurationException {
        if (scheduler == null) {
            scheduler = new Scheduler();
            scheduler.setCacheManager(getTableSource());
            scheduler.setJobManager(getJobManager());
        }
        return scheduler;
    }

    public JdbcTableSource getJdbcTableSource() throws ConfigurationException {
        try {
            if (jdbcQuery == null) {
                HashMap<String, Object> cacheConfig = (HashMap<String, Object>) config.get("cacheConfig");
                DriverManagerDataSource ds = new DriverManagerDataSource();
                ds.setDriverClassName((String) cacheConfig.get("driver"));
                ds.setUrl((String) cacheConfig.get("url"));
                ds.setUsername((String) cacheConfig.get("username"));
                ds.setPassword((String) cacheConfig.get("password"));
                JdbcTemplate jdbcTemplate = new JdbcTemplate();
                jdbcTemplate.setDataSource(ds);
                jdbcQuery = new JdbcTableSource();
                jdbcQuery.setDatabaseName((String) cacheConfig.get("database"));
                jdbcQuery.setJdbcTemplateObject(jdbcTemplate);
            }
            return jdbcQuery;
        } catch (Exception e) {
            throw new ConfigurationException("Failed to create cache object... Check configuration. Caused by:" + e);
        }
    }
}
