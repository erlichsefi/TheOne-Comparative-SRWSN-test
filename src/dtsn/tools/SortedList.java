package dtsn.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SortedList {
	ArrayList<DtsnMessage> ReceptionBuffer;
	
	public  SortedList(){
		ReceptionBuffer=new ArrayList<DtsnMessage>();
	}
	
	public void add(DtsnMessage m){
		if (!ReceptionBuffer.contains(m)) {
			ReceptionBuffer.add(m);
		}
		Collections.sort(ReceptionBuffer);
	}
	
	public boolean  CheckAllPackets1(int to) {
		String[] a=getMissingPackets(1,to);
		return a.length==0;
	}
	
	

	public String[] getMissingPackets(int last2, int last){
		//copy all packet until the seq number to check
		ArrayList<Integer> LowerSeq=new ArrayList<Integer>();
		for (int i = 0; i < ReceptionBuffer.size(); i++) {
			int seq=ReceptionBuffer.get(i).getSeqNum();
			if (seq>=last2 && seq<last)
				LowerSeq.add(seq);
		}

		//find if there are missing packets
		ArrayList<Integer> seq_missing=new ArrayList<Integer>();
		int min_missing=last;
		for (int j=last2; j< last;j++) {
			if (!LowerSeq.contains(j)){
				min_missing=Math.min(min_missing, j);
				seq_missing.add(j);
			}
		}
		//Creating the missing list
		String[] missing=new String[seq_missing.size()];
		for (int i = 0; i < missing.length; i++) {
			missing[i]=seq_missing.get(i)+"";
		}
		return missing;
	}
	
	
	public boolean IsDuplicate(DtsnMessage m) {
		return ReceptionBuffer.contains(m);
	}

	
	
	
	public boolean IsInSenquence(DtsnMessage m) {
		if (ReceptionBuffer.isEmpty())
			return (getMissingPackets(0,m.getSeqNum()).length==0);
		else {
			return ((ReceptionBuffer.get(ReceptionBuffer.size()-1).getSeqNum()+1)==m.getSeqNum()); 
		}
	}

	public String toString(){
		ReceptionBuffer.sort(new Comparator<DtsnMessage>() {
			@Override
			public int compare(DtsnMessage o1, DtsnMessage o2) {
				return o1.SeqNum-o2.SeqNum;
			}
		});
		String r="";
		for (DtsnMessage m:ReceptionBuffer){
			r=r+","+m.SeqNum;
		}
		return r;


	}
}
