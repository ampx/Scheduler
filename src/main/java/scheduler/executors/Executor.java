package scheduler.executors;

import scheduler.util.table.model.Table;

import java.util.*;

public abstract class Executor {
    Set<String> executeUsers;
    Set<String> readUsers;
    protected Set argSet = new HashSet();
    protected HashMap fixedArgs = null;

    public Executor(HashMap<String, Object> config){
        if (config.containsKey("executeUsers")) {
            Set<String> executeUsers = new HashSet<>();
            Set<String> readUsers = new HashSet<>();
            if (config.get("executeUsers") instanceof ArrayList){
                for(String user:(ArrayList<String>) config.get("executeUsers")){
                    executeUsers.add(user);
                    readUsers.add(user);
                }
            } else if (config.get("executeUsers") instanceof String) {
                executeUsers = new HashSet<>();
                executeUsers.add((String) config.get("executeUsers"));
                readUsers = new HashSet<>();
                readUsers.add((String) config.get("executeUsers"));
            }
            if (config.containsKey("readUsers")) {
                if (config.get("readUsers") instanceof ArrayList){
                    for(String user:(ArrayList<String>) config.get("readUsers")){
                        readUsers.add(user);
                    }
                } else if (config.get("readUsers") instanceof String) {
                    readUsers.add((String) config.get("readUsers"));
                }
            }
            setExecuteUsers(executeUsers);
            setReadUsers(readUsers);
            if (config.containsKey("argSet")) {
                argSet.addAll((ArrayList) config.get("argSet"));
            }
            if (config.containsKey("fixedArgs")) {
                fixedArgs = (HashMap) config.get("fixedArgs");
            }
        }
    }

    public abstract Table execute(HashMap arguments, boolean captureOutput);

    public Set<String> getExecuteUsers() {
        return executeUsers;
    }

    public void setExecuteUsers(Set<String> executeUsers) {
        this.executeUsers = executeUsers;
    }

    public Set<String> getReadUsers() {
        return readUsers;
    }

    public void setReadUsers(Set<String> readUsers) {
        this.readUsers = readUsers;
    }

    public boolean canExecute(String user){
        if (executeUsers == null || executeUsers.contains(user) || executeUsers.contains("*")) {
            return true;
        }
        return false;
    }

    public boolean canRead(String user){
        if (readUsers == null || readUsers.contains("*") || readUsers.contains(user) ) {
            return true;
        }
        return false;
    }

    public HashMap filterRequestArgs(HashMap<Object, Object> requestArgs) {
        HashMap cleanArgs = null;
        if (requestArgs != null) {
            cleanArgs = new HashMap();
            for (Map.Entry<Object, Object> entry : requestArgs.entrySet()) {
                if (argSet.contains(entry.getKey())) {
                    cleanArgs.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return cleanArgs;
    }
}
