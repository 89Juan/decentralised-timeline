import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;

import java.util.ArrayList;
import java.util.List;

public class Group {
    final SpreadConnection conn;
    SpreadGroup group;
    List<String> view;

    public Group(SpreadConnection conn) {
        this.conn = conn;
        this.group = new SpreadGroup();
        this.view = new ArrayList<>();
    }

    public Group(SpreadConnection conn, String user) {
        this.conn = conn;
        this.group = new SpreadGroup();
        this.view = new ArrayList<>();
        this.view.add(user);
    }

    public boolean join(String groupName){
        try {
            this.group.join(this.conn, groupName);
            return true;
        } catch (SpreadException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean leave(){
        try {
            this.group.leave();
            return true;
        } catch (SpreadException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setView(List<String> view) {
        this.view.clear();
        this.view.addAll(view);
    }

    public SpreadGroup getGroup() {
        return group;
    }

    public List<String> getView() {
        return view;
    }
}
