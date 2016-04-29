package com.mastek.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mastek.util.PostgresImpl;

import net.sf.json.JSONObject;

@ServerEndpoint("/websocket/fitnesscalc")
@Path("/apis")
public class IOTEventController {

	@Context
	private HttpServletRequest request;


	@POST
	@Path("/heartbeat")
	//public void HeartBeatPost(Object eData,
	public void HeartBeatPost(StreamSource eData,
			@Context HttpServletResponse servletResponse) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(eData.getInputStream()));
		StringBuilder out = new StringBuilder();
        String line;
        try {
			while ((line = reader.readLine()) != null) {
			    out.append(line);
			} 
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        String body = out.toString();
        System.out.println(body);
        if (body.contains("om2m")){
        	System.out.println("****************om2m********************");
			    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			    try {
			        DocumentBuilder dBuilder = dbf.newDocumentBuilder();
			
			        Document notifyDoc = dBuilder.parse(new InputSource(new ByteArrayInputStream(body.getBytes("utf-8"))));
			
			        String contentInstance64 = notifyDoc.getElementsByTagName("om2m:representation").item(0).getTextContent();
			        System.out.println("ContentInstance (Base64-encoded):\n"+contentInstance64+"\n");
			
			        String contentInstance = new String(DatatypeConverter.parseBase64Binary(contentInstance64));
			        System.out.println("ContentInstance:\n"+contentInstance+"\n");
			
			        Document instanceDoc = dBuilder.parse(new InputSource(new ByteArrayInputStream(contentInstance.getBytes("utf-8"))));
			        String content64 = instanceDoc.getElementsByTagName("om2m:content").item(0).getTextContent();;
			        System.out.println("Content (Base64-encoded):\n"+content64+"\n");
			
			        final String content = new String(DatatypeConverter.parseBase64Binary(content64));
			        System.out.println("Content:\n"+content+"\n");
				    JSONObject eventData = JSONObject.fromObject(content);
					addEventData(eventData);
			
			    } catch (ParserConfigurationException e) {
				        e.printStackTrace();
				    } catch (SAXException e) {
				        e.printStackTrace();
				    } catch (IOException e) {
				        e.printStackTrace();
				    }
        	}
        else{
        	JSONObject eventData = JSONObject.fromObject(body);
			addEventData(eventData);
        }
	}

	//@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	
	
	private void addEventData(JSONObject eventdata) {	
		PostgresImpl pg = new PostgresImpl();
		pg.connect();
		pg.insertEvents(eventdata);
		pg.close();
	}

	@POST
	@Path("/netcalorie")
	@Consumes(MediaType.APPLICATION_JSON)
	public void NetCaloriePost(Object eData,
			@Context HttpServletResponse servletResponse) {
		JSONObject eventData = JSONObject.fromObject(eData);
		addEventData(eventData);
	}

	@POST
	@Path("/temperature")
	@Consumes(MediaType.APPLICATION_JSON)
	public void TemperaturePost(Object eData,
			@Context HttpServletResponse servletResponse) {
		JSONObject eventData = JSONObject.fromObject(eData);
		addEventData(eventData);
	}

	@OnMessage
	public void echoTextMessage(Session session, String msg, boolean last) {
		try {
			synchronized (session) {
				if (session.isOpen()) {			
					PostgresImpl pg = new PostgresImpl();
					pg.connect();
					while (true) {
						msg= pg.fetchDetails();
						System.out.println(msg);
						session.getBasicRemote().sendText(msg ,
								last);
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (IOException e) {
			try {
				session.close();
			} catch (IOException e1) {
				// Ignore
			}
		}
	}

	@OnMessage
	public void echoBinaryMessage(Session session, ByteBuffer bb, boolean last) {
		try {
			if (session.isOpen()) {
					session.getBasicRemote().sendBinary(bb, last);
			}
		} catch (IOException e) {
			try {
				session.close();
			} catch (IOException e1) {
				// Ignore
			}
		}
	}

	@OnOpen
	public void onOpen(Session session) {
		try {
			session.getBasicRemote().sendText("Connection Established");
			echoTextMessage(session, "getData", true);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	
	
	
}

/*
 ws://192.168.59.3/IOTWebClient/websocket/fitnesscalc
 */


