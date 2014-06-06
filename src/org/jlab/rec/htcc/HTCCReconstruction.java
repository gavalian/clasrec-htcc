/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.rec.htcc;

import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioSource;

/**
 *
 * @author gavalian
 */
public class HTCCReconstruction {
    public void processEvent(EvioDataEvent event){
        EvioDataBank bankTRUE = (EvioDataBank) event.getBank("HTCC:true");
        EvioDataBank bankDGTZ = (EvioDataBank) event.getBank("HTCC:dgtz");
        
        int[] sector = bankDGTZ.getInt("sector");
        
    }
    /**
     * Main routine for testing.
     * @param args 
     */
    public static void main(String[] args){
        String inputfile = args[0];
        String dictdir   = args[1];
        
        EvioSource reader = new EvioSource(dictdir);
        reader.open(inputfile);
        
        HTCCReconstruction htccRec = new HTCCReconstruction();
        while(reader.hasEvent()){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            htccRec.processEvent(event);
        }
    }
}
