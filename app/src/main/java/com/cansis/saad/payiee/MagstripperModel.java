package com.cansis.saad.payiee;

/**
 * Created by Saad on 20/05/2017.
 */

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


        import android.annotation.SuppressLint;
        import java.util.HashMap;
        import java.util.Observable;

public class MagstripperModel extends Observable {
    private boolean listening = false;
    private Boolean locked = Boolean.valueOf(true);
    private String comport = "COM1";
    private static ErrorLog el = new ErrorLog();
    private HashMap<String, Swipe> openSwipes = new HashMap();
    private short thresdelta = 600;
    private int lockTime = 4000;
    private int zerolvl = 0;
    private boolean zeroautomatic = true;
    private short numswipes = 0;
    private char spaceChar = 32;
    private boolean secLog = true;
    private int saltRange = 256;
    private String version = "0.3a";

    public MagstripperModel() {
    }

    public void setSpaceChar(char c) {
        this.spaceChar = c;
        this.setChanged();
        el.addMsg("Info: Setting Space Character To " + c);
        this.notifyObservers("space");
    }

    public char getSpaceChar() {
        return this.spaceChar;
    }

    public void setNumSwipes(short i) {
        this.numswipes = i;
        el.addMsg("Info: Setting Number Of Swipes In Memory To " + i);
    }

    public short getNumSwipes() {
        return this.numswipes;
    }

    public void setZeroAutomatic(boolean b) {
        this.zeroautomatic = b;
        this.setChanged();
        this.notifyObservers("zerolevelautomatic");
    }

    public boolean getZeroAutomatic() {
        return this.zeroautomatic;
    }

    public void setZeroLevel(int i) {
        this.zerolvl = i;
        this.setChanged();
        this.notifyObservers("zerolevel");
    }

    public void setZeroLevel(int i, boolean b) {
        if(b) {
            this.setZeroLevel(i);
        } else {
            this.zerolvl = i;
        }

    }

    public int getZeroLevel() {
        return this.zerolvl;
    }

    public void setDelta(short d) {
        this.thresdelta = d;
        this.setChanged();
        this.notifyObservers("thresholds");
    }

    public short getDelta() {
        return this.thresdelta;
    }

    public void setLockTime(int i) {
        this.lockTime = i;
        el.addMsg("Info: Setting Default Unlock Time To " + i);
    }

    public int getLockTime() {
        return this.lockTime;
    }

    public void setListening(boolean tl) {
        this.listening = tl;
    }

    public boolean getListeningState() {
        return this.listening;
    }

    public void setLockState(boolean l) {
        this.locked = Boolean.valueOf(l);
        el.addMsg("Info: Setting Strike To " + (l?"Locked":"Unlocked"));
        this.setChanged();
        this.notifyObservers("lock");
    }

    public boolean getLockState() {
        return this.locked.booleanValue();
    }

    public void addSwipe(Swipe s) {
        s.decodeSwipe();
        this.openSwipes.put(s.getTimestamp(), s);
        this.setChanged();
        this.notifyObservers(s);
    }

    public void removeSwipe(String id) {
        this.openSwipes.remove(id);
    }

    @SuppressLint({"UseValueOf"})
    public Swipe getSwipe(String time) {
        return (Swipe)this.openSwipes.get(time);
    }

    public int getSwipeCount() {
        return this.openSwipes.size();
    }

    public ErrorLog getLog() {
        return el;
    }

    public void setComPort(String s) {
        this.comport = s;
        el.addMsg("Info: Setting Com Port to " + s);
    }

    public String getComPort() {
        return this.comport;
    }

    public void setSecLog(boolean b) {
        this.secLog = b;
    }

    public boolean getSecLog() {
        return this.secLog;
    }

    public int getSaltRange() {
        return this.saltRange;
    }

    public void setSaltRange(int i) {
        this.saltRange = i;
    }

    public String getVersion() {
        return this.version;
    }
}
