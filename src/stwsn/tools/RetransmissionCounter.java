package stwsn.tools;

import java.util.HashMap;

public class RetransmissionCounter {
	HashMap<Integer,Integer> Table;
	int RetransmissionLimition;
	public RetransmissionCounter(int _RetransmissionLimition){
		Table=new   HashMap<Integer,Integer> (); 
		RetransmissionLimition=_RetransmissionLimition;
	}

	public boolean Totransmite(int seq){
		Integer count=Table.remove(seq);
		if (count!=null){
			if (count<RetransmissionLimition){
				Table.put(seq, ++count);
				return true;
			}
		}
		else{
			Table.put(seq, 1);
			return true;
		}
		return false;
	}
}
