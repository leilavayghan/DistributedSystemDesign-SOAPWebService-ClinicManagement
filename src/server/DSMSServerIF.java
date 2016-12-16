package server;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
 
//Service Endpoint Interface
@WebService
@SOAPBinding(style = Style.RPC)
public interface DSMSServerIF {
	
	@WebMethod void createDoctorRecord(String firstName, String lastName, String address, String phone,
			String specialization, String location, String managerID);
	@WebMethod void createNurseRecord(String firstName, String lastName, String designation, String status, String date,
			String location, String managerID);
	@WebMethod void editRecord(String recordID, String fieldName, String fieldValue);
	@WebMethod void transferRecord(String managerID, String recordID, String remoteClinicServerName);
	@WebMethod String getRecord();
	@WebMethod void udprun() throws InterruptedException;
	@WebMethod int getFlag();
	@WebMethod String getMsg();
	@WebMethod void setFlag(int f);

}
