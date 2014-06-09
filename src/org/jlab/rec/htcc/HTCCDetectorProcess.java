package org.jlab.rec.htcc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.evio.clas12.EvioDataEvent;

/**
 *
 * @author Jeremiah Hankins
 */
public class HTCCDetectorProcess {
    
    private final ReconstructionParameters parameters;
    private final HTCCRawData rawData;
    
    private int maxHitNumPhotoelectrons;
    private int maxHitRemainingIndex;
    private int maxHitRawDataIndex;
    
    /**
     * Initializes the HTCCDetectorProcess().
     */
    public HTCCDetectorProcess() {
        parameters = new ReconstructionParameters();
        rawData = new HTCCRawData();
    }
    
    public void processEvent(EvioDataEvent event) {
        // Load the raw data about the event
        rawData.loadEventData(event);
        
        // Initialize the remaining hits list
        List<Integer> remainingHits = getRemainingHits();
        
        // Place all of the hits into clusters
        List<HTCCCluster> clusters = new ArrayList();
        while (remainingHits.size() > 0) {
            HTCCCluster cluster = findCluster(remainingHits);
            if (cluster != null)
                clusters.add(cluster);
            else
                break;
        }
        
        // Display Results
        System.out.printf("[Detector-HTCC] >>>> Input hits %8d Output Clusters %8d\n", 
                rawData.getNHits(), clusters.size());
        
        // Push all of the clusters into the bank
        fillBankResults(clusters, event);
    }
    
    List<Integer> getRemainingHits() {
        List<Integer> remainingHits = new ArrayList();
        
        // Get the number of hits in this event
        int num_hits = rawData.getNHits();
        
        // Find all hits above the hit threshold
        for (int i=0; i<num_hits; ++i) {
            if (rawData.getNPhe(i) > parameters.npeminhit) {
                remainingHits.add(i);
            }
        }
        
        return remainingHits;
    }
    
    /**
     * Returns the next cluster or null if no clusters are left.
     * @param remainingHits the list of remaining hits
     * @return the next cluster or null if no clusters are left
     */
    HTCCCluster findCluster(List<Integer> remainingHits) {
        // maxHitNumPhotoelectrons : the number of photoelectrons for the maximum hit
        // maxHitRawDataIndex : the index of the hit within rawData
        // maxHitRemainingIndex : the index of the hit within remainingHits
        
        // Find the hit from the list of remaining hits with the largest number 
        // of photoelectrons that also meets the threshold for the minimum 
        // number of photoelectrons specified by parameters.npheminmax
        findMaximumHit(remainingHits);
        
        // If a maximum hit was found:
        if (maxHitNumPhotoelectrons > 0) {
            
            // Remove the maximum hit from the list of remaining hits
            remainingHits.remove(maxHitRemainingIndex);
            
            // Get Hit Data:
            // Detector Indicies
            int    itheta = rawData.getITheta(maxHitRawDataIndex);
            int    iphi   = rawData.getIPhi(maxHitRawDataIndex);
            // Numver of Photoelectrons
            int    nphe   = maxHitNumPhotoelectrons;
            // Hit Time
            double time   = rawData.getTime(maxHitRawDataIndex) - parameters.t0[itheta];
            // Detector Coordinates (polar)
            double theta  = parameters.theta0[itheta];
            double phi    = parameters.phi0 + 2.0*parameters.dphi0*iphi;
            // Detector Alignment Errors
            double dtheta = parameters.dtheta0[itheta];
            double dphi   = parameters.dphi0;
            
            // Create a new cluster and add the maximum hit
            HTCCCluster cluster = new HTCCCluster();
            cluster.addHit(itheta, iphi, nphe, time, theta, phi, dtheta, dphi);
                    
            // Recursively grow the cluster by adding nearby hits
            growCluster(cluster, remainingHits);
            
            //Check whether this cluster has nphe above threshold, size along theta and phi and total number of hits less than maximum:
            if( cluster.getNPheTot() >= parameters.npeminclst && 
                cluster.getNThetaClust() <= parameters.nthetamaxclst && 
                cluster.getNPhiClust() <= parameters.nphimaxclst && 
                cluster.getNHitClust() <= parameters.nhitmaxclst ){
                
                // Return the cluster
                return cluster;
            }
        }
        
        // There are no clusters left, so return null
        return null;
    }
    
