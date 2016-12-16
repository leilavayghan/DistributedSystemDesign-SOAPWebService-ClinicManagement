package server;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import Manager.ManagerSession;
import data.StaffRecordRepository;
import models.DoctorRecord;
import models.NurseRecord;
import shared.Config;
import view.MainWindow;

import javax.jws.WebService;

//Service Implementation
@WebService(endpointInterface = "server.DSMSServerIF")

public class DSMSServer implements Runnable, DSMSServerIF{

	public  StaffRecordRepository StaffRecords = new StaffRecordRepository();
	public static int MTLcount = 0;
	public static int DDOcount = 0;
	public static int LVLcount = 0;
	public static int udpserverport = Config.PORT_NUMBER_MTL - 100;
	public static String receivedmsg,sendmsg,mymsg;
	public static boolean isDone=false;
	public URL url;
	public DSMSServerIF myRef;
	public static int flag = 0;
	
	@Override
	public int getFlag(){
		return flag;
	}
	
	@Override
	public void setFlag(int f){
		flag=f;
	}
	
	@Override
	public String getMsg(){
		return sendmsg;
	}
	
	@Override
	public void createDoctorRecord(String firstName, String lastName, String address, String phone,
			String specialization, String location, String managerID) {
		
		DoctorRecord record = new DoctorRecord(location);
		record.FirstName = firstName;	
		record.LastName = lastName;
		record.Address = address;
		record.PhoneNumber = phone;
		record.Specialization = specialization;
		record.getRecordID();
		record.Location = location;
		System.out.println("Doctor with id of " +record.getRecordID()+" added in Location " + location);
		StaffRecords.Add(record);
		if(location.equals("MTL")){
			MTLcount++;
		}
		if(location.equals("DDO")){
			DDOcount++;
		}
		if(location.equals("LVL")){
			LVLcount++;
		}
	}

	@Override
	public void createNurseRecord(String firstName, String lastName, String designation, String status, String date,
			String location, String managerID) 
	{
		NurseRecord record = new NurseRecord(location);
		record.FirstName = firstName;
		record.LastName = lastName;
		record.designation = designation;
		record.status = status;
		record.statusDate =  date;
		record.getRecordID();
		record.cliniclocation = location;
		System.out.println("Location " + location);
		System.out.println("Nurse with id of " +record.getRecordID()+" added in Location " + location);
		StaffRecords.Add(record);
		
		if(location.equals("MTL")){
			MTLcount++;
		}
		if(location.equals("DDO")){
			DDOcount++;
		}
		if(location.equals("LVL")){
			LVLcount++;
		}
		
	}

	@Override
	public void editRecord(String recordID, String fieldName, String fieldValue) 
	{
		try
		{
			StaffRecords.Edit(recordID, fieldName, fieldValue);
		}
		catch(Exception ex)
		{
			
		}
		
	}

