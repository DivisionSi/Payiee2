package com.cansis.saad.payiee;

/**
 * Created by Saad on 20/05/2017.
 */

        import java.util.Observable;
        import java.util.Vector;

public class ErrorLog extends Observable {
    private Vector<String> log = new Vector();

    public ErrorLog() {
    }

    public boolean addMsg(String s) {
        boolean retval = false;
        if(s.startsWith("Error:")) {
            retval = true;
        } else if(s.startsWith("Warning:")) {
            retval = true;
        } else if(s.startsWith("Info:")) {
            retval = true;
        } else if(s.startsWith("Debug:")) {
            retval = true;
        }

        if(retval) {
            this.log.add(s);
            this.setChanged();
            this.notifyObservers("log");
        } else {
            this.addMsg("Warning: New Log Entry Lacks Proper Prefix; " + s);
        }

        return retval;
    }
}
