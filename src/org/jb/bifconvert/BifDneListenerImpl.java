package org.jb.bifconvert;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
	public void exitStruct(BifDneParser.StructContext ctx) {
		if(ctx.ID(0).getText().equalsIgnoreCase("bnet")){
			Node netNode  = xmlBifDoc.createElement("NETWORK");
			Node nameNode = xmlBifDoc.createElement("NAME");
			nameNode.setTextContent(ctx.ID(1).getText());
			netNode.appendChild(nameNode);
		}
	}

	@Override
	public void exitFullValue(BifDneParser.FullValueContext ctx){
	}
}
