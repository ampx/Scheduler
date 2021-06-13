package service;

import executors.JobExecutor;
import executors.ProcessExecutor;
import executors.UriJobExecutor;

import java.util.*;

public class JobManager {
    HashMap<String, JobExecutor> jobs = new HashMap<>();

    public void setExternalJobManager(JobManager externalJobManager) {
        this.externalJobManager = externalJobManager;
    }

    JobManager externalJobManager;

    public JobExecutor addJob(String executorName, HashMap<String, Object> config){
        JobExecutor jobExecutor = null;
        if (externalJobManager != null) {
            jobExecutor = externalJobManager.addJob(executorName, config);
        }
        if (config.get("type").equals("get")) {
            jobExecutor = new UriJobExecutor(config);
        } else if (config.get("type").equals("process")) {
            jobExecutor = new ProcessExecutor(config);
        }
        if (jobExecutor != null) {
            jobs.put(executorName, jobExecutor);
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
