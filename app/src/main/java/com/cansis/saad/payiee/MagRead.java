package com.cansis.saad.payiee;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
/*
        import com.cansis.saad.payiee.MagReadListener;
        import com.square.MagstripperModel;
        import com.square.MicIn;
        import com.square.Swipe;
        */
        import java.util.Observable;
        import java.util.Observer;

public class MagRead implements Observer {
    private MagReadListener mListener;
    private MicIn main;
    private MagstripperModel model = new MagstripperModel();

    public MagRead() {
        this.main = new MicIn(this.model);
        this.main.setPriority(10);
        this.main.start();
        this.model.addObserver(this);
    }

    public void start() {
        this.model.setListening(true);
        this.main.suspendListening(false);
    }

    public void stop() {
        this.model.setListening(false);
        this.main.suspendListening(true);
    }

    public void addListener(MagReadListener listener) {
        this.mListener = listener;
    }

    public void release() {
        if(!this.main.isInterrupted()) {
            this.main.interrupt();
        }

        if(this.main.getMicIn().getState() == 1) {
            this.main.getMicIn().release();
        }

    }

    public void update(Observable observable, Object obj) {
        if(obj == "zerolevel") {
            if(!this.model.getZeroAutomatic()) {
                this.main.setZeroLevel((short)this.model.getZeroLevel());
            }
        } else if(obj == "thresholds") {
            this.main.setThresholds();
        } else if(obj instanceof Swipe) {
            Swipe swipe = (Swipe)obj;
            this.mListener.updateBytes(swipe.getDecodedString());
            this.mListener.updateBits(swipe.getStrippedBinary());
        }

    }
}
