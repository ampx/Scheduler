package scheduler.util.table.logic;

import scheduler.util.table.model.Table;

import java.util.*;

public class TableUtil {

    public static String listToAnnotation(List objects){
        String result = "[";
        for (Object object : objects) {
            result += "{ \"text\":\"" + object.toString() + "\"}";
        }
        result += "]";
        return result;
    }

    public static Table getEmptyTable() {
        Table table = new Table(1);
        table.setHeaders(new String[]{"Data"});
        List<Object[]> column = new ArrayList<>(1);
        column.add(new String[]{""});
        table.setRows(column);
        return table;
    }

    public static Table objectToTable(Object object){
        Table table = new Table(1);
        table.setHeaders(new String[]{"Data"});
        List<Object[]> column = new ArrayList<>(1);
        column.add(new String[]{object.toString()});
        table.setRows(column);
        return table;
    }

    public static Table toTable(List<Object> objectList){
        Table table = new Table(1);
        table.setHeaders(new String[]{"Data"});
        List<Object[]> column = new ArrayList<>(objectList.size());
        for (Object object: objectList){
            column.add(new String[]{object.toString()});
        }
        table.setRows(column);
        return table;
    }

    public static Table toTable(HashMap<String, Object> objectMap){
        Set<String> headerSet = objectMap.keySet();
        Table table = new Table(headerSet.size());
        List<Object[]> column = new ArrayList<>(1);
        Object[] row = new Object[headerSet.size()];
        String[] headers = new String[headerSet.size()];
        int i = 0;
        for (String key: headerSet){
            row[i] = objectMap.get(key).toString();
            headers[i++] = key.toString();
        }
        table.setHeaders(headers);
        column.add(row);
        table.setRows(column);
        return table;
    }

    public static Table toTable(HashMap<String, Object>[] objectMaps, Integer scanRows){
        int i = 0;
        Set<String> headerSet = new HashSet<>();
        for (HashMap objectMap: objectMaps) {
            headerSet.addAll(objectMap.keySet());
            if (i++ > scanRows) {
                break;
            }
        }
        Table table = new Table(headerSet.size());
        table.setHeaders((String[]) headerSet.toArray());
        List<Object[]> column = new ArrayList<>(objectMaps.length);
        for (HashMap objectMap: objectMaps) {
            Object[] row = new Object[headerSet.size()];
            int k = 0;
            for (String key : headerSet) {
                row[i++] = objectMap.get(key);
            }
            column.add(row);
        }
        table.setRows(column);
        return table;
    }
}
