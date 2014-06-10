package org.jlab.rec.htcc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;

/**
 *
 * @author Jeremiah Hankins
 */
public class OTOFDetectorProcess {
    
    public OTOFDetectorProcess() { }
    
    public void processEvent(EvioDataEvent event) {
        EvioDataBank bankTRUE = (EvioDataBank) event.getBank("HTCC::true");
        EvioDataBank bankDGTZ = (EvioDataBank) event.getBank("HTCC::dgtz");
        
        // TODO: check to see if these are the correct columns
        int[]    sectorIn = bankDGTZ.getInt("sector");
        int[]    paddleIn = bankDGTZ.getInt("ring");
        int[]    pidIn    = bankTRUE.getInt("pid");
        double[] energyIn = bankTRUE.getDouble("totEdep");
        double[] timeIn   = bankTRUE.getDouble("avg_t");
        double[] hitXIn   = bankTRUE.getDouble("avg_t");
        double[] hitYIn   = bankTRUE.getDouble("avg_t");
        double[] hitZIn   = bankTRUE.getDouble("avg_t");
        
        int nhitsIn = energyIn.length;
        
        int nhitsOut = 0;
        for (int i=0; i<nhitsIn; ++i)
            if (energyIn[i] > 1.0)
                nhitsOut++;
        
        int[]    sectorOut = new int[nhitsOut];
        int[]    paddleOut = new int[nhitsOut];
        int[]    pidOut    = new int[nhitsOut];
        int[]    layerOut  = new int[nhitsOut];
        double[] energyOut = new double[nhitsOut];
        double[] timeOut   = new double[nhitsOut];
        double[] hitXOut   = new double[nhitsOut];
        double[] hitYOut   = new double[nhitsOut];
        double[] hitZOut   = new double[nhitsOut];
        
        
        int out = 0;
        for (int in=0; in<nhitsIn; ++in) {
            if (energyIn[in] > 1.0) {
                sectorOut[out] = sectorIn[in];
                paddleOut[out] = paddleIn[in];
                pidOut[out]    = pidIn[in];
                layerOut[out]  = 1;
                energyOut[out] = energyIn[in];
                timeOut[out]   = timeIn[in];
                hitXOut[out]   = hitXIn[in];
                hitYOut[out]   = hitYIn[in];
                hitZOut[out]   = hitZIn[in];
            }
        }
        
        System.out.printf("[Detector-FTOF >>>> Input hits %8d Output Hits %8d\n", nhitsIn , nhitsOut);
        System.out.println("sector: "+Arrays.toString(sectorOut));
        System.out.println("paddle: "+Arrays.toString(paddleOut));
        System.out.println("   pid: "+Arrays.toString(pidOut));
        System.out.println(" layer: "+Arrays.toString(layerOut));
        System.out.println("energy: "+Arrays.toString(energyOut));
        System.out.println("  time: "+Arrays.toString(timeOut));
        System.out.println("  hitX: "+Arrays.toString(hitXOut));
        System.out.println("  hitY: "+Arrays.toString(hitYOut));
        System.out.println("  hitZ: "+Arrays.toString(hitZOut));
        
        // TODO figureout which bank to use
        EvioDataBank bank = (EvioDataBank) event.getBank("HTCC::??");
        bank.setInt("sector", sectorOut);
        bank.setInt("paddle", paddleOut);
        bank.setInt("pid", pidOut);
        bank.setInt("layer", layerOut);
        bank.setDouble("energy", energyOut);
        bank.setDouble("time", timeOut);
        bank.setDouble("hitX", hitXOut);
        bank.setDouble("hitY", hitYOut);
        bank.setDouble("hitZ", hitZOut);
    }
}
