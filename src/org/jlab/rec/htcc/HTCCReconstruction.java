package org.jlab.rec.htcc;

import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioSource;

/**
 *
 * @author gavalian
 */
public class HTCCReconstruction {
    private final HTCCDetectorProcess detectorProcess;
    
    public HTCCReconstruction() {
        detectorProcess = new HTCCDetectorProcess();
    }
    
    public void processEvent(EvioDataEvent event) {
        detectorProcess.processEvent(event);
        
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
