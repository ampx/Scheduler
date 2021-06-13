package util.time.logic;

import util.time.model.Time;

import java.util.ArrayList;
import java.util.List;

public class TimeUtil {
    static public List<String> getStaleRecords(Time cutoffDate, String pattern, List<String> records){
        List<String> filteredList = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            String record = records.get(i);
            Time record_timestamp = Time.parse(record, pattern);
            if (record_timestamp.isBeforeOrEqual(cutoffDate)) {
              filteredList.add(record);
            }
        }
        return filteredList;
    }
}
