package scheduler.service;

import scheduler.executors.JobExecutor;
import scheduler.util.table.dao.TableSource;
import scheduler.util.table.model.Table;
import scheduler.model.Request;

import java.util.HashMap;

public class Scheduler {

    public void setJobManager(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    public void setCacheManager(TableSource cacheManager) {
        this.cacheManager = cacheManager;
    }

    JobManager jobManager;
    TableSource cacheManager;

    public Table run(Request runRequest){
        try {
            JobExecutor jobExecutor = jobManager.getJob(runRequest.getTarget());
            if (jobExecutor != null) {
                return jobExecutor.execute(runRequest.getArgs(), runRequest.isOutputCapture());
            }
            runRequest.complete();
        } catch (Exception e) {

        }
        return null;
    }

    public boolean submit(Request submitRequest) {
        try {
            Delegator delegator = new Delegator(submitRequest);
            delegator.start();
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    private class Delegator extends Thread {

        Request request;

        public Delegator(Request request) {
            this.request = request;
        }

        public void run(){
            try {
                request.progress();
                JobExecutor jobExecutor = jobManager.getJob(request.getTarget());
                if (jobExecutor != null) {
                    HashMap<String, Object> args = request.getArgs();
                    if (request.getDumpCacheName() != null) {
                        if (args == null) {
                            args = new HashMap<>();
                        }
                        args.put("cacheTable", request.getDumpCacheName());
                    }
                    if (request.isOutputCapture() != null) {
                        Table outputTable = jobExecutor.execute(args, true);
                        cacheManager.insertTable(outputTable, request.getOutputCacheName());
                    } else {
                        jobExecutor.execute(args, false);
                    }
                    request.complete();
                }
            } catch (Exception e) {
                request.failed();
                e.printStackTrace();
            }
        }
    }

}