    /**
     * Finds the hit from the list of remaining hits with the largest number of
     * photoelectrons that also meets the threshold for the minimum number of
     * photoelectrons specified by <code>parameters.npheminmax</code>.
     * 
     * Side effects:
     * If a maximum hit was found with a number of photo electrons greater than 
     * or equal to parameters.npheminmax, then:
     * maxHitNumPhotoelectrons = the number of photoelectrons for the max hit
     * maxHitRawDataIndex      = the index of the hit within rawData
     * maxHitRemainingIndex    = the index of the hit within remainingHits
     * 
     * If no remaining hit has a number of photoelectrons greater than or equal 
     * to parameters.npheminmax, then:
     * maxHitNumPhotoelectrons = -1
     * maxHitRawDataIndex      = -1
     * maxHitRemainingIndex    = -1
     * 
     * @param remainingHits the list of remaining hits
     */
    void findMaximumHit(List<Integer> remainingHits) {
        maxHitNumPhotoelectrons = -1; // the number of photoelectrons for the maximum hit
        maxHitRemainingIndex = -1; // the index of the hit within remainingHits
        maxHitRawDataIndex = -1; // the index of the hit within rawData
        for (int hit=0; hit<remainingHits.size(); ++hit) {
            int hitIndex = remainingHits.get(hit);
            int numPhotoElectrons = rawData.getNPhe(hitIndex);
            if (numPhotoElectrons >= parameters.npheminmax && 
                numPhotoElectrons > maxHitNumPhotoelectrons) {
                maxHitNumPhotoelectrons = numPhotoElectrons;
                maxHitRemainingIndex = hit;
                maxHitRawDataIndex = hitIndex;
            }
        }
    }
    
    /**
     * Grows the given cluster by adding nearby hits from the remaining hits 
     * list.  As hits are added to the cluster they are removed from the 
     * remaining hits list.
     * @param cluster the cluster
     * @param remainingHits the remaining hits
     */
    void growCluster(HTCCCluster cluster, List<Integer> remainingHits) {
        // Get the average time of the cluster
        double clusterTime = cluster.getTime();
        // For each hit in the cluster:
        for (int currHit=0; currHit<cluster.getNHitClust(); ++currHit) {
            // Get the hits coordinates
            int ithetaCurr = cluster.getHitITheta(currHit);
            int iphiCurr   = cluster.getHitIPhi(currHit);
            // For each of the remaining hits:
            int hit = 0;
            while (hit < remainingHits.size()) {
                // Get the index of the remaining hit (and call it a test hit)
                int testHit = remainingHits.get(hit);
                // Get the coordinates of the test hit
                int ithetaTest = rawData.getITheta(testHit);
                int iphiTest   = rawData.getIPhi(testHit);
                // Find the distance
                int ithetaDiff = Math.abs(ithetaTest - ithetaCurr);
                int iphiDiff = Math.min((12+iphiTest-iphiCurr)%12, (12+iphiCurr-iphiTest)%12);
                // Find the difference in time
                double time = rawData.getTime(testHit) - parameters.t0[ithetaTest];
                double timeDiff = Math.abs(time - clusterTime);
                // If the test hit is close enough in space and time
                if ((ithetaDiff == 1 || iphiDiff == 1) &&
                    (ithetaDiff + iphiDiff <= 2) &&
                    (timeDiff <= parameters.maxtimediff)) {
                    // Remove the hit from the remaining hits list
                    remainingHits.remove(hit);
                    // Get the Numeber of Photoelectrons
                    int    npheTest   = rawData.getNPhe(testHit);
                    // Get the Detector Coordinates (polar)
                    double thetaTest  = parameters.theta0[ithetaTest];
                    double phiTest    = parameters.phi0 + 2.0*parameters.dphi0*iphiTest;
                    // Get the Detector Alignment Errors
                    double dthetaTest = parameters.dtheta0[ithetaTest];
                    double dphiTest   = parameters.dphi0;
                    // Add the hit to the cluster
                    cluster.addHit(ithetaTest, iphiTest, npheTest, time, thetaTest, phiTest, dthetaTest, dphiTest);
                    // Get the new average time of the cluster
                    clusterTime = cluster.getTime();
                } else {
                    // Go to the next hit in the remaining hits list
                    hit++;
                }
            }
        }
    }
    
