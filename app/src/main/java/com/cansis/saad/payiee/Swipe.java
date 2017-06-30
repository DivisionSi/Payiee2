package com.cansis.saad.payiee;

/**
 * Created by Saad on 20/05/2017.
 */

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


        import android.annotation.SuppressLint;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.regex.Matcher;
        import java.util.regex.Pattern;

public class Swipe {
    private byte[] bis;
    private int bytesperframe;
    private int channels;
    private float samplerate;
    private static int SAMPLE_RATE_IN_HZ = '걄';
    private int bitspersample;
    private boolean bitorder;
    private String aformat;
    private long framelength;
    private short totalnegpeaks = -1;
    private short totalpospeaks = -1;
    private short zerolvl;
    private short currposthres;
    private short currnegthres;
    private ArrayList<int[]> peaks = new ArrayList();
    private String rawbinary = "";
    private String strippedbinary = "";
    private boolean swipedreverse = false;
    private boolean leadingone = false;
    private byte bitsperchar = 0;
    private String lrc;
    private ArrayList<Integer> crcerr = new ArrayList();
    private String decodedString = "";
    private ArrayList<Integer> lrcerr = new ArrayList();
    private short leadingzeros = 0;
    private short trailingzeros = 0;
    private short rawlength;
    private ArrayList<Integer> crccorrections = new ArrayList();
    private static ErrorLog errorLog = new ErrorLog();
    private Date decodeTime;

    public Swipe(byte[] sdata) {
        this.bis = new byte[sdata.length];
        this.bis = sdata;
        this.bytesperframe = 2;
        this.channels = 1;
        this.samplerate = (float)SAMPLE_RATE_IN_HZ;
        this.bitspersample = 16;
        this.framelength = (long)(sdata.length / 2);
    }

    public void decodeSwipe() {
        this.decodeTime = new Date();
        errorLog.addMsg("Info: Decode Time " + this.getTimestamp());
        this.peaks = this.findPeaks();
        if(this.peaks.size() > 1) {
            this.rawbinary = this.dumpRawBinary();
            this.strippedbinary = this.decodeChars();
            System.out.println("strippedbinary:" + this.strippedbinary);
            if(this.bitsperchar == 5) {
                this.decodedString = decodeABA(this.strippedbinary, this.bitsperchar, errorLog);
            } else if(this.bitsperchar == 7) {
                this.decodedString = decodeIATA(this.strippedbinary, this.bitsperchar, errorLog);
            }

            System.out.println("ASCII:" + this.decodedString);
        }

    }

