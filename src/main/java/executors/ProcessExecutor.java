package executors;

import util.table.model.Table;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

public class ProcessExecutor extends JobExecutor {
    private String process;
    private String argEquality="=";
    private Set argSet = new HashSet();
    private String homeDir = null;
    private String[] envVars = null;

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
        if (config.containsKey("argSet")) {
            argSet.addAll((ArrayList) config.get("argSet"));
        }
    }

    public void setArgEquality(String argEquality) {
        this.argEquality = argEquality;
    }

    @Override
    public Table execute(HashMap arguments, boolean captureOutput) {
        Process p;
        Table outputTable = null;
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

    public String[] cmdAppender(HashMap<Object, Object> arguments) {
        List<String> processList = new ArrayList<>();
        processList.add(process);
        if (arguments != null) {
            for (Map.Entry<Object, Object> entry : arguments.entrySet()) {
                if (argSet.contains(entry.getKey())){
                    String arg = entry.getKey() + argEquality + entry.getValue();
                    processList.add(arg);
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

    public Set getArgSet() {
        return argSet;
    }

    public String getHomeDir() {
        return homeDir;
    }

    public String[] getEnvVars() {
        return envVars;
    }
}