    void fillBankResults(List<HTCCCluster> clusters, EvioDataEvent event) {
        
        int size = clusters.size();
        
        int[] nhits    = new int[size];
        int[] ntheta   = new int[size];
        int[] nphi     = new int[size];
        int[] mintheta = new int[size];
        int[] maxtheta = new int[size];
        int[] minphi   = new int[size];
        int[] maxphi   = new int[size];
        int[] nphe     = new int[size];
        double[] time   = new double[size];
        double[] theta  = new double[size];
        double[] dtheta = new double[size];
        double[] phi    = new double[size];
        double[] dphi   = new double[size];
        
        for(int i=0; i<size; ++i){
            HTCCCluster cluster = clusters.get(i);
            nhits[i]    = cluster.getNHitClust();
            ntheta[i]   = cluster.getNThetaClust();
            nphi[i]     = cluster.getNPhiClust();
            mintheta[i] = cluster.getIThetaMin();
            maxtheta[i] = cluster.getIThetaMax();
            minphi[i]   = cluster.getIPhiMin();
            maxphi[i]   = cluster.getIPhiMax();
            nphe[i]     = cluster.getNPheTot();
            time[i]     = cluster.getTime();
            theta[i]    = cluster.getTheta();
            phi[i]      = cluster.getPhi();
            dtheta[i]   = cluster.getDTheta();
            dphi[i]     = cluster.getDPhi();
        }
        
        // TODO add the arrays to the bank
        
        System.out.println("   nhits: "+Arrays.toString(nhits));
        System.out.println("  ntheta: "+Arrays.toString(ntheta));
        System.out.println("    nphi: "+Arrays.toString(nphi));
        System.out.println("mintheta: "+Arrays.toString(mintheta));
        System.out.println("maxtheta: "+Arrays.toString(maxtheta));
        System.out.println("  minphi: "+Arrays.toString(minphi));
        System.out.println("  maxphi: "+Arrays.toString(maxphi));
        System.out.println("    nphe: "+Arrays.toString(nphe));
        System.out.println("    time: "+Arrays.toString(time));
        System.out.println("   theta: "+Arrays.toString(theta));
        System.out.println("  dtheta: "+Arrays.toString(dtheta));
        System.out.println("     phi: "+Arrays.toString(phi));
        System.out.println("    dphi: "+Arrays.toString(dphi));
    }
    
    
    /**
     * Contains the HTCC reconstruction parameters.
     * Note: Used to be implemented with GooPropertyList.
     */
    class ReconstructionParameters {
        double theta0[];
        double dtheta0[];
        double phi0;
        double dphi0;
        int npeminclst;
        int npheminmax;
        int npeminhit;
        int nhitmaxclst;
        int nthetamaxclst;
        int nphimaxclst;
        double maxtimediff;
        double t0[];
        
        /**
         * Initialize reconstruction parameters with sensible defaults.
         */
        ReconstructionParameters() {
            theta0 = new double[] { 8.75, 16.25, 23.75, 31.25 };
            dtheta0 = new double[] {3.75, 3.75, 3.75, 3.75 } ;
            for (int i=0; i<4; ++i) {
                theta0[i] = Math.toRadians(theta0[i]);
                dtheta0[i] = Math.toRadians(dtheta0[i]);
            }
            phi0 = Math.toRadians(15.0);
            dphi0 = Math.toRadians(15.0);
            npeminclst = 1;
            npheminmax = 1;
            npeminhit = 1;
            nhitmaxclst = 4;
            nthetamaxclst = 2;
            nphimaxclst = 2;
            maxtimediff = 2;
            t0 = new double[] { 11.54, 11.93, 12.33, 12.75 };
        }
        
        /**
         * Initialize reconstruction parameters from a packed string.
         * @param packed_string 
         * @throws UnsupportedOperationException
         */
        ReconstructionParameters(String packed_string) {
            // TODO if necessary
            throw new UnsupportedOperationException("ReconstructionParameters(String packed_string)");
        }
    }
}
