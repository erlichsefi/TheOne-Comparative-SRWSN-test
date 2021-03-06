package dtsn;


public class Values {
	public enum attackerType{
		on,off,both,none
	}
	public static  attackerType ATTACKER_TYPE = attackerType.on;

	public static  int STWSN_NUM_OF_MERKLE_TREE = 1;
	public static  double LOST_EVERY_N_PACKET = 1-0.01;
	public static double ATTACK_PROB=1-0.1;
	public static  int NUMBER_OF_PACKETS_TO_SEND = 100;
	public static int Source_BUFFER_SIZE = 30;
	public static  int MAX_NUM_OF_EAR = 15;

	
	public static  long DTSN_SOURCE_INIT_ESTIMITED_RRT_VARIANCE = 500;
	public static  long DTSN_SOURCE_INIT_ESTIMITED_RTT_VALUE = 500;
	public static  double DTSN_SOURCE_ALPA = 0.9;
	public static  double DTSN_SOURCE_BETA = 0.8;
	
	public static  byte[] SDTP_ACK_MASTER_KEY=new byte[128];
	public static  byte[] SDTP_NACK_MASTER_KEY=new byte[128];
	
	
	public static  long DTSN_LENGTH_OG_ACTIVITY_TIMRER = 1000L*50L;
	
	public static  int SRWN_EAR_TO_HANDLE = 10;
	public static  long SRWN_EAR_STATUS_TIMER = 500;
	public static  int SRWSN_RTX_LIMITION_PER_PACKET = 3;
	public static  int SRWSN_MAX_SN_RETURN = 5;
	public static  long SRWSN_TIME_TO_WAIT_TO_OPEN_SESSION = 500;
	public static  long SRWN_EAR_TIMER_LENGTH = 2000;
	public static  int SRWSN_MAX_OPEN_SESSION_TO_SEND = 3;

	public static  long SRWSN_TIME_TO_AGGREGATE_CPNTROL = 200;
	
	
	public static String getString() {
		return "Values [NUMBER_OF_PACKETS_TO_SEND=" + NUMBER_OF_PACKETS_TO_SEND + ", Source_BUFFER_SIZE="
				+ Source_BUFFER_SIZE + ", MAX_NUM_OF_EAR=" + MAX_NUM_OF_EAR
				+ ", DTSN_SOURCE_INIT_ESTIMITED_RRT_VARIANCE=" + DTSN_SOURCE_INIT_ESTIMITED_RRT_VARIANCE
				+ ", DTSN_SOURCE_INIT_ESTIMITED_RTT_VALUE=" + DTSN_SOURCE_INIT_ESTIMITED_RTT_VALUE
				+ ", DTSN_SOURCE_ALPA=" + DTSN_SOURCE_ALPA + ", DTSN_SOURCE_BETA=" + DTSN_SOURCE_BETA
				+ ", SDTP_ACK_MASTER_KEY=" + SDTP_ACK_MASTER_KEY.length + ", SDTP_NACK_MASTER_KEY="
				+ SDTP_NACK_MASTER_KEY.length + ", DTSN_LENGTH_OG_ACTIVITY_TIMRER="
				+ DTSN_LENGTH_OG_ACTIVITY_TIMRER + ", SRWN_EAR_TO_HANDLE=" + SRWN_EAR_TO_HANDLE
				+ ", SRWN_EAR_STATUS_TIMER=" + SRWN_EAR_STATUS_TIMER + ", SRWSN_RTX_LIMITION_PER_PACKET="
				+ SRWSN_RTX_LIMITION_PER_PACKET + ", SRWSN_MAX_SN_RETURN=" + SRWSN_MAX_SN_RETURN
				+ ", SRWSN_TIME_TO_WAIT_TO_OPEN_SESSION=" + SRWSN_TIME_TO_WAIT_TO_OPEN_SESSION
				+ ", SRWN_EAR_TIMER_LENGTH=" + SRWN_EAR_TIMER_LENGTH + ", SRWSN_MAX_OPEN_SESSION_TO_SEND="
				+ SRWSN_MAX_OPEN_SESSION_TO_SEND + ", SRWSN_TIME_TO_AGGREGATE_CPNTROL="
				+ SRWSN_TIME_TO_AGGREGATE_CPNTROL + "]";
	}


}
