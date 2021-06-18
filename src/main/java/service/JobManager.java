package service;

import executors.DummyExecutor;
import executors.JobExecutor;
import executors.ProcessExecutor;
import executors.UriJobExecutor;

import java.util.*;

public class JobManager {

    public JobManager() {
        jobs.put("test", new DummyExecutor(new HashMap<>()));
    }

    HashMap<String, JobExecutor> jobs = new HashMap<>();

    public void setExternalJobManager(JobManager externalJobManager) {
        this.externalJobManager = externalJobManager;
    }

    JobManager externalJobManager;

    public boolean addJob(String executorName, String type, HashMap<String, Object> config){
        JobExecutor jobExecutor = createExecutor(type, config);
        if (jobExecutor != null) {
            jobs.put(executorName, jobExecutor);
            return true;
        }
        return false;
    }

    public JobExecutor createExecutor(String type, HashMap<String, Object> config) {
        JobExecutor jobExecutor = null;
        if (externalJobManager != null) {
            jobExecutor = externalJobManager.createExecutor(type, config);
        }
        if (jobExecutor == null) {
            if (type.equals("get")) {
                jobExecutor = new UriJobExecutor(config);
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
    public JobExecutor getJob(String jobName){
        return jobs.get(jobName);
    }

    public boolean processPermission(String user, String jobName){
        JobExecutor jobProcessing = getJob(jobName);
        if (jobProcessing != null && jobProcessing.canExecute(user)) {
            return true;
        }
        return false;
    }

    public boolean readPermission(String user, String jobName){
        JobExecutor jobProcessing = getJob(jobName);
        if (jobProcessing != null && jobProcessing.canRead(user)) {
            return true;
        }
        return false;
    }

}
