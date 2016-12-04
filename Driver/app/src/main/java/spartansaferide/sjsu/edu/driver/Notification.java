package spartansaferide.sjsu.edu.driver;

/**
 * Created by balaji.byrandurga on 12/3/16.
 */

public class Notification {

    private static Notification notify=null;
    public String message="";

    public static synchronized  Notification getInstance() {
        if(notify == null) {
            notify = new Notification();
        }
        return notify;
    }
}
