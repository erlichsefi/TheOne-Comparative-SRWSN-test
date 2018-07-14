package dtsn.tools;

import java.util.ArrayList;
import java.util.HashMap;
import dtsn.Values;

public class Buffer{
	private final double alpa=Values.DTSN_SOURCE_ALPA;
	private final double beta=Values.DTSN_SOURCE_BETA;
	private long RTT_AVG=Values.DTSN_SOURCE_INIT_ESTIMITED_RTT_VALUE;
	private long RRT_VARIANCE=Values.DTSN_SOURCE_INIT_ESTIMITED_RRT_VARIANCE;
	private int size;
	private int index;
	private DtsnMessage[] array;
	private  HashMap<Integer, Long> timeCache ;


	public Buffer(int _size){
		this.size=_size;
		array=new DtsnMessage[size];
		timeCache = new HashMap<Integer, Long>();

	}

	public int minimal(){
		return array[0]!=null ? array[0].SeqNum: -1;
	}

	public boolean add(DtsnMessage m){
		if (index<size){
			array[index++]=m;
			timeCache.put(m.getSeqNum(), System.currentTimeMillis());
			return true;
		}
		return false;
	}

	public int size() {
		return index;
	}

	public boolean isEmpty() {
		return index==0;
	}

	public void removeAll() {
		index=0;
		array=new DtsnMessage[size];
	}

	public DtsnMessage get(int i2) {
		if (index>i2)
			return array[i2];
		return null;
	}
	public DtsnMessage getSeqNum(int SeqNum) {
		for (int i = 0; i < index; i++) {
			if (array[i].getSeqNum()==SeqNum){
				return array[i];
			}
		}
		return null;
	}

	public boolean IsFull() {
		return index==size;
	}


	public void removeAllBefore(int seqNum) {
		ArrayList<DtsnMessage> still=new ArrayList<DtsnMessage>();
		DtsnMessage last=null;
		for (int i = 0; i < index; i++) {
			if (array[i].getSeqNum()<seqNum){
				last=array[i];
			}
			else{
				still.add(array[i]);
			}
		}
		array=new DtsnMessage[size];
		for (int j = 0; j < still.size(); j++) {
			array[j]=still.get(j);
		}
		index=still.size();
		UpdateTimeValues(last);
	}

	public int Totalsize() {
		return size;
	}

	private void UpdateTimeValues(DtsnMessage m){
		if (m!=null){
			int seqNum=m.getSeqNum();
			long packetTime=timeCache.get(seqNum);
			long x=System.currentTimeMillis()-packetTime;
			RTT_AVG=(long) ((1-this.alpa)*RTT_AVG+this.alpa*x);
			RRT_VARIANCE=(long) ((1-this.beta)*RRT_VARIANCE+this.beta*Math.abs(x-RTT_AVG));
		}
	}

	public long GetEarTime(){
		return RTT_AVG+4*RRT_VARIANCE;
	}


	public DtsnMessage[] getAllStoredByOrder(String[] seqNumbers) {
		DtsnMessage[] ans=new DtsnMessage[seqNumbers.length];
		for (int i = 0; i < ans.length; i++) {
			ans[i]=getSeqNum(Integer.parseInt(seqNumbers[i]));
		}
		return ans;
	}

	@Override
	public String toString() {
		return "Buffer [IsFull()=" + IsFull() + "]";
	}

	
	

}
