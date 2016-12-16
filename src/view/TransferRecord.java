package view;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;

import Manager.ManagerSession;
import models.NurseRecord;
import models.StaffRecord;
import server.DSMSServerIF;
import shared.enumerations.Locations;
import shared.enumerations.NurseDesignation;
import shared.enumerations.NurseStatus;


public class TransferRecord extends JPanel{
	
	private JPanel jpanel;
	private static JTextField txtRecordID,txtDestination;
	private static String strRecordID,strDestination;
	private static JComboBox cmbDestination;
	static Logger loggerSystem = Logger.getLogger("dsmssystem");
	static Logger loggerServer = Logger.getLogger("server");
	public ManagerSession remoteSession;
	public URL url;
	
	
	
	public JPanel panel(boolean restriction){			// false for creating , true for editing

	JPanel jpanel = new JPanel();
	
	JLabel lblRecordID = new JLabel("Record ID: ");
	txtRecordID = new JTextField();
		
	JLabel lblDesignation = new JLabel("Destination Server: ");
	
	if(Login.managerCode.equals("MTL")){
		String[] myStrings = { "DDO", "LVL"};
		cmbDestination = new JComboBox(myStrings);
	}
	else if(Login.managerCode.equals("DDO")){
		String[] myStrings = { "MTL", "LVL"};
		cmbDestination = new JComboBox(myStrings);
	}
	else if(Login.managerCode.equals("LVL")){
		String[] myStrings = { "MTL", "DDO"};
		cmbDestination = new JComboBox(myStrings);
	}
	
	
	jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.Y_AXIS));
	jpanel.add(lblRecordID);
	jpanel.add(txtRecordID);
	
	jpanel.add(lblDesignation);
	jpanel.add(cmbDestination);
	
	if(restriction)
	{
		txtRecordID.setEditable(false);
	}
	return jpanel;
	
	}
	
	public void TransferValidCreate(ManagerSession session) throws Exception
	{
		
		strRecordID = txtRecordID.getText().toString();
		strDestination = cmbDestination.getSelectedItem().toString();
		if(strRecordID.equals(""))
			JOptionPane.showMessageDialog(jpanel,"Enter Record ID");	
			else if(strDestination.equals(""))
			JOptionPane.showMessageDialog(jpanel,"Choose Destination Server");
			else
			{
				session.getService().transferRecord(Login.managerID, strRecordID, strDestination);
				
				if(strDestination.equals("MTL"))
					url = new URL("http://localhost:8070/ws/DSMS?wsdl");
				else if(strDestination.equals("DDO"))
					url = new URL("http://localhost:8080/ws/DSMS?wsdl");
				else
					url = new URL("http://localhost:8090/ws/DSMS?wsdl");
				
				
		        QName qname = new QName("http://server/", "DSMSServerService");

		        Service service = Service.create(url, qname);

		        DSMSServerIF obj = service.getPort(DSMSServerIF.class);
		        
				remoteSession = new ManagerSession(Login.managerID,obj);
				
				String RecordInfo = session.getService().getMsg();
				String[] array = RecordInfo.split(",,");
				
				if(remoteSession.getService().getFlag()==1){
					
					remoteSession.getService().createDoctorRecord(array[0], array[0],array[0],array[0],
							array[0],strDestination,Login.managerID);
					remoteSession.getService().setFlag(0);
				}
				else if(remoteSession.getService().getFlag()==2){
					remoteSession.getService().createNurseRecord(array[0], array[0],array[0],array[0],
							array[0],strDestination,Login.managerID);
					remoteSession.getService().setFlag(0);
				}
				loggerSystem.info("The manager with managerID of "
						 + Login.managerID + " in clinic location " + Login.managerCode + " transfered the record with the ID of " + strRecordID + " to the server in " + strDestination);
				loggerServer.info("Transfer Record Operation is Invoked at server of " + Login.managerCode + " by manager with manager-id " + Login.managerID);
				loggerSystem.info("New Record is created by manager with managerID "
					 + Login.managerID + " in clinic location " + strDestination);
			}
	}
}