    private ArrayList<int[]> findPeaks() {
        double thresper = 0.2D;
        this.currposthres = (short)(this.bytesperframe == 2?400:2);
        this.currnegthres = (short)(-this.currposthres);
        this.zerolvl = 0;
        int lastpeakframe = 0;
        short lastpeakval = 0;
        short lastval = 0;
        boolean peakhit = false;
        this.totalnegpeaks = 0;
        this.totalpospeaks = 0;
        ArrayList apeaks = new ArrayList();

        int i;
        for(i = 0; i < this.bis.length; i += this.bytesperframe) {
            int frame;
            if(i == 0) {
                int val = 0;

                for(frame = 0; frame < 50 && frame < this.bytesperframe * 10; frame += this.bytesperframe) {
                    short val1 = 0;
                    if(this.bytesperframe == 2) {
                        val1 = (short)(((short)this.bis[frame + 1] << 8) + ((short)this.bis[frame] & 255));
                    } else if(this.bytesperframe == 1) {
                        val1 = (short)(-128 ^ this.bis[frame]);
                    }

                    if(val1 >= this.currposthres || val1 <= this.currnegthres) {
                        if(frame == 0) {
                            this.zerolvl = val1;
                        }

                        errorLog.addMsg("Warning: The noise level is shifted outside the normal range. Setting to " + val1);
                        break;
                    }

                    val += val1;
                    this.zerolvl = (short)(val / (frame / this.bytesperframe + 1));
                    errorLog.addMsg("Debug: Setting zerolevel frame: " + frame / this.bytesperframe + " val " + val1 + " curr sum " + val + " curr zerolvl " + this.zerolvl);
                }

                this.currposthres += this.zerolvl;
                this.currnegthres += this.zerolvl;
                errorLog.addMsg("Debug: Initial thresholds: " + this.currposthres + " - " + this.currnegthres + " zerolevel: " + this.zerolvl);
            }

            boolean var12 = false;
            short var13;
            if(this.bytesperframe == 2) {
                var13 = (short)(((short)this.bis[i + 1] << 8) + ((short)this.bis[i] & 255));
            } else {
                if(this.bytesperframe != 1) {
                    errorLog.addMsg("Error: This Recording Did Not Have 8 Or 16 Bit Sample Sizes, Cannot Process");
                    return null;
                }

                var13 = (short)(-128 ^ this.bis[i]);
            }

            frame = i / this.bytesperframe;
            if(var13 > this.currposthres && !peakhit && var13 < lastval) {
                apeaks.add(new int[]{frame, lastval, frame - lastpeakframe});
                ++this.totalpospeaks;
                peakhit = true;
                lastpeakval = lastval;
                if((double)(var13 - this.zerolvl) * thresper - (double)this.zerolvl > (double)this.currposthres) {
                    this.currposthres = (short)((int)((double)(var13 - this.zerolvl) * thresper - (double)this.zerolvl));
                }
            } else if(var13 < this.currnegthres && !peakhit && var13 > lastval) {
                apeaks.add(new int[]{frame, lastval, frame - lastpeakframe});
                ++this.totalnegpeaks;
                peakhit = true;
                lastpeakval = lastval;
                if((double)this.zerolvl - (double)(this.zerolvl - var13) * thresper < (double)this.currnegthres) {
                    this.currnegthres = (short)((int)((double)this.zerolvl - (double)(this.zerolvl - var13) * thresper));
                }
            } else if(peakhit && lastpeakval > this.currposthres && var13 > lastpeakval) {
                apeaks.add(new int[]{frame, var13, frame - lastpeakframe});
                apeaks.remove(apeaks.size() - 2);
                lastpeakval = var13;
                errorLog.addMsg("Debug: Found Second Higher Peak Before Reaching Noise Level, Frame: " + frame + " Gap: " + (frame - lastpeakframe) + " Val: " + var13);
                if((double)(var13 - this.zerolvl) * thresper - (double)this.zerolvl > (double)this.currposthres) {
                    this.currposthres = (short)((int)((double)(var13 - this.zerolvl) * thresper - (double)this.zerolvl));
                }
            } else if(peakhit && lastpeakval < this.currnegthres && var13 < lastpeakval) {
                apeaks.add(new int[]{frame, var13, frame - lastpeakframe});
                apeaks.remove(apeaks.size() - 2);
                lastpeakval = var13;
                errorLog.addMsg("Debug: Found Second Lower Peak Before Reaching Noise Level, Frame: " + frame + " Gap: " + (frame - lastpeakframe) + " Val: " + var13);
                if((double)this.zerolvl - (double)(this.zerolvl - var13) * thresper < (double)this.currnegthres) {
                    this.currnegthres = (short)((int)((double)this.zerolvl - (double)(this.zerolvl - var13) * thresper));
                }
            } else if(peakhit && var13 < this.currposthres && var13 > this.currnegthres) {
                peakhit = false;
                lastpeakframe = ((int[])apeaks.get(apeaks.size() - 1))[0];
            }

            lastval = var13;
        }

        errorLog.addMsg("Debug: Threshold Before Dropping Peak Rescan: " + this.currposthres + " - " + this.currnegthres + " Zerolevel: " + this.zerolvl);

        for(i = 0; i < apeaks.size() - 1; ++i) {
            if(((int[])apeaks.get(i))[1] > this.currnegthres && ((int[])apeaks.get(i))[1] < this.currposthres) {
                errorLog.addMsg("Info: Dropping Peak: " + i + " At Frame: " + ((int[])apeaks.get(i))[0] + " Val: " + ((int[])apeaks.get(i))[1] + " Gap: " + ((int[])apeaks.get(i))[2]);
                int[] var10000 = (int[])apeaks.get(i + 1);
                var10000[2] += ((int[])apeaks.get(i))[2];
                apeaks.remove(i);
                apeaks.trimToSize();
                --i;
            }
        }

        if(apeaks.size() <= 1) {
            errorLog.addMsg("Error: No Peaks Detected, Cannot Decode");
            return apeaks;
        } else {
            errorLog.addMsg("Debug: Number Of Peaks: " + apeaks.size() + " Frames Of Peaks: " + (((int[])apeaks.get(apeaks.size() - 1))[0] - ((int[])apeaks.get(0))[0]) + " First Frame: " + ((int[])apeaks.get(0))[0] + " Last Frame: " + ((int[])apeaks.get(apeaks.size() - 1))[0]);
            if(apeaks.size() < 2) {
                errorLog.addMsg("Error: Less Than 2 Peaks Found, Cannot Decode");
                return apeaks;
            } else {
                errorLog.addMsg("Debug: Dropping First Peak, Frame: " + ((int[])apeaks.get(0))[0] + " Val: " + ((int[])apeaks.get(0))[1]);
                apeaks.remove(0);
                apeaks.trimToSize();
                return apeaks;
            }
        }
    }

