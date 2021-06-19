package scheduler.service;

import scheduler.util.table.dao.TableSource;
import scheduler.util.table.logic.TableUtil;
import scheduler.util.table.model.Table;
import scheduler.util.time.model.Time;
import org.apache.commons.text.RandomStringGenerator;
import scheduler.model.Request;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;

public class RequestManager {
    ReadWriteLock lock = new ReentrantReadWriteLock();
    //key=JobName, value Job details
    HashMap<String, List<Request>> requestHistory = new HashMap<>();
    HashMap<String, HashMap<String, List<Request>>> labelledRequestHistory = new HashMap();
    String cacheTimestampPattern = "uuMMddHHmm";
    Integer cacheTTLMins = 1440;
    Timer timer;
    String defaultUser = "*";
    String defaultSource = "*";

    public RequestManager(){
        enableCleanup();
    }

    public RequestManager(Integer cacheTTLMins){
        this.cacheTTLMins = cacheTTLMins;
        enableCleanup();
    }

    public void setJobManager(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }


    public void setCacheManager(TableSource cacheManager) {
        this.cacheManager = cacheManager;
    }

    JobManager jobManager;
    Scheduler scheduler;
    //StatsCollector statsCollector;
    TableSource cacheManager;

    public boolean submitJob(Request request){
        try {
            if (jobManager.jobExists(request.getTarget())
                    && jobManager.processPermission(request.getUser(), request.getTarget())) {
                if (request.isDataDump()) {
                    request.setDumpCacheName(createCacheTableName(request.getRequestTime(), request.getTarget()));
                } else if (request.isOutputCapture()) {
                    request.setOutputCacheName(createCacheTableName(request.getRequestTime(), request.getTarget()));
                }
                if (saveRequest(request)) {
                    return scheduler.submit(request);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public Table getRequests(Request request){
        HashMap args = request.getArgs();
        if (args.containsKey("label") && !args.get("label").equals("*")) {
            Request originalReq = getUserRequest(request.getSource(), request.getUser(), request.getLabel());
            if (originalReq != null) {
                Table table = new Table(3);
                table.setHeaders(new String[]{"Target", "Status", "Submit time"});
                table.addRow(new String[]{originalReq.getTarget(), originalReq.getStatusString(),
                        originalReq.getRequestTime().mysqlString()});
                return TableUtil.objectToTable(originalReq);
            }
        } else {

        }
        return null;
    }

    public boolean deleteUserRequest(Request request){
        /*HashMap<String, Object> options = scheduler.request.getOptions();
        if (options.containsKey("delete")){
            String userRequest
        }*/
        return false;
    }

    //Find original submit scheduler.request
    //will return cached table if the submit scheduler.request has complete status and
    //cache result scheduler.request user has privilege to run original submit scheduler.request
    public Table getCachedResult(Request getRequest){
        Request submitRequest = null;
        if (getRequest.getLabel() != null && !getRequest.getLabel().equals("*")) {
            submitRequest = getUserRequest(getRequest.getSource(), getRequest.getUser(), getRequest.getLabel());
        } else {
            submitRequest = getJobRequest(getRequest.getTarget(), getRequest.getArgs());
        }
        if (submitRequest != null && submitRequest.isComplete()) {
            if (jobManager.jobExists(submitRequest.getTarget())
                    && jobManager.readPermission(getRequest.getUser(), submitRequest.getTarget())) {
                String cacheName = null;
                if (getRequest.isDataDump()) {
                    cacheName = submitRequest.getDumpCacheName();
                } else if (getRequest.isOutputCapture()) {
                    cacheName = submitRequest.getOutputCacheName();
                }
                if (cacheName != null) {
                    return cacheManager.getTable(cacheName);
                }
            }
        }
        return null;
    }

    public Table runJob(Request runRequest){
        try {
            if (jobManager.jobExists(runRequest.getTarget())
                    && jobManager.processPermission(runRequest.getUser(), runRequest.getTarget())) {
                return scheduler.run(runRequest);
            }
        } catch (Exception e) {

        }
        return null;
    }

    public Table systemRequest(Request request) {
        /*Table response = null;
        if (jobManager.processPermission(scheduler.request.getRequestUser(), "admin")) {
            HashMap<String, Object> options = scheduler.request.getOptions();
            if (scheduler.request.isRequestsRequest()) {
                if (options.containsKey("top")) {
                    List requests = getLongRunningRequest((Integer) options.get("top"));
                    response = TableUtil.toTable(requests);
                } else if (options.containsKey("status")) {
                    List requests = getRequestsByStatus(Job.Status.valueOf((String) options.get("status")));
                    response = TableUtil.toTable(requests);
                }
            } else if (scheduler.request.isMetricsRequest()) {
                if (options.containsKey("metric_name")) {
                    response = statsCollector.getStats((String) options.get("metric_name"));
                } else {
                    response = statsCollector.getStatsList();
                }
            }
        }
        return response;*/return null;
    }

    public List getUserRequestLabels(String user, String source) {
        List<String> userRequestNames = null;
        if (user == null) user = defaultUser;
        if (source == null) source = defaultSource;
        if (labelledRequestHistory.containsKey(source) && labelledRequestHistory.get(source).containsKey(user)) {
            List<Request> requests = labelledRequestHistory.get(source).get(user);
            if (requests != null && requests.size()>0) {
                userRequestNames = new ArrayList<>(requests.size());
                for (Request request : requests) {
                    userRequestNames.add(request.getLabel());
                }
            }
        }
        return userRequestNames;
    }
    
    public boolean saveRequest(Request request){
        String source = request.getSource();
        String user = request.getUser();
        String requestLabel = request.getLabel();
        if (requestLabel != null && !requestLabel.trim().isEmpty()){
            HashMap<String, List<Request>> usersRequests;
            if (labelledRequestHistory.containsKey(source)) {
                usersRequests = labelledRequestHistory.get(source);
            } else {
                usersRequests = new HashMap<>();
                labelledRequestHistory.put(source, usersRequests);
            }
            List<Request> jobRequests;
            if (usersRequests.containsKey(user)) {
                jobRequests = usersRequests.get(user);
            } else {
                jobRequests = new CopyOnWriteArrayList<>();
                usersRequests.put(user, jobRequests);
            }
            for (Request existingRequest : jobRequests){
                if (existingRequest.getLabel().equals(requestLabel)){
                    return false;
                }
            }
            jobRequests.add(request);
        } else {
            String jobName = request.getTarget();
            if (requestHistory.containsKey(jobName)) {
                List<Request> requests = requestHistory.get(jobName);
                for (Request existingRequest: requests) {
                    if (existingRequest.getArgs().equals(request.getArgs())){
                        return false;
                    }
                }
                requests.add(request);
            } else {
                List<Request> requests = new CopyOnWriteArrayList<>();
                requests.add(request);
                requestHistory.put(jobName, requests);
            }
        }
        return true;
    }
    
    public boolean deleteRequest(String jobName, HashMap arguments){
        Request cleanupRequest = getJobRequest(jobName, arguments);
        if (cleanupRequest != null){
            List<Request> requestLog = requestHistory.get(jobName);
            requestLog.remove(cleanupRequest);
            return true;
        }
        else return false;
    }

    public Request getUserRequest(String source, String user, String requestName){
        if (labelledRequestHistory.containsKey(source) && labelledRequestHistory.get(source).containsKey(user)){
            List<Request> requestLog = labelledRequestHistory.get(source).get(user);
            for (Request request : requestLog){
                if (request.getLabel().equals(requestName)){
                    return request;
                }
            }
        }
        return null;
    }

    public Set<String> getJobList() {
        return jobManager.getJobList();
    }

    public String createCacheTableName(Time timestamp, String name) {
        Integer maxTableLength = 64;
        name = name.replace(".", "_");
        name = name.replace(" ", "_");
        //Calendar ttl = timestamp.add(Calendar.MINUTE,expireAfterMins);
        String timestampString = timestamp.toString(cacheTimestampPattern);
        String tableName = timestampString + "_" + getRandomString(8) + "_";
        if (tableName.length() + name.length() > maxTableLength){
            Integer maxAppNameLength = maxTableLength - tableName.length();
            name = name.substring(name.length() - maxAppNameLength);
        }
        tableName = tableName + name;
        return tableName;
    }

    public String getRandomString(Integer length){
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(LETTERS, DIGITS)
                .build();
        return generator.generate(8);
    }

    private Request getJobRequest(String jobName, HashMap arguments){
        if (requestHistory.containsKey(jobName)){
            List<Request> requestLog = requestHistory.get(jobName);
            for (Request request : requestLog){
                if (request.getArgs().equals(arguments)){
                    return request;
                }
            }
        }
        return null;
    }

    private void cleanupJobRequests(Time cutoffTime) {
        for (List<Request> requests:requestHistory.values()){
            for (Request request: requests){
                if (request.getRequestTime().isBeforeOrEqual(cutoffTime)){
                    requests.remove(request);
                }
            }
        }
        for (HashMap<String, List<Request>> requestMap: labelledRequestHistory.values()) {
            for (List<Request> requests: requestMap.values()){
                for (Request request: requests) {
                    requests.remove(request);
                }
            }
        }
    }

    public void enableCleanup(){
        if (timer != null) {
            disableCleanup();
        }
        timer = new Timer();
        timer.schedule(new CacheCleanup(), 0, 1000 * 60 * this.cacheTTLMins);
    }

    public void disableCleanup() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private class CacheCleanup extends TimerTask {
        public void run(){
            Time cutoffTime = (new Time()).addMinutes(-1 * cacheTTLMins);
            cleanupJobRequests(cutoffTime);
            cacheManager.cleanupTables(cutoffTime, cacheTimestampPattern);
        }
    }

}
