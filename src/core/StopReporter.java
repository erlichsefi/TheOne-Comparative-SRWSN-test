package core;

public class StopReporter {
     static boolean stopWasReported=false;

    public StopReporter(){
        this.stopWasReported=false;
    }
     public StopReporter(StopReporter r){
     this.stopWasReported=r.stopWasReported;
     }

     public void reportedStop(StopReporter r){
        if (r.wasreported()){
            stopWasReported=true;
        }
    }

    public boolean wasreported(){
        return stopWasReported;
    }
}
