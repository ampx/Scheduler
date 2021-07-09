package scheduler.executors;

import scheduler.util.table.model.Table;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

public class ProcessExecutor extends Executor {
    private String process;
    private String argEquality="=";
    private String homeDir = null;
    private String[] envVars = null;
    private Boolean captureOutput = false;
    private String cacheArg = null;

    public ProcessExecutor(HashMap<String, Object> config) {
        super(config);
        if (config.containsKey("process")) {
            this.process = (String) config.get("process");
        }
        if (config.containsKey("homeDir")) {
            this.homeDir = (String) config.get("homeDir");
        }
        if (config.containsKey("envVars")) {
            ArrayList varsList = (ArrayList) config.get("envVars");
            envVars = (String[]) varsList.toArray(new String[varsList.size()]);
        }
        if (config.containsKey("captureOutput")) {
            this.captureOutput = (Boolean) config.get("captureOutput");
        }
        if (config.containsKey("cacheArg")) {
            this.cacheArg = (String) config.get("cacheArg");
        }
    }

    public void setArgEquality(String argEquality) {
        this.argEquality = argEquality;
    }

    @Override
    public Table execute(HashMap arguments, String cacheName) {
        Process p;
        Table outputTable = null;
        HashMap filteredArgs = filterRequestArgs(arguments);
        if (cacheArg != null && cacheName != null && filteredArgs != null) {
            filteredArgs.put(cacheArg, cacheName);
        }
        try {
            if (homeDir != null) {
                p = Runtime.getRuntime().exec(cmdAppender(filteredArgs), envVars, new File(homeDir));
            } else {
                p = Runtime.getRuntime().exec(cmdAppender(filteredArgs), envVars);
            }
            if (captureOutput) {
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String s;
                outputTable = new Table(1);
                outputTable.setHeaders(new String[]{"Output"});
                while ((s = br.readLine()) != null) {
                    outputTable.addRow(new String[]{s});
                }
            }
            p.waitFor();
            Integer exitValue = p.exitValue();//can do interesting stuff here to log if job failed
            p.destroy();
        } catch (Exception e) {
            System.out.println(e);
        }
        return outputTable;
    }

    public String[] cmdAppender(HashMap<Object, Object> arguments) {
        List<String> processList = new ArrayList<>();
        processList.add(process);
        if (arguments != null) {
            if (fixedArgs != null) arguments.putAll(fixedArgs);
            for (Map.Entry<Object, Object> entry : arguments.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().equals("")) {
                    String arg = entry.getKey() + argEquality + entry.getValue();
                    processList.add(arg);
                } else {
                    processList.add((String) entry.getKey());
                }
            }
        }
        return processList.toArray(new String[processList.size()]);
    }

    public String getProcess() {
        return process;
    }

    public String getArgEquality() {
        return argEquality;
    }

    public String getHomeDir() {
        return homeDir;
    }

    public String[] getEnvVars() {
        return envVars;
    }
}
