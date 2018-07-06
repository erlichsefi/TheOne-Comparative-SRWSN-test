/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import core.*;
import dtsn.DtsnApplication;
import dtsn.Values;
import dtsn.tools.DtsnMessage;
import ui.DTNSimUI;

/**
 * Reporter for the <code>PingApplication</code>. Counts the number of pings
 * and pongs sent and received. Calculates success probabilities.
 *
 * @author teemuk
 */
public class DtsnAppReporter extends Report implements ApplicationListener {
	public static final String NACK_DROP_TO_OLD = "nack drop because it to old";
	public static final String ACK_DROP_TO_OLD = "ack drop because it to old";
	public static final String NACK_DROP_Validion = "nack drop because Validion";
	public static final String ACK_DROP_TO_Validion = "ack drop because Validion";
	public static final String STARTIME = "start";
	public static final String ENDTIME = "end";
	public static final String TO_MUCH_OPEN_SESSION_FAILED = "open session fail";
	public static final String TO_MUCH_RETRANSMITE = "to much retransmition request";
	public static final String ALL_CONF="all packet confiremed";
	public static final  String PASS="pass";
	public static  final String SEND="send";
	public static  final String RECIVE="recive";
	public static final String ERROR = "error ";
	public  boolean finishedOK = false;

	String protocol=null;

	HashMap<Integer,site> sites=new HashMap<Integer,site>();
	ArrayList<String> status=new ArrayList<String>();
	long starttime;
	long endtime;

	public DtsnAppReporter(){

	}


	public void gotEvent(String event, Object params, Application app,
			DTNHost host) {
		// Check that the event is sent by correct application type
		if (!(app instanceof DtsnApplication)) return;



		if (event.equals(STARTIME)){
			starttime=System.currentTimeMillis();
		}
		else if (event.equals(ENDTIME)){
			endtime=System.currentTimeMillis();
			done();
			System.out.println("Sim Ended!");
			//System.exit(-1);
		}
		else if (event.equals(ERROR)){
			endtime=System.currentTimeMillis();
			write("E-R-R-O-R");
			done();
			System.out.println("Sim Crashed! ");

		}

		DtsnMessage m=(DtsnMessage)params;
		if (m!=null){
			if (protocol==null){
				protocol=m.getAppID();
			}
		}
		else{
			if (event==ALL_CONF){
				System.out.println("all acked");
				this.finishedOK=true;
			}
			status.add(event);
		}

	//	System.out.println(SimClock.getTime()+" "+host+" "+event+" [ "+m+" ]");
		if (m!=null){
			int hostid=host.getAddress();
			int numberofbyte=m.getSize();
			String type=(m.IsACK_BIT())? "ack":(m.IsNACK_BIT())? "nack" :(m.IsDATA_BIT())? "data" :(m.IsOPEN_SESSION_BIT())? "open":"others"; 
			if (!sites.containsKey(hostid))
				sites.put(hostid, new site(hostid));

			if  (type!=null){
				if (event.equals(RECIVE)){
					sites.get(hostid).addIn(type, numberofbyte);
				}
				else if (event.equals(SEND)){
					sites.get(hostid).addOut(type, numberofbyte);
				}
				else if (event.equals(PASS)){
					sites.get(hostid).addPass(type, numberofbyte);

				}
			}
		}
		else{
			status.add(event);
		}
	}



	@Override
	public void done() {
		write("run values:");
		write(Values.getString());
		write(" ******************* ");
		write("Protocol: "+protocol);
		write(" ******************* ");
      
		int[][] ans=new int[3][3];
		int[][] count=new int[3][3];
		for (Entry<Integer, site> entry : sites.entrySet())
		{
			site stats=entry.getValue();
			stats.writeStat();
			ans=Sum(stats.sumall(),ans);
			count=Sum(stats.sumallCount(),count);

			write(" ******************* ");
		}
		write("sum");
		write("ack: in,pass,out "+Arrays.toString(ans[0]));
		write("nack: in,pass,out "+Arrays.toString(ans[1]));
		write("data: in,pass,out "+Arrays.toString(ans[2]));
		HashMap<String,String> output=new HashMap<>();
		output.put("ack_size_sum",IntStream.of(ans[0]).sum()+"");
		output.put("nack_size_sum",IntStream.of(ans[1]).sum()+"");
		output.put("data_size_sum",IntStream.of(ans[2]).sum()+"");


		write("count");
		write("ack: in,pass,out "+Arrays.toString(count[0]));
		write("nack: in,pass,out "+Arrays.toString(count[1]));
		write("data: in,pass,out "+Arrays.toString(count[2]));



		output.put("ack_size_count",IntStream.of(count[0]).sum()+"");
		output.put("nack_size_count",IntStream.of(count[1]).sum()+"");
		output.put("data_size_count",IntStream.of(count[2]).sum()+"");

		write("   ");
		write("status: ");

		for (int i = 0; i < status.size(); i++) {
			write(status.get(i)+"  ");
			write("");
		}
		
		

		write("time= "+(this.endtime-this.starttime));
		output.put("time",this.endtime-this.starttime+"");

		super.done();
		if (finishedOK) {
			CsvSim.cuurnet = output;
		}
	}



