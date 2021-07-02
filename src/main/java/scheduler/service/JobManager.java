package scheduler.service;

import scheduler.executors.*;

import java.util.*;

public class JobManager {

    public JobManager() {
        HashMap testConfig = new HashMap();
        testConfig.put("executeUsers", new ArrayList(){{add("writer");}});
        testConfig.put("readUsers", new ArrayList(){{add("reader");}});
        jobs.put("test", new DummyExecutor(testConfig));
    }

    HashMap<String, Executor> jobs = new HashMap<>();

    public boolean addExecutor(String executorName, String type, HashMap<String, Object> config){
        Executor jobExecutor = createExecutor(type, config);
        if (jobExecutor != null) {
            jobs.put(executorName, jobExecutor);
            return true;
        }
        return false;
    }

    public boolean addExecutor(String executorName, Executor executor) {
        if (executorName != null && executor != null) {
            jobs.put(executorName, executor);
            return true;
        }
        return false;
    }

    public Executor createExecutor(String type, HashMap<String, Object> config) {
        Executor jobExecutor = null;
        if (jobExecutor == null) {
            if (type.equals("get")) {
                jobExecutor = new UriExecutor(config);
            } else if (type.equals("process")) {
                jobExecutor = new ProcessExecutor(config);
            }
        }
        return jobExecutor;
    }

    public Set<String> getJobList() {
        return jobs.keySet();
    }

    public boolean jobExists(String jobName){
        if (jobs.containsKey(jobName)){
            return true;
        }
        return false;
    }
    public Executor getJob(String jobName){
        return jobs.get(jobName);
    }

    public boolean processPermission(String user, String jobName){
        Executor jobProcessing = getJob(jobName);
        if (jobProcessing != null && jobProcessing.canExecute(user)) {
            return true;
        }
        return false;
    }

    public boolean readPermission(String user, String jobName){
        Executor jobProcessing = getJob(jobName);
        if (jobProcessing != null && jobProcessing.canRead(user)) {
            return true;
        }
        return false;
    }

    public void addRequestMonitor(RequestManager requestManager) {
        HashMap requestConfig = new HashMap();
        requestConfig.put("executeUsers", new ArrayList(){{add("*");}});
        requestConfig.put("readUsers", new ArrayList(){{add("*");}});
        jobs.put("requests", new RequestMonitor(requestConfig, requestManager));
    }

}