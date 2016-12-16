package server;

import javax.xml.ws.Endpoint;

import org.apache.log4j.Logger;

import server.DSMSServer;
import view.Login;

//Endpoint publisher
public class Publisher{
	static Logger loggerServer = Logger.getLogger("server");
	public static void main(String[] args) {
	   Endpoint.publish("http://localhost:8070/ws/DSMS", new DSMSServer());
	   loggerServer.info("WSDL Created for Montreal at port 8070");
	   Endpoint.publish("http://localhost:8080/ws/DSMS", new DSMSServer());
	   loggerServer.info("WSDL Created for Dollard-Des-Ormeaux at port 8080");
	   Endpoint.publish("http://localhost:8090/ws/DSMS", new DSMSServer());
	   loggerServer.info("WSDL Created for Laval at port 8090");
    }

}