	@Override
	public void transferRecord(String managerID, String recordID, String remoteClinicServerName) 
	{
		int remoteport;
		if(remoteClinicServerName.equals("MTL")){
			remoteport = Config.PORT_NUMBER_MTL;
		}
		else if(remoteClinicServerName.equals("DDO")){
			remoteport = Config.PORT_NUMBER_DDO;
		}
		else{
			remoteport = Config.PORT_NUMBER_LVL;
		}
		if(StaffRecords.findRecord(recordID)){
			String mymsg = null;
			DoctorRecord doctor;
			NurseRecord nurse;
			
			if (recordID.substring(0, 1).equals("D")){
				doctor = (DoctorRecord) StaffRecords.GetRecord(recordID);
				mymsg = "D" + ",," + doctor.FirstName + ",," + doctor.LastName + ",," + doctor.Address + ",," + 
						doctor.PhoneNumber + ",," + doctor.Specialization + ",," + remoteClinicServerName + 
							",," + managerID;
			}
			
			else if (recordID.substring(0, 1).equals("N")){
				nurse = (NurseRecord) StaffRecords.GetRecord(recordID);
				mymsg = "N" + ",," + nurse.FirstName + ",," + nurse.LastName + ",," + nurse.designation + ",," + 
						nurse.status + ",," + nurse.statusDate + ",," + remoteClinicServerName + 
							",," + managerID;
			}
			
			DatagramSocket clientSocket = null;
			try
			{
				
				clientSocket = new DatagramSocket();
				
				byte[] udpCount = mymsg.getBytes();
				InetAddress address = InetAddress.getByName("localhost");
				
				DatagramPacket request = new DatagramPacket(udpCount, mymsg.length(),address,remoteport);
				
				clientSocket.send(request);
				
				byte[] buffer = new byte[1000];
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				clientSocket.receive(reply);
				if(new String(reply.getData()).substring(0, 4).equals("done")){
					isDone=true;
				}
				
			}
			catch (SocketException e){System.out.println("Socket: " + e.getMessage());
			} catch (IOException e) {System.out.println("IO: " + e.getMessage());}
			finally{
				if(clientSocket!=null)
				clientSocket.close();
			}
			
			if (isDone){
				StaffRecords.deleteRecord(recordID);
				if(managerID.substring(0,3).equals("MTL"))
					MTLcount--;
				else if(managerID.substring(0,3).equals("DDO"))
					DDOcount--;
				else if(managerID.substring(0,3).equals("LVL"))
					LVLcount--;
				isDone = false;
			}
		}
		else{
			JOptionPane.showMessageDialog(null, "Invalid Record ID for transfer");
		}
	} 
	
	@Override
	public String getRecord() {
		return StaffRecords.getRecord();
	}

	public static String interpret(String received){
		
		// if received.substring(0, 1) : it means we need to send the count
		// if received.substring(0, 1) is something else, we need to transfer
		
		if (received.substring(0, 1).equals("1")){
			
			if (received.substring(1, 4).equals("MTL")){
				return Integer.toString(MTLcount);
			}
			else if (received.substring(1, 4).equals("DDO")){
				return Integer.toString(DDOcount);
			}
			else {
				return Integer.toString(LVLcount);
			}
		}
		
		else{
			
			return received;
		}
	}
	
	
	@Override 
	public void udprun() throws InterruptedException{
		
				//MTL
				DSMSServer dsmsServantMTL = new DSMSServer();
				 
				Thread mtl = new Thread(dsmsServantMTL);
				mtl.start();
				Thread.sleep(10);
				System.out.println("Montreal Server Running");
				
				//DDO
				DSMSServer dsmsServantDDO = new DSMSServer();
				 
				
				Thread ddo = new Thread(dsmsServantDDO);
				ddo.start();
				Thread.sleep(10);
				System.out.println("DDO Server Running");
				
				//LVL
				DSMSServer dsmsServantLVL = new DSMSServer();
				 
				Thread lvl = new Thread(dsmsServantLVL);
				lvl.start();
				Thread.sleep(10);
				System.out.println("LVL Server Running");
		
	}
	
	@Override
	public void run() {
		DatagramSocket mySocket = null;
		
		try
		{
			udpserverport = udpserverport + 100;
			mySocket = new DatagramSocket(udpserverport);
			
			byte[] mybuffer = new byte[1000];
			
			while(true)
			{
				
				DatagramPacket myrequest = new DatagramPacket(mybuffer, mybuffer.length);
				
				mySocket.receive(myrequest);
				receivedmsg=(new String(myrequest.getData()));
				sendmsg=interpret(receivedmsg);
				String[] array = sendmsg.split(",,");
				if (array[0].equals("D")){
					
					flag = 1;
					sendmsg="done";
					}
				if (array[0].equals("N")){
					
					flag = 2;
					sendmsg="done";
					}
				DatagramPacket myreply = new DatagramPacket(sendmsg.getBytes(),
						sendmsg.length(),myrequest.getAddress(),myrequest.getPort());
				mySocket.send(myreply);
				
			}
		}
		catch (SocketException e){System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {System.out.println("IO: " + e.getMessage());}
		finally
		{
			if(mySocket!=null){
				mySocket.close();
			}
		}
		
	}
	
}
