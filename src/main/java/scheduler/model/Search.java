package scheduler.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import scheduler.logic.SearchDeserializer;
import scheduler.logic.SearchSerializer;

import java.util.HashMap;

@JsonDeserialize(using = SearchDeserializer.class)
@JsonSerialize(using = SearchSerializer.class)
public class Search {

    String user;
    Search target;
    String name;
    HashMap args;


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
        if (target != null) target.setUser(user);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap getArgs() {
        return args;
    }

    public void setArgs(HashMap args) {
        this.args = args;
    }

    public Search getTarget() {
        return target;
    }

    public void setTarget(Search target) {
        this.target = target;
        if (user != null) {
            target.setUser(user);
        }
    }

    public Search createTarget() {
        target = new Search();
        if (user != null) {
            target.setUser(user);
        }
        return target;
    }

}
