package scheduler.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import scheduler.logic.RequestDeserializer;
import scheduler.logic.RequestSerializer;
import scheduler.util.time.model.Time;

import java.util.HashMap;

@JsonDeserialize(using = RequestDeserializer.class)
@JsonSerialize(using = RequestSerializer.class)
public class Request {

    public Request(Type type){
        this.type = type;
        requestTime = Time.now();
        status = Status.CREATED;
    }

    Type type;
    volatile Status status;
    String source = "*";
    String user = "*";
    String label;
    String target;
    HashMap<String, Object> args = new HashMap<>();
    Time requestTime;
    volatile Time completionTime;
    Boolean outputCapture = false;
    String outputCacheName;
    Boolean dataDump = false;
    String dumpCacheName;

    public String getSource() {
        return source;
    }

    public String getUser() {
        return user;
    }

    public String getLabel() {
        return label;
    }

    public String getTarget() {
        return target;
    }

    public HashMap<String, Object> getArgs() {
        return args;
    }

    public Time getRequestTime() {
        return requestTime;
    }

    public Time getCompletionTime() {
        return completionTime;
    }

    public Boolean isOutputCapture() {
        return outputCapture;
    }

    public void setOutputCapture(Boolean capture) {
        this.outputCapture = capture;
    }

    public Boolean isDataDump() {
        return dataDump;
    }

    public void setDataDump(Boolean dump) {
        this.dataDump = dump;
    }

    public String getOutputCacheName() {
        return outputCacheName;
    }

    public void setOutputCacheName(String outputCacheName) {
        this.outputCacheName = outputCacheName;
    }

    public String getDumpCacheName() {
        return dumpCacheName;
    }

    public void setDumpCacheName(String dumpCacheName) {
        this.dumpCacheName = dumpCacheName;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setArgs(HashMap<String, Object> args) {
        this.args = args;
    }

    public enum Type{
        RUN,
        SUBMIT,
        GET
    }

    public static Request createGetRequest(){
        return new Request(Type.GET);
    }

    public static Request createSubmitRequest() {
        return new Request(Type.SUBMIT);
    }

    public static Request createRunRequest() {
        return new Request(Type.RUN);
    }

    public Boolean isGetRequest() {
        if (type.equals(Type.GET)) return true;
        return false;
    }

    public Boolean isRunRequest() {
        if (type.equals(Type.RUN)) return true;
        return false;
    }

    public Boolean isSubmitRequest() {
        if (type.equals(Type.SUBMIT)) return true;
        return false;
    }

    public boolean isComplete(){
        if (status == Status.COMPLETE){
            return true;
        }
        return false;
    }

    public String getTypeString() {
        return type.name();
    }

    public void complete() {
        status = Status.COMPLETE;
        completionTime = Time.now();
    }

    public boolean isInProgress(){
        if (status == Status.PROGRESS){
            return true;
        }
        return false;
    }

    public void progress() {
        status = Status.PROGRESS;
    }

    public boolean isFailed(){
        if (status == Status.FAILED){
            return true;
        }
        return false;
    }

    public void failed() {
        status = Status.FAILED;
        completionTime = Time.now();
    }

    public boolean isCreated(){
        if (status == Status.CREATED){
            return true;
        }
        return false;
    }

    public String getStatusString() {
        return status.toString();
    }

    public enum Status {
        COMPLETE,
        PROGRESS,
        FAILED,
        CREATED
    }

    public String toString(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
