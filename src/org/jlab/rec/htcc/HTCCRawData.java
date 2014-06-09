package org.jlab.rec.htcc;

import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;

/**
 *
 * @author Jeremiah Hankins
 */
public class HTCCRawData {
    private int[] hitn;
    private int[] sector;
    private int[] ring;
    private int[] half;
    private int[] nphe;
    private int[] time;

    void loadEventData(EvioDataEvent event) {
        //EvioDataBank bankTRUE = (EvioDataBank) event.getBank("HTCC::true");
        EvioDataBank bankDGTZ = (EvioDataBank) event.getBank("HTCC::dgtz");
        
        hitn   = bankDGTZ.getInt("hitn");
        sector = bankDGTZ.getInt("sector");
        ring   = bankDGTZ.getInt("ring");
        half   = bankDGTZ.getInt("half");
        nphe   = bankDGTZ.getInt("nphe");
        time   = null;
    }
    
    public int getNHits() {
        return hitn.length;
    }
    public int getHitN(int hit) {
        return hitn[hit];
    }
    public int getSector(int hit) {
        return sector[hit];
    }
    public int getRing(int hit) {
        return ring[hit];
    }
    public int getHalf(int hit) {
        return half[hit];
    }
    public int getNPhe(int hit) {
        return nphe[hit];
    }
    public double getTime(int hit) {
        return time[hit];
    }
    public int getITheta(int hit) {
        return ring[hit] - 1;
    }
    public int getIPhi(int hit) {
        int iphi = 2*sector[hit] + half[hit] - 3;
        return (iphi == 0 ? iphi + 12 : iphi) - 1;
    }
}
