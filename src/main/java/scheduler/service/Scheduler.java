package scheduler.service;

import scheduler.executors.Executor;
import scheduler.util.table.dao.TableSource;
import scheduler.util.table.model.Table;
import scheduler.model.Request;

import java.util.HashMap;

public class Scheduler {

    public void setJobManager(ExecutorManager executorManager) {
        this.executorManager = executorManager;
    }

    public void setCacheManager(TableSource cacheManager) {
        this.cacheManager = cacheManager;
    }

    ExecutorManager executorManager;
    TableSource cacheManager;

    public Table run(Request runRequest){
        try {
            Executor jobExecutor = executorManager.getJob(runRequest.getTarget());
            if (jobExecutor != null) {
                return jobExecutor.execute(runRequest.getArgs(), null);
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
                Executor jobExecutor = executorManager.getJob(request.getTarget());
                if (jobExecutor != null) {
                    HashMap<String, Object> args = request.getArgs();
                    Table outputTable = jobExecutor.execute(args, request.getCacheName());
                    if (outputTable != null && request.getCacheName() != null) {
                        cacheManager.insertTable(outputTable, request.getCacheName());
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