	private int[][] Sum(int[][] sumall, int[][] ans) {
		int[][] id=new int[3][4];
		for (int i = 0; i < ans.length; i++) {
			for (int j = 0; j < ans.length; j++) {
				id[i][j]=ans[i][j]+sumall[i][j];
			}
		}
		return id;
	}



	class site{
		HashMap<String,packet> stat;
		int myhostID;
		public site(int hostid){
			stat=new HashMap<String,packet> ();
			myhostID=hostid;

		}

		public void addPass(String sign, int numberofbyte) {
			if (stat.containsKey(sign))
				stat.get(sign).countPass(numberofbyte);
			else{
				packet p=new packet(sign);
				p.countIN(numberofbyte);
				stat.put(sign, p);
			}

		}

		public void addIn(String sign,int numberofbyte){
			if (stat.containsKey(sign))
				stat.get(sign).countIN(numberofbyte);
			else{
				packet p=new packet(sign);
				p.countIN(numberofbyte);
				stat.put(sign, p);
			}
		}

		public void addOut(String sign,int numberofbyte ){
			if (stat.containsKey(sign))
				stat.get(sign).countOUT(numberofbyte);
			else{
				packet p=new packet(sign);
				p.countOUT(numberofbyte);
				stat.put(sign, p);
			}
		}

		public int[][] sumall(){
			int[][] ans=new int[3][4];
			ans[0]=stat.get("ack")==null? new int[4] :stat.get("ack").sumall();
			ans[1]=stat.get("nack")==null? new int[4] :stat.get("nack").sumall();
			ans[2]=stat.get("data")==null? new int[4] :stat.get("data").sumall();

			return ans;
		}
		
		public int[][] sumallCount(){
			int[][] ans=new int[3][4];
			ans[0]=stat.get("ack")==null? new int[4] :stat.get("ack").sumallCount();
			ans[1]=stat.get("nack")==null? new int[4] :stat.get("nack").sumallCount();
			ans[2]=stat.get("data")==null? new int[4] :stat.get("data").sumallCount();
			return ans;
		}

		public void writeStat(){
			write("my host id "+this.myhostID);
			for (Entry<String, packet> entry : stat.entrySet()){
				packet stats=entry.getValue();
				stats.writeStat();
			}
		}



		class packet{
			private String type;
			private int countin;
			private int countout;
			private int countpass;


			private int numberofbytepass;
			private int numberofbytein;
			private int numberofbyteout;

			public packet(String _type){
				type=_type;
				countout=0;
				countin=0;
				numberofbytein=0;
				numberofbyteout=0;
			}

			public void writeStat() {
				write("#");
				write("type: "+type);	
				write("in: count= "+countin +" number of bytes= "+numberofbytein);				
				write("out: count="+countout +" number of bytes= "+numberofbyteout);				
				write("pass: count= "+countpass +" number of bytes= "+numberofbytepass);				

			}

			public void countPass(int numberofbyte) {
				countpass++;
				numberofbytepass+=numberofbyte;
			}
			public void countIN(int numberofbyte){
				countin++;
				numberofbytein+=numberofbyte;
			}
			public void countOUT(int numberofbyte){
				countout++;
				numberofbyteout+=numberofbyte;

			}

			public int[] sumall(){
				return new int[]{numberofbytein,numberofbytepass,numberofbyteout,numberofbyteout+numberofbytein};
			}
			public int[] sumallCount(){
				return new int[]{countin,countpass,countout,countout+countin};
			}
		}
	}
}
