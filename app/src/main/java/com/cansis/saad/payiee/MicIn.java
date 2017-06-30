package com.cansis.saad.payiee;

/**
 * Created by Saad on 20/05/2017.
 */

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

        import android.media.AudioRecord;

        import java.util.Iterator;
        import java.util.LinkedList;

public class MicIn extends Thread {
    private short zerolvl = 0;
    private short posthres;
    private short negthres;
    private short delta;
    private MagstripperModel model;
    private volatile boolean suspend = true;
    private byte[] lastRead;
    private AudioRecord ar;
    private int bs;
    private static int SAMPLE_RATE_IN_HZ = '걄';

    public MicIn(MagstripperModel m) {
        this.model = m;
        this.delta = m.getDelta();
    }

    public void setZeroLevel(short i) {
        this.zerolvl = i;
        this.posthres = (short)(this.delta + this.zerolvl);
        this.negthres = (short)(this.zerolvl - this.delta);
        this.model.getLog().addMsg("Info: Setting Microphone Zero Level To: " + this.zerolvl + " Setting Thresholds To: " + this.posthres + " - " + this.negthres);
    }

    public short recalculateZeroLevel() {
        int retval = 0;

        for(int i = 200; i < 1200; i += 2) {
            retval += (short)(((short)this.lastRead[i + 1] << 8) + ((short)this.lastRead[i] & 255));
        }

        short retval1 = (short)(retval / 500);
        this.model.getLog().addMsg("Info: Recalculating Microphone Zero Level To: " + retval1);
        return (short)retval1;
    }

    public void setThresholds() {
        this.delta = this.model.getDelta();
        this.posthres = (short)(this.delta + this.zerolvl);
        this.negthres = (short)(this.zerolvl - this.delta);
        this.model.getLog().addMsg("Info: Setting Thresholds To: " + this.posthres + " - " + this.negthres);
    }

    public void run() {
        this.bs = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, 16, 2);
        this.ar = new AudioRecord(1, SAMPLE_RATE_IN_HZ, 16, 2, this.bs * 10);
        byte[] buffer = new byte[this.bs * 10];

        try {
            super.run();
            this.ar.startRecording();
        } catch (IllegalArgumentException var15) {
            this.model.getLog().addMsg("Error: IllegalArgumentException Caught When Trying To Initialize Microphone Input");
            this.model.setListening(false);
            return;
        }

        this.lastRead = new byte[SAMPLE_RATE_IN_HZ / 10];
        boolean read = false;
        if(81 > this.ar.read(this.lastRead, 0, this.lastRead.length)) {
            this.zerolvl = 0;
            this.model.getLog().addMsg("Warning: We Couldn\'t Set The Zero Level Because We Didn\'t Read Enough Bytes");
        }

        boolean trackDetected = false;
        int inNoiseLevel = 0;
        LinkedList currAudioData = new LinkedList();
        int min = '\uffff';
        int max = -65535;
        int quartersecs = 0;

        int var18;
        while((var18 = this.ar.read(buffer, 0, buffer.length)) > 0) {
            synchronized(this) {
                try {
                    for(; this.suspend; this.setThresholds()) {
                        this.wait();
                        if(!this.suspend) {
                            this.zerolvl = this.recalculateZeroLevel();
                            this.model.setZeroLevel(this.zerolvl, false);
                        }
                    }
                } catch (InterruptedException var16) {
                    this.model.getLog().addMsg("Error: InterruptedException Thrown When Trying To Suspend Microphone Input");
                    return;
                }
            }

            int i;
            for(i = 0; i < var18; i += 2) {
                short val = (short)(((short)buffer[i + 1] << 8) + ((short)buffer[i] & 255));
                if((trackDetected || val <= this.posthres) && (trackDetected || val >= this.negthres)) {
                    if(trackDetected) {
                        if(currAudioData.size() > SAMPLE_RATE_IN_HZ) {
                            currAudioData.clear();
                            this.model.getLog().addMsg("Warning: The Zero Level / Thresholds Are Way Off Causing A Detected Swipe Over 5 Seconds, Discarding Swipe And Resetting Levels");
                            this.zerolvl = this.recalculateZeroLevel();
                            this.model.setZeroLevel(this.zerolvl, false);
                            if(this.model.getDelta() < 600) {
                                this.model.getLog().addMsg("Info: Thresholds Reset To Default Value");
                                this.model.setDelta((short)600);
                            } else {
                                this.model.getLog().addMsg("Info: Thresholds Increased beyond Default Value, User Should Check Microphone Levels And Manually Set Thresholds");
                                this.model.setDelta((short)(this.model.getDelta() + 300));
                            }

                            trackDetected = false;
                        } else {
                            currAudioData.add(new Byte(buffer[i]));
                            currAudioData.add(new Byte(buffer[i + 1]));
                            if(val < this.posthres && val > this.negthres) {
                                inNoiseLevel += 2;
                            } else {
                                inNoiseLevel = 0;
                            }

                            if(inNoiseLevel >= this.lastRead.length) {
                                trackDetected = false;
                                inNoiseLevel = 0;
                                if(currAudioData.size() % 2 != 0) {
                                    this.model.getLog().addMsg("Error: Read Odd Amount Of Data From Microphone, Cannot Decode Swipe");
                                } else {
                                    this.model.getLog().addMsg("Info: Detected End Of Swipe From Microphone");
                                    byte[] var19 = new byte[currAudioData.size()];
                                    Iterator it = currAudioData.iterator();

                                    for(int pos = 0; it.hasNext(); ++pos) {
                                        var19[pos] = ((Byte)it.next()).byteValue();
                                    }

                                    currAudioData.clear();
                                    Swipe s = new Swipe(var19);
                                    this.model.addSwipe(s);
                                }
                            }
                        }
                    } else {
                        if(val > max) {
                            max = val;
                        }

                        if(val < min) {
                            min = val;
                        }
                    }
                } else {
                    trackDetected = true;
                    min = '\uffff';
                    max = -65535;
                    quartersecs = 0;
                    this.model.getLog().addMsg("Info: Detected Start Of Swipe From Microphone");
                    int sdata;
                    if(i < this.lastRead.length) {
                        currAudioData.add(new Byte(buffer[i + 1]));

                        for(sdata = 0; sdata < this.lastRead.length - 1; ++sdata) {
                            if(i - sdata >= 0) {
                                currAudioData.add(0, new Byte(buffer[i - sdata]));
                            } else {
                                currAudioData.add(0, new Byte(this.lastRead[i + this.lastRead.length - sdata]));
                            }
                        }
                    } else {
                        currAudioData.add(new Byte(buffer[i + 1]));

                        for(sdata = 0; sdata < this.lastRead.length - 1; ++sdata) {
                            currAudioData.add(0, new Byte(buffer[i - sdata]));
                        }
                    }
                }
            }

            ++quartersecs;
            if(quartersecs == 80 && !trackDetected) {
                this.model.getLog().addMsg("Debug: Min/Max Values Of Noise From Mic " + min + " - " + max + "  Zerolevel Off By " + ((max - min) / 2 - this.zerolvl));
                quartersecs = 0;
                min = '\uffff';
                max = -65535;
            }

            for(i = 0; i < buffer.length && i < this.lastRead.length; ++i) {
                this.lastRead[i] = buffer[buffer.length - this.lastRead.length + i];
            }
        }

    }

    public synchronized void suspendListening(boolean b) {
        this.suspend = b;
        if(!this.suspend) {
            this.notifyAll();
        }

    }

    public AudioRecord getMicIn() {
        return this.ar;
    }
}
