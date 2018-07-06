package dtsn.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import core.Message;
/**
 * the middle cache
 * @author erlichsefi
 *
 */
public class Cache<T extends DtsnMessage> {
	/*
	 * data storage
	 */
	private HashMap<String, C_unit> cache ;

     /**
      * constructor 
      */
     public Cache(){
          cache = new HashMap<String, C_unit>();
     }
     /**
      * add entry
      * @param _appId the app id of the app involved
      * @param message the message to store
      * @param prob a probability  to assign the message 
      * @param sessionId the session id in the app 
      * @param sourceId the source node
      * @param destId   the dest node
      */
    public void Add(String _appId,DtsnMessage message, int sessionId, int sourceId, int destId){
    	C_unit unit=new C_unit(_appId, message  ,sessionId ,sourceId ,destId);
    	cache.put(KeyCache(sourceId,destId,message.getSeqNum()), unit);
    }
	
    /**
     * the way we compute the key
     * @param SourceId the source node
     * @param DestId the dest node
     * @param Seq the sequence number of the message
     * @return the key for the has map
     */
	private static String KeyCache(int SourceId,int DestId,int Seq){
		return SourceId+"_"+DestId+"_"+Seq;
	}
	
	/**
	 * remove old data that contain the same the same source<-> dest and app id and is not the current session
	 * @param SourceId the source node
	 * @param DestId the dest node
	 * @param appId the app id to remove from
	 * @param CurrentSession the current session id to keep
	 */
	public void RemoveOldSessions(int SourceId,int DestId,String appId,int CurrentSession){
		ArrayList<String> to_remove=new ArrayList<String>();
         for (Entry<String, C_unit> entry : cache.entrySet()) {
			C_unit value=	entry.getValue();
			if (value.DestId==DestId && value.SourceId==SourceId && value.appId.endsWith(appId)  && value.sessionId!=CurrentSession){
				to_remove.add(entry.getKey());
			}
		}
         for (int i = 0; i < to_remove.size(); i++) {
        	 cache.remove(to_remove.get(i));
		}
	}
	
	/**
	 * check if a sequence number is in the cache
	 * @param Seq a sequence to look for
	 * @return true if inside
	 */
	public boolean IsInCache(int Seq){
		for (Entry<String, C_unit> entry : cache.entrySet()) {
			C_unit value=	entry.getValue();
			if (value.message.getSeqNum()==Seq){
				return true;
			}
		}
		return false;
	}
   /**
    * get message from the cache
    * @param Seq a sequence to look for
    * @return  a pointer to the message (please use Message#myreplicate())
    */
	public T getMesage(int Seq) {
		for (Entry<String, C_unit> entry : cache.entrySet()) {
			C_unit value=	entry.getValue();
			if (value.message.getSeqNum()==Seq){
				return (T)value.message;
			}
		}
		return null;
		
	}
	/**
	 * get the number of elements in the cache
	 * @return
	 */
	public int getSize() {
		return cache.size();
	}
	
	public T[] getAllinCache(String[] seq) {
		ArrayList<DtsnMessage> ans=new ArrayList<DtsnMessage>();
		for (int i = 0; i < seq.length; i++) {
			DtsnMessage newM=getMesage(Integer.parseInt(seq[i]));
			if (newM!=null)ans.add(newM);
		}
		DtsnMessage[] a=new DtsnMessage[ans.size()];
		for (int i = 0; i < a.length; i++) {
			a[i]=ans.get(i);
		}
		return (T[])a;
	}
	
	public Message getNextSeqMessage(int to) {
        for (Entry<String, C_unit> entry : cache.entrySet()) {
			C_unit value=	entry.getValue();
			if (value.message.getSeqNum()>to){
				return value.message;
			}
		}
		return null;
	}
	
	public void DeleteAllBefore(int seqNum) {
		ArrayList<String> toremove=new ArrayList<String>();
 		for (Entry<String, C_unit> entry : cache.entrySet()) {
			C_unit value=	entry.getValue();
			if (value.message.getSeqNum()<seqNum){
				toremove.add(entry.getKey());
			}
		}
 		for (int i = 0; i < toremove.size(); i++) {
 			cache.remove(toremove.get(i));
		}
		
	}
class C_unit{
	DtsnMessage message;
	int sessionId;
	int SourceId;
	int DestId;
	String appId;
	/**
	 * @param message
	 * @param prob
	 * @param sessionId
	 * @param sourceId
	 * @param destId
	 */
	public C_unit(String _appId,DtsnMessage message, int sessionId, int sourceId, int destId) {
		super();
		this.message = message;
		this.sessionId = sessionId;
		SourceId = sourceId;
		DestId = destId;
		appId=_appId;
	}
	
	
}









}
