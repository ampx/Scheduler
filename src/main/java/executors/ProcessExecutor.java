package executors;

import util.table.model.Table;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ProcessExecutor extends JobExecutor {
    String process;
    protected String argDeliminator;
    protected String argEquality;

    public ProcessExecutor(HashMap<String, Object> config) {
        super(config);
        if (config.containsKey("argDelim")) setArgDeliminator((String) config.get("argDelim"));
        else setArgDeliminator("&");
        if (config.containsKey("argEquality")) setArgEquality((String) config.get("argEquality"));
        else setArgEquality("=");
        if (config.containsKey("process")) {
            this.process = (String) config.get("process");
        }
    }

    public void setArgDeliminator(String argDeliminator) {
        this.argDeliminator = argDeliminator;
    }

    public void setArgEquality(String argEquality) {
        this.argEquality = argEquality;
    }

    @Override
    public Table execute(HashMap arguments, boolean captureOutput) {
        Process p;
        Table outputTable = null;
        try {
            p = Runtime.getRuntime().exec(new String[]{"bash", "-c", processArgAppender(arguments)});
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

    public String processArgAppender(HashMap<Object, Object> arguments) {
        String processWithArgs = process;
        String argumentsStr = null;
        if (arguments != null) {
            for (Map.Entry<Object, Object> entry : arguments.entrySet()) {
                if (argumentsStr == null) {
                    argumentsStr = entry.getKey().toString();
                } else {
                    argumentsStr += argDeliminator + entry.getKey();
                }
                if (entry.getValue() != null) {
                    argumentsStr += argEquality + entry.getValue();
                }
            }
        } else {
            argumentsStr = "";
        }
        if (process.contains("__$args")) {
            processWithArgs = process.replace("__$args", argumentsStr);
        } else {
            processWithArgs = process.concat(" " + argumentsStr);
        }
        return processWithArgs;
    }
}
