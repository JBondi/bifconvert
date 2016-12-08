package org.jb.bifconvert;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jb.bifconvert.generated.BifDneBaseListener;
import org.jb.bifconvert.generated.BifDneParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class BifDneListenerImpl extends BifDneBaseListener {
	
	private Node rootBif;
	private String context;
	Document xmlBifDoc;
	
	public BifDneListenerImpl() throws ParserConfigurationException {
		final String NAMESPACE = "http://www.cs.ubc.ca/labs/lci/fopi/ve/XMLBIFv0_3";
		
		xmlBifDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		rootBif	  = xmlBifDoc.createElement("BIF");
		Node namespaceAtt		= xmlBifDoc.createAttribute( "xmlns");
		Node versionAtt			= xmlBifDoc.createAttribute( "VERSION");
		Node xsiAtt				= xmlBifDoc.createAttribute( "xmlns:xsi");
		Node schemaLocationAtt 	= xmlBifDoc.createAttribute("xsi:schemaLocation");
		
		versionAtt.setNodeValue("0.3");
		schemaLocationAtt.setNodeValue("http://www.cs.ubc.ca/labs/lci/fopi/ve/XMLBIFv0_3 http://www.cs.ubc.ca/labs/lci/fopi/ve/XMLBIFv0_3/XMLBIFv0_3.xsd");
		xsiAtt.setNodeValue("http://www.w3.org/2001/XMLSchema-instance");
		namespaceAtt.setNodeValue(NAMESPACE);
		rootBif.getAttributes().setNamedItem(versionAtt);
		rootBif.getAttributes().setNamedItem(namespaceAtt);
		rootBif.getAttributes().setNamedItemNS(xsiAtt);
		rootBif.getAttributes().setNamedItemNS(schemaLocationAtt);				
	}

	@Override
	public void exitAssignment(BifDneParser.AssignmentContext ctx){
		if(ctx.ID().getText().equals("functable")){
			System.out.println(ctx.fullValue().getText());
		}
	}
	
	@Override
	public void exitStruct(BifDneParser.StructContext ctx) {
		if(ctx.ID(0).getText().equalsIgnoreCase("bnet")){
			Node netNode  = xmlBifDoc.createElement("NETWORK");
			Node nameNode = xmlBifDoc.createElement("NAME");
			nameNode.setTextContent(ctx.ID(1).getText());
			netNode.appendChild(nameNode);
			rootBif.appendChild(netNode);
			
			for(BifDneParser.AssignmentContext asCtx : ctx.assignment()){
				Node propNode = xmlBifDoc.createElement("PROPERTY");
				propNode.setTextContent(asCtx.getText());
				netNode.appendChild(propNode);
			}
			xmlBifDoc.appendChild(rootBif);
			Transformer transformer;
			try {
				transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				//initialize StreamResult with File object to save to file
				StreamResult result = new StreamResult(new StringWriter());
				DOMSource source = new DOMSource(xmlBifDoc);
				transformer.transform(source, result);
				String xmlString = result.getWriter().toString();
				System.out.println(xmlString);	
			} catch (TransformerFactoryConfigurationError | TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void exitFullValue(BifDneParser.FullValueContext ctx){
	}
}
