package scheduler.executors;

import scheduler.util.table.model.Table;

import javax.naming.ConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

public class ProcessExecutor extends Executor {
    private ArrayList process;
    private String argEquality="=";
    private String homeDir = null;
    private String[] envVars = null;
    private Boolean captureOutput = false;
    private String cacheArg = null;

    public ProcessExecutor(HashMap<String, Object> config) throws ConfigurationException {
        super(config);
        this.process = (ArrayList) config.get("process");
        if (this.process == null || this.process.size() == 0 || ((String)this.process.get(0)).trim().isEmpty()) {
            throw new ConfigurationException();
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
        if (!safeArgs(arguments)) {
            return null;
        }
        Process p;
        Table outputTable = null;
        if (cacheArg != null && cacheName != null) {
            if (arguments == null) {
                arguments = new HashMap();
            }
            arguments.put(cacheArg, cacheName);
        }
        try {
            if (homeDir != null) {
                p = Runtime.getRuntime().exec(cmdAppender(arguments), envVars, new File(homeDir));
            } else {
                p = Runtime.getRuntime().exec(cmdAppender(arguments), envVars);
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

    public boolean safeArgs(Map args) {
        if (args != null) {
            String permittedCharRegex = "[a-zA-Z0-9\\.\\-_\\ ]*";
            for (Object key : args.keySet()) {
                if (key instanceof String && !((String) key).matches(permittedCharRegex)) {
                    return false;
                }
            }
            for (Object value : args.values()) {
                if (value instanceof String && !((String) value).matches(permittedCharRegex)) {
                    return false;
                }
            }
        }
        return true;
    }

    public String[] cmdAppender(HashMap<Object, Object> arguments) {
        List<String> processList = new ArrayList<>();
        processList.addAll(process);
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
}
