package stwsn.tools;


import dtsn.Values;
import dtsn.tools.DtsnMessage;

public class MessageAggregation {
	long AggregationTime=Values.SRWSN_TIME_TO_AGGREGATE_CPNTROL;
	StwsnMessage ackMessage;
	StwsnMessage nackMessage;
	boolean AggregationSatus=true;


	public MessageAggregation(){
		ackMessage=null;
		nackMessage=null;
	}


	public boolean FitToAggregation(DtsnMessage m){
		return (AggregationSatus && (m.IsACK_BIT() || m.IsNACK_BIT()));
	}

	public void Disable(){
		AggregationSatus=false;
	}

	public void Add(StwsnMessage m){
		if (m.IsACK_BIT()){
			if (ackMessage==null ||m.getConfiramtionSeq()>ackMessage.getConfiramtionSeq()){
				ackMessage=m;
			}
		}else if (m.IsNACK_BIT()){
			if (nackMessage==null){
				nackMessage=m;
			}
			else{
				nackMessage=JoinNack(nackMessage,m);
			}

		}

	}


	private StwsnMessage JoinNack(StwsnMessage m1, StwsnMessage m2) {
		String[] m1Missing=m1.getMissingPackets();
		String[] m2Missing=m2.getMissingPackets();
		if (Integer.parseInt(m1Missing[0])> Integer.parseInt(m2Missing[0])){
			return m1;
		}
		return m2;
	}


	public long getAggregationTime() {
		return AggregationTime;
	}


	public void init(){
		ackMessage=null;
		nackMessage=null;
	}



	public DtsnMessage Collect(){
		if (ackMessage==null && nackMessage==null){
			return null;
		}
		else if (ackMessage==null ){
			nackMessage.setRTX_BIT();
			return   nackMessage;
		}
		else if (nackMessage==null ){
			ackMessage.setRTX_BIT();
			return   ackMessage;
		}

		else{
			String[] mis=nackMessage.getMissingPackets();
			int max=Integer.parseInt(mis[mis.length-1]);
			if (ackMessage.getConfiramtionSeq()>max){
				return ackMessage;
			}
			else{
				DtsnMessage m= nackMessage.removeElementLowerEquleThen(ackMessage.getConfiramtionSeq());
				m.setRTX_BIT();
				return m;
			}
		}
	}




	}
