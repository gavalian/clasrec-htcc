package org.jlab.rec.htcc;

import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioSource;

/**
 *
 * @author J. Hankins
 * @author A. Puckett
 * @author G. Gavalian
 */
public class HTCCReconstruction {
    private final HTCCDetectorProcess htccDetectorProcess;
    private final OTOFDetectorProcess otofDetectorProcess;
    
    public HTCCReconstruction() {
        htccDetectorProcess = new HTCCDetectorProcess();
        otofDetectorProcess = new OTOFDetectorProcess();
    }
    
    public void processEvent(EvioDataEvent event) {
        htccDetectorProcess.processEvent(event);
        otofDetectorProcess.processEvent(event);
    }
    
    /**
     * Main routine for testing.
     * 
     * The environment variable $CLAS12DIR must be set and point to a directory 
     * that contains lib/bankdefs/clas12/<dictionary file name>.xml
     *
     * @param args ignored
     */
    public static void main(String[] args){
        String inputfile = "out.ev";
        
        EvioSource reader = new EvioSource();
        reader.open(inputfile);
        
        HTCCReconstruction htccRec = new HTCCReconstruction();
        while(reader.hasEvent()){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            htccRec.processEvent(event);
        }
    }
}
