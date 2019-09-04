package Protocol;

import java.util.List;

public class Subscription extends Interaction {
    List<String> nextView;
    List<String> currentView;
    String group;

    public Subscription(List<String> nextView, List<String> currentView, String group) {
        this.nextView = nextView;
        this.currentView = currentView;
        this.group = group;
    }


    public List<String> getNextView() {
        return nextView;
    }

    public List<String> getCurrentView() {
        return currentView;
    }

    public String getGroup() {
        return group;
    }
}
