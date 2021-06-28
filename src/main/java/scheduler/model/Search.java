package scheduler.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import scheduler.logic.SearchDeserializer;
import scheduler.logic.SearchSerializer;

@JsonDeserialize(using = SearchDeserializer.class)
@JsonSerialize(using = SearchSerializer.class)
public class Search {

    String user;
    String source;
    String tempLabel;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTempLabel() {
        return tempLabel;
    }

    public void setTempLabel(String tempLabel) {
        this.tempLabel = tempLabel;
    }
}