    private String dumpRawBinary() {
        boolean expectshort = false;
        String binary = "";
        double rangeper = 0.2D;
        int[] avglasta = new int[5];
        int avglastpos = 0;
        int peaksdecoded = 0;

        int j;
        for(j = 0; j < avglasta.length; ++j) {
            avglasta[j] = ((int[])this.peaks.get(0))[2];
        }

        int range;
        double var23;
        for(j = 1; j < avglasta.length && j < this.peaks.size(); ++j) {
            int[] peakframe = (int[])this.peaks.get(j);
            double peakvalue = 0.0D;

            for(int avglast = 0; avglast < j; ++avglast) {
                peakvalue += (double)avglasta[avglast] * (double)(avglast + 1) / (double)j;
            }

            peakvalue /= (double)j / 2.0D + 0.5D;
            var23 = rangeper * peakvalue;
            if(peakvalue + var23 >= (double)peakframe[2] && peakvalue - var23 <= (double)peakframe[2]) {
                avglasta[j] = peakframe[2];
                if(expectshort) {
                    errorLog.addMsg("Warning: When calculating the initial average, we were expecting a short gap but detected a long");
                }

                expectshort = false;
            } else if(peakvalue / 2.0D + var23 >= (double)peakframe[2]) {
                if(peakvalue / 2.0D - var23 > (double)peakframe[2]) {
                    errorLog.addMsg("Warning: When calculating the initial average, we found a gap below the short range");
                }

                avglasta[j] = peakframe[2] * 2;
                expectshort = !expectshort;
            } else if(peakvalue * 2.0D + var23 >= (double)peakframe[2] && peakvalue * 2.0D - var23 <= (double)peakframe[2]) {
                if(j % 2 != 0) {
                    errorLog.addMsg("Warning: When calculating the initial average, possible initial one bits, first zero was found after an odd number of peaks");
                }

                for(range = 0; range < j / 2; ++range) {
                    avglasta[range] = avglasta[range * 2] + avglasta[range * 2 + 1];
                }

                j /= 2;
                avglasta[j] = peakframe[2];
                errorLog.addMsg("Info: This track started with a 1 bit (non standard)");
            } else if(peakvalue + var23 < (double)peakframe[2]) {
                errorLog.addMsg("Warning: When calculating the initial average, we found a gap above high range");
                avglasta[j] = peakframe[2];
            } else if(peakvalue - var23 > (double)peakframe[2] && peakvalue / 2.0D + var23 < (double)peakframe[2]) {
                if(peakvalue - (double)peakframe[2] < (double)peakframe[2] - peakvalue / 2.0D) {
                    avglasta[j] = peakframe[2];
                    errorLog.addMsg("Warning: When calculating the initial average, we found a gap between ranges, closer to high");
                } else {
                    errorLog.addMsg("Warning: When calculating the initial average, we found a gap between ranges, closer to low");
                    avglasta[j] = peakframe[2] * 2;
                    expectshort = !expectshort;
                }
            } else {
                errorLog.addMsg("Warning: Can\'t calculate the initial average for peak " + j + ", skipping");
            }

            String var24 = "Debug: Initial average prediction, frame: " + peakframe[0] + " gap: " + peakframe[2] + " avg gap: " + (float)((int)((peakvalue + var23) * 100.0D)) / 100.0F + " - " + (float)((int)((peakvalue - var23) * 100.0D)) / 100.0F + " " + (float)((int)((peakvalue / 2.0D - var23) * 100.0D)) / 100.0F + " - " + (float)((int)((peakvalue / 2.0D + var23) * 100.0D)) / 100.0F + " array: ";

            for(int k = 0; k < avglasta.length; ++k) {
                var24 = var24 + avglasta[k] + " ";
            }

            errorLog.addMsg(var24);
        }

        expectshort = false;

        for(j = 0; j < this.peaks.size(); ++j) {
            int var21 = ((int[])this.peaks.get(j))[0];
            int var22 = ((int[])this.peaks.get(j))[1];
            int distfromlast = ((int[])this.peaks.get(j))[2];
            var23 = 0.0D;

            for(range = avglastpos; range < avglastpos + avglasta.length; ++range) {
                var23 += (double)avglasta[range % avglasta.length] * ((double)(range - avglastpos) + 1.0D) / (double)avglasta.length;
            }

            var23 /= (double)avglasta.length / 2.0D + 0.5D;
            double var25 = 0.15D * var23;
            if(var23 + var25 >= (double)distfromlast && var23 - var25 <= (double)distfromlast) {
                avglasta[avglastpos] = distfromlast;
                binary = binary + "0";
                expectshort = false;
            } else if(var23 / 2.0D + var25 >= (double)distfromlast && var23 / 2.0D - var25 <= (double)distfromlast) {
                if(expectshort) {
                    binary = binary + "1";
                    avglasta[avglastpos] = distfromlast + ((int[])this.peaks.get(j - 1))[2];
                } else {
                    avglastpos = (avglastpos - 1) % avglasta.length;
                }

                expectshort = !expectshort;
            } else {
                errorLog.addMsg("Warning: Peak didn\'t fit in range, binary decoded: " + binary.length() + " frame: " + var21 + " val: " + ((int[])this.peaks.get(j))[1] + " gap: " + distfromlast + " expecting small gap: " + expectshort + " ranges: " + (float)((int)((var23 - var25) * 100.0D)) / 100.0F + "-" + (float)((int)((var23 + var25) * 100.0D)) / 100.0F + " " + (float)((int)((var23 / 2.0D - var25) * 100.0D)) / 100.0F + "-" + (float)((int)((var23 / 2.0D + var25) * 100.0D)) / 100.0F);
                double erange = 0.2D * var23;
                if(j == 0) {
                    if(var23 + erange >= (double)distfromlast && var23 - erange <= (double)distfromlast) {
                        errorLog.addMsg("Debug: Corrected bit " + (peaksdecoded + 1) + " extending ranges, first peak of track, decoded: 0");
                        binary = binary + "0";
                        avglasta[avglastpos] = distfromlast;
                    } else if(var23 / 2.0D + erange >= (double)distfromlast && var23 / 2.0D - erange <= (double)distfromlast && !expectshort) {
                        errorLog.addMsg("Debug: Corrected peak " + (peaksdecoded + 1) + " extending ranges, first peak of track, decoded: 1");
                        expectshort = !expectshort;
                        avglastpos = (avglastpos - 1) % avglasta.length;
                    } else {
                        errorLog.addMsg("Warning: Can\'t decode first peak, dropping");
                    }
                } else if(this.peaks.size() == j + 1) {
                    if(var23 + erange >= (double)distfromlast && var23 - erange <= (double)distfromlast) {
                        errorLog.addMsg("Debug: Corrected bit " + (peaksdecoded + 1) + " extending ranges, last peak of track, decoded: 0");
                        binary = binary + "0";
                        avglasta[avglastpos] = distfromlast;
                    } else if(var23 / 2.0D + erange >= (double)distfromlast && var23 / 2.0D - erange <= (double)distfromlast && expectshort) {
                        errorLog.addMsg("Debug: Corrected peak " + (peaksdecoded + 1) + " extending ranges, last peak of track, decoded: 1");
                        binary = binary + "1";
                        avglasta[avglastpos] = distfromlast + ((int[])this.peaks.get(j - 1))[2];
                        expectshort = !expectshort;
                    } else {
                        errorLog.addMsg("Warning: Can\'t decode last peak, dropping");
                    }
                } else {
                    int lastvalue = ((int[])this.peaks.get(j - 1))[1];
                    int lastdist = ((int[])this.peaks.get(j - 1))[2];
                    int nextdist = ((int[])this.peaks.get(j + 1))[2];
                    if((double)distfromlast > var23) {
                        if(!expectshort && (lastvalue - this.zerolvl) * (var22 - this.zerolvl) < 0 && (var23 + erange >= (double)distfromlast && var23 - erange <= (double)distfromlast || var23 + erange >= (double)nextdist && var23 - erange <= (double)nextdist || (double)avglasta[(avglastpos + avglasta.length - 1) % avglasta.length] + erange >= (double)distfromlast && (double)avglasta[(avglastpos + avglasta.length - 1) % avglasta.length] - erange <= (double)distfromlast)) {
                            errorLog.addMsg("Warning: Corrected bit " + (peaksdecoded + 1) + " above ranges, first bit, decoded: 0");
                            binary = binary + "0";
                            avglasta[avglastpos] = distfromlast;
                        }
                    } else if((double)distfromlast > var23 / 2.0D) {
                        if(expectshort) {
                            if((lastvalue - this.zerolvl) * (var22 - this.zerolvl) >= 0) {
                                errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + " between ranges, second bit, NOT opposite signage, decoded: 1");
                                binary = binary + "1";
                                avglasta[avglastpos] = distfromlast + lastdist;
                                expectshort = !expectshort;
                            } else if(var23 + erange >= (double)(distfromlast + lastdist) && var23 - erange <= (double)(distfromlast + lastdist) || var23 / 2.0D + erange >= (double)distfromlast && var23 / 2.0D - erange <= (double)distfromlast || (double)avglasta[(avglastpos + avglasta.length - 1) % avglasta.length] + erange >= (double)(distfromlast + lastdist) && (double)avglasta[(avglastpos + avglasta.length - 1) % avglasta.length] - erange <= (double)(distfromlast + lastdist) || (double)(avglasta[(avglastpos + avglasta.length - 1) % avglasta.length] / 2) + erange >= (double)distfromlast && (double)(avglasta[(avglastpos + avglasta.length - 1) % avglasta.length] / 2) - erange <= (double)distfromlast) {
                                errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + " between ranges, second bit, decoded: 0");
                                binary = binary + "0";
                                avglasta[avglastpos] = distfromlast;
                            } else {
                                errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + " between ranges (even extended), second bit, decoded 1");
                                if(var23 - (double)distfromlast < (double)distfromlast - var23 / 2.0D) {
                                    errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + " between ranges, first bit, decoded: 0, based on closest calculation");
                                    binary = binary + "0";
                                    avglasta[avglastpos] = distfromlast;
                                } else {
                                    errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + " between ranges, first bit, decoded: 1, based on closest calculation");
                                    binary = binary + "1";
                                    avglasta[avglastpos] = distfromlast + lastdist;
                                    expectshort = !expectshort;
                                }
                            }
                        } else if(var23 + erange >= (double)distfromlast && var23 - erange <= (double)distfromlast || var23 + erange >= (double)nextdist && var23 - erange <= (double)nextdist || (double)avglasta[(avglastpos + avglasta.length - 1) % avglasta.length] + erange >= (double)distfromlast && (double)avglasta[(avglastpos + avglasta.length - 1) % avglasta.length] - erange <= (double)distfromlast) {
                            if((lastvalue - this.zerolvl) * (var22 - this.zerolvl) < 0) {
                                errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + " between ranges, first bit, decoded: 0");
                                binary = binary + "0";
                                avglasta[avglastpos] = distfromlast;
                            } else {
                                errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + " between ranges, first bit, NOT opposite signage, decoded: 1");
                                binary = binary + "1";
                                avglasta[avglastpos] = distfromlast + lastdist;
                                expectshort = !expectshort;
                            }
                        } else if(var23 / 2.0D + erange >= (double)distfromlast && var23 / 2.0D - erange <= (double)distfromlast || var23 + erange >= (double)(distfromlast + nextdist) && var23 - erange <= (double)(distfromlast + nextdist) || var23 / 2.0D + erange >= (double)nextdist && var23 / 2.0D - erange <= (double)nextdist || 1.5D * var23 + erange >= (double)(distfromlast + avglasta[(avglastpos + avglasta.length - 1) % avglasta.length]) && 1.5D * var23 - erange <= (double)(distfromlast + avglasta[(avglastpos + avglasta.length - 1) % avglasta.length])) {
                            errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + " between ranges, first bit, decoded: 1");
                            expectshort = !expectshort;
                            avglastpos = (avglastpos - 1) % avglasta.length;
                        } else if(var23 - (double)distfromlast < (double)distfromlast - var23 / 2.0D) {
                            errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + " between ranges, first bit, decoded: 0, based on closest calculation");
                            binary = binary + "0";
                            avglasta[avglastpos] = distfromlast;
                        } else {
                            errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + " between ranges, first bit, decoded: 1, based on closest calculation");
                            expectshort = !expectshort;
                            avglastpos = (avglastpos - 1) % avglasta.length;
                        }
                    } else if(expectshort) {
                        if((var23 + erange < (double)(distfromlast + lastdist) || var23 - erange > (double)(distfromlast + lastdist)) && (var23 / 2.0D + erange < (double)distfromlast || var23 / 2.0D - erange > (double)distfromlast) && ((double)avglasta[(avglastpos + avglasta.length - 1) % avglasta.length] + erange < (double)(distfromlast + lastdist) || (double)avglasta[(avglastpos + avglasta.length - 1) % avglasta.length] - erange > (double)(distfromlast + lastdist)) && ((double)(avglasta[(avglastpos + avglasta.length - 1) % avglasta.length] / 2) + erange < (double)distfromlast || (double)(avglasta[(avglastpos + avglasta.length - 1) % avglasta.length] / 2) - erange > (double)distfromlast)) {
                            errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + "below ranges(even extended), second bit, decoded: 1");
                            binary = binary + "1";
                            avglasta[avglastpos] = distfromlast + lastdist;
                            expectshort = !expectshort;
                        } else if((lastvalue - this.zerolvl) * (var22 - this.zerolvl) < 0) {
                            errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + "below ranges, second bit, decoded: 1");
                            binary = binary + "1";
                            avglasta[avglastpos] = distfromlast + lastdist;
                            expectshort = !expectshort;
                        } else {
                            errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + "below ranges, second bit, NOT opposite signage, decoded: 1");
                            binary = binary + "1";
                            avglasta[avglastpos] = distfromlast + lastdist;
                            expectshort = !expectshort;
                        }
                    } else if(var23 / 2.0D + erange >= (double)distfromlast && var23 / 2.0D - erange <= (double)distfromlast || 1.5D * var23 + erange >= (double)(distfromlast + avglasta[(avglastpos + avglasta.length - 1) % avglasta.length]) && 1.5D * var23 - erange <= (double)(distfromlast + avglasta[(avglastpos + avglasta.length - 1) % avglasta.length]) || var23 + erange >= (double)(distfromlast + nextdist) && var23 - erange <= (double)(distfromlast + nextdist) || var23 / 2.0D + erange >= (double)nextdist && var23 / 2.0D - erange <= (double)nextdist) {
                        if((lastvalue - this.zerolvl) * (var22 - this.zerolvl) < 0) {
                            errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + " below ranges, first bit, decoded: 0");
                            expectshort = !expectshort;
                            avglastpos = (avglastpos - 1) % avglasta.length;
                        } else {
                            errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + " below ranges, first bit, NOT opposite signage, decoded: 1");
                            binary = binary + "1";
                            avglasta[avglastpos] = distfromlast + lastdist;
                            expectshort = !expectshort;
                        }
                    } else {
                        errorLog.addMsg("Warning: Corrected peak " + (peaksdecoded + 1) + " below ranges (even extended), first bit, decoded: 1");
                        binary = binary + "1";
                        avglasta[avglastpos] = distfromlast + lastdist;
                        expectshort = !expectshort;
                    }
                }
            }

            avglastpos = (avglastpos + 1) % avglasta.length;
            ++peaksdecoded;
        }

        this.rawlength = (short)binary.length();
        return binary;
    }

    @SuppressLint({"UseValueOf"})
    private String decodeChars() {
        String tempstripped = this.rawbinary;
        if(tempstripped.length() > 6 && tempstripped.startsWith("1") && tempstripped.substring(1, 6).equals("00000")) {
            errorLog.addMsg("Info: Chopping Off Nonstandard Leading One");
            tempstripped = tempstripped.substring(1);
            this.leadingone = true;
        }

        int one = tempstripped.indexOf("1");
        int endsentpos = -1;
        if(one != -1 && tempstripped.length() >= one + 5 && tempstripped.substring(one, one + 5).equals("11010")) {
            errorLog.addMsg("Info: 5 Bit Start Sentinal Found At Raw Position: " + one);
            tempstripped = tempstripped.substring(one);
            this.leadingzeros = (short)one;
            this.bitsperchar = 5;

            do {
                endsentpos = tempstripped.indexOf("11111", endsentpos + 1);
            } while(endsentpos % 5 != 0 && endsentpos != -1);

            if(endsentpos == -1) {
                errorLog.addMsg("Warning: No 5 Bit End Sentinal Found");
            } else {
                errorLog.addMsg("Info: 5 Bit End Sentinal Found At Raw Position: " + endsentpos);
            }
        } else if(one != -1 && tempstripped.length() >= one + 7 && tempstripped.substring(one, one + 7).equals("1010001")) {
            errorLog.addMsg("Info: 7 Bit Start Sentinal Found At Raw Position: " + one);
            tempstripped = tempstripped.substring(one);
            this.leadingzeros = (short)one;
            this.bitsperchar = 7;

            do {
                endsentpos = tempstripped.indexOf("1111100", endsentpos + 1);
            } while(endsentpos % 7 != 0 && endsentpos != -1);

            if(endsentpos == -1) {
                errorLog.addMsg("Warning: No 7 Bit End Sentinal Found");
            } else {
                errorLog.addMsg("Info: 7 Bit End Sentinal Found At Raw Position: " + endsentpos);
            }
        } else {
            if(one == -1) {
                errorLog.addMsg("Error: There Was No \'1\' Bit Found In The Track. Can\'t Decode");
                return "";
            }

            if(!this.swipedreverse) {
                if((tempstripped.lastIndexOf("01011") == -1 || tempstripped.indexOf("1", tempstripped.lastIndexOf("01011") + 5) != -1) && (tempstripped.lastIndexOf("01011") == -1 || !tempstripped.endsWith("1") || tempstripped.lastIndexOf("1", tempstripped.length() - 1) == -1) && (tempstripped.lastIndexOf("1000101") == -1 || tempstripped.indexOf("1", tempstripped.lastIndexOf("1000101") + 7) != -1) && (tempstripped.lastIndexOf("1000101") == -1 || !tempstripped.endsWith("1") || tempstripped.lastIndexOf("1", tempstripped.length() - 1) == -1)) {
                    this.leadingzeros = (short)tempstripped.indexOf("1");
                    this.trailingzeros = (short)(tempstripped.length() - tempstripped.lastIndexOf("1") - 1);
                    errorLog.addMsg("Warning: No Start Sentinel Found, We Can\'t Decode Any Chars");
                    return "";
                }

                errorLog.addMsg("Info: Probably Swiped The Wrong Way, Reversing To Decode");
                this.swipedreverse = true;
                this.rawbinary = (new StringBuffer(tempstripped)).reverse().toString();
                return this.decodeChars();
            }
        }

        if((endsentpos == -1 || this.bitsperchar != 5 || tempstripped.length() < endsentpos + 2 * this.bitsperchar || tempstripped.substring(endsentpos, endsentpos + this.bitsperchar).equals("00000")) && (endsentpos == -1 || this.bitsperchar != 7 || tempstripped.length() < endsentpos + 2 * this.bitsperchar || tempstripped.substring(endsentpos, endsentpos + this.bitsperchar).equals("0000000"))) {
            if(endsentpos != -1) {
                errorLog.addMsg("Warning: LRC Not Found");
                tempstripped = tempstripped.substring(0, endsentpos + 5);
                this.trailingzeros = (short)(tempstripped.length() - endsentpos - this.bitsperchar);
            } else if(endsentpos == -1) {
                errorLog.addMsg("Info: Can\'t Find LRC Without End Sentinel");
            }
        } else {
            this.trailingzeros = (short)(tempstripped.length() - endsentpos - 2 * this.bitsperchar);
            tempstripped = tempstripped.substring(0, endsentpos + 2 * this.bitsperchar);
            this.lrcerr = this.verifyLRC(tempstripped);
            this.lrc = tempstripped.substring(endsentpos + this.bitsperchar, endsentpos + 2 * this.bitsperchar);
            tempstripped = tempstripped.substring(0, endsentpos + this.bitsperchar);
        }

        if(this.bitsperchar > 0) {
            this.crcerr = this.verifyCRC(tempstripped);
            int chartoreplace;
            if(this.crcerr.size() == 1 && !this.lrcerr.contains(new Integer(this.bitsperchar)) && this.lrcerr.size() == 1) {
                chartoreplace = ((Integer)this.crcerr.get(0)).intValue() * this.bitsperchar + ((Integer)this.lrcerr.get(0)).intValue();
                if(chartoreplace < tempstripped.length() && chartoreplace >= 0) {
                    errorLog.addMsg("Warning: Trying To Correct Char " + ((Integer)this.crcerr.get(0)).intValue() + " Position " + ((Integer)this.lrcerr.get(0)).intValue() + " Bit " + chartoreplace);
                    tempstripped = tempstripped.substring(0, chartoreplace) + (Integer.valueOf(tempstripped.substring(chartoreplace, chartoreplace + 1)).byteValue() ^ 1) + (chartoreplace == tempstripped.length() - 1?"":tempstripped.substring(chartoreplace + 1, tempstripped.length()));
                    this.crccorrections.add(new Integer(chartoreplace));
                } else {
                    errorLog.addMsg("Error: Tried to CRC correct a bit off the end of the string");
                }
            } else if(this.crcerr.size() == 1 && this.lrcerr.size() == 0) {
                errorLog.addMsg("Warning: LRC Passes, CRC Error At Char " + this.crcerr.get(0) + " Probably Due To CRC Being Wrong");
                chartoreplace = ((Integer)this.crcerr.get(0)).intValue() * this.bitsperchar + this.bitsperchar - 1;
                tempstripped = tempstripped.substring(0, chartoreplace) + (Integer.valueOf(tempstripped.substring(chartoreplace, chartoreplace + 1)).byteValue() ^ 1) + (chartoreplace == tempstripped.length() - 1?"":tempstripped.substring(chartoreplace + 1, tempstripped.length()));
                this.crccorrections.add(new Integer(chartoreplace));
            }
        }

        return tempstripped;
    }

    public static String decodeBin(String binary) {
        Pattern[] encodings = new Pattern[]{Pattern.compile("^1?0*(11010)(([01]{5})*)(11111)([01]{5})?0*$"), Pattern.compile("^1?0*(1010001)(([01]{7})*)(1111100)([01]{7})?0*$")};
        String ss = null;
        String payload = null;
        String es = null;
        String lrc = null;

        for(int decoded = 0; decoded < encodings.length && ss == null; ++decoded) {
            Matcher m = encodings[decoded].matcher(binary);
            if(m.matches()) {
                ss = m.group(1);
                payload = m.group(2);
                es = m.group(4);
                lrc = m.group(5);
            }
        }

        String var8 = null;
        if(ss != null) {
            if(ss.equals("11010")) {
                var8 = decodeABA(ss + payload + es, 5, errorLog);
            } else if(ss.equals("1010001")) {
                var8 = decodeIATA(ss + payload + es, 7, errorLog);
            }
        }

        return var8;
    }

    @SuppressLint({"UseValueOf"})
    private ArrayList<Integer> verifyCRC(String binary) {
        if(binary.length() % this.bitsperchar != 0) {
            errorLog.addMsg("Warning: We Have An Odd Amount Of Bits To Verify The CRC. Dropping " + binary.length() % this.bitsperchar + " Bits");
            binary = binary.substring(0, binary.length() - binary.length() % this.bitsperchar);
        }

        ArrayList crcerr = new ArrayList();

        for(int i = 0; i < binary.length(); i += this.bitsperchar) {
            byte par = 0;
            byte val = 0;

            for(int j = 0; j < this.bitsperchar - 1; ++j) {
                par = (byte)(par + Integer.valueOf(binary.substring(i + j, i + j + 1)).intValue());
                val = (byte)((int)((double)val + Math.pow(2.0D, (double)j) * (double)Integer.valueOf(binary.substring(i + j, i + j + 1)).intValue()));
            }

            if((par & 1) == Integer.valueOf(binary.substring(i + this.bitsperchar - 1, i + this.bitsperchar)).intValue()) {
                errorLog.addMsg("Warning: Parity Failure at char: " + i / this.bitsperchar + " " + binary.substring(i, i + this.bitsperchar));
                crcerr.add(new Integer(i / this.bitsperchar));
            }
        }

        return crcerr;
    }

    public static String decodeABA(String bin, int abpc, ErrorLog el) {
        String decoded = "";

        for(int i = 0; i < bin.length() - abpc + 1; i += abpc) {
            int val = 0;

            for(int j = 0; j < abpc - 1 && j < bin.length(); ++j) {
                val = (int)((double)val + Math.pow(2.0D, (double)j) * (double)Integer.valueOf(bin.substring(i + j, i + j + 1)).intValue());
            }

            decoded = decoded + (char)(val + 48);
        }

        if(decoded.length() > 40 && el != null) {
            el.addMsg("Info: Decoded More Characters Than Is Specified By The Track 2 ANSI/ISO Standards");
        } else if(decoded.length() > 107 && el != null) {
            el.addMsg("Info: Decoded More Characters Than Is Specified By The Track 3 ANSI/ISO Standards");
        }

        return decoded;
    }

    public static String decodeIATA(String bin, int abpc, ErrorLog el) {
        String decoded = "";

        for(int i = 0; i < bin.length() + 1 - abpc; i += abpc) {
            int val = 0;

            for(int j = 0; j < abpc - 1 && j < bin.length(); ++j) {
                val = (int)((double)val + Math.pow(2.0D, (double)j) * (double)Integer.valueOf(bin.substring(i + j, i + j + 1)).intValue());
            }

            decoded = decoded + (char)(val + 32);
        }

        if(decoded.length() > 79 && el != null) {
            el.addMsg("Warning: Decoded More Characters Than Is Specified By The Track 1 ANSI/ISO Standards");
        }

        return decoded;
    }

    @SuppressLint({"UseValueOf"})
    private ArrayList<Integer> verifyLRC(String binary) {
        if(binary.length() % this.bitsperchar != 0) {
            errorLog.addMsg("Error: We Have An Odd Amount Of Bits To Verify The LRC. Can Not Check LRC");
            binary.substring(0, binary.length() - binary.length() % this.bitsperchar);
        } else {
            for(int par = 0; par < this.bitsperchar - 1; ++par) {
                byte i = 0;

                for(int j = 0; j < binary.length() / this.bitsperchar - 1; ++j) {
                    i = (byte)(i + Integer.valueOf(binary.substring(par + this.bitsperchar * j, par + this.bitsperchar * j + 1)).intValue());
                }

                if((i & 1) != Integer.valueOf(binary.substring(binary.length() - this.bitsperchar + par, binary.length() - this.bitsperchar + 1 + par)).intValue()) {
                    errorLog.addMsg("Warning: LRC Failure At Bit: " + par);
                    this.lrcerr.add(new Integer(par));
                }
            }

            byte var5 = 0;

            for(int var6 = binary.length() - this.bitsperchar; var6 < binary.length() - 1; ++var6) {
                var5 = (byte)(var5 + Integer.valueOf(binary.substring(var6, var6 + 1)).intValue());
            }

            if((var5 & 1) == Integer.valueOf(binary.length() - 1).intValue()) {
                errorLog.addMsg("Warning: LRC CRC Failure, LRC Corrections Can Not Be Calculated");
                this.lrcerr.add(new Integer(this.bitsperchar));
            }
        }

        return this.lrcerr;
    }

    public String getSwipedReverse() {
        return this.swipedreverse?"Yes":"No";
    }

    public ArrayList<Integer> getCRCErrors() {
        return this.crcerr;
    }

    public ArrayList<Integer> getLRCErrors() {
        return this.lrcerr;
    }

    public String getDecodedString() {
        return this.decodedString;
    }

    public byte getBitsPerChar() {
        return this.bitsperchar;
    }

    public String getRawBinary() {
        return this.rawbinary;
    }

    public String getStrippedBinary() {
        return this.strippedbinary;
    }

    public short getTotalPosPeaks() {
        return this.totalpospeaks;
    }

    public short getTotalNegPeaks() {
        return this.totalnegpeaks;
    }

    public short getZeroLevel() {
        return this.zerolvl;
    }

    public String getLRC() {
        return this.lrc;
    }

    public String getLeadingOne() {
        return this.leadingone?"Yes":"No";
    }

    public short getLeadingZeros() {
        return this.leadingzeros;
    }

    public short getTrailingZeros() {
        return this.trailingzeros;
    }

    public short getStrippedLength() {
        return (short)this.strippedbinary.length();
    }

    public short getRawLength() {
        return this.rawlength;
    }

    public ErrorLog getErrorLog() {
        return errorLog;
    }

    @SuppressLint({"SimpleDateFormat"})
    public String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy HH:mm:ss.SSS ");
        return sdf.format(this.decodeTime);
    }

    public String getChannels() {
        return this.channels == 1?"Mono":"Stereo";
    }

    public String getFormat() {
        return this.aformat;
    }

    public float getSampleRate() {
        return this.samplerate;
    }

    public long getFrameLength() {
        return this.framelength;
    }

    public int getBitsPerSample() {
        return this.bitspersample;
    }

    public String getBitOrder() {
        return this.bitorder?"Big Endian":"Little Endian";
    }

    public short getPosThres() {
        return this.currposthres;
    }

    public short getNegThres() {
        return this.currnegthres;
    }

    public byte[] getStream() {
        return this.bis;
    }
}
