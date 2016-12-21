package org.jb.bifconvert;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

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
import org.jb.bifconvert.generated.BifDneLexer;
import org.jb.bifconvert.generated.BifDneParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.emory.mathcs.backport.java.util.Collections;

public class BifDneListenerImpl extends BifDneBaseListener {
	
	private Node rootBif;
	private String context;
	private Document xmlBifDoc;
	
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
			rootBif.appendChild(netNode);
			
			for(BifDneParser.AssignmentContext asCtx : ctx.assignment()){
				Node propNode = xmlBifDoc.createElement("PROPERTY");
				propNode.setTextContent(asCtx.getText());
				netNode.appendChild(propNode);
			}
			
			//Parsing each node
			for(BifDneParser.StructContext struct : ctx.struct()){
				if((struct.ID().size() > 1) && (struct.ID(0).getText().equalsIgnoreCase("node"))){
					String nodeName = struct.ID(1).getText();
					Node varNode  = xmlBifDoc.createElement("VARIABLE");
					Node varName  = xmlBifDoc.createElement("NAME");
					Node textNode = xmlBifDoc.createTextNode(nodeName);
					Node defNode  = xmlBifDoc.createElement("DEFINITION");
					Node defName  = xmlBifDoc.createElement("FOR");
					String kindName = null;
					defName.appendChild(xmlBifDoc.createTextNode(nodeName));
					defNode.appendChild(defName);
					varName.appendChild(textNode);
					varNode.appendChild(varName);
					
					//Each item in the node
					for(BifDneParser.AssignmentContext asCtx : struct.assignment()){
						String asName = asCtx.ID().getText();
						
						//Type of node
						if(asName.equalsIgnoreCase("kind")){
							kindName = asCtx.fullValue().ID().getText();
							if(		kindName.equalsIgnoreCase("nature") ||
									kindName.equalsIgnoreCase("utility") ||
									kindName.equalsIgnoreCase("decision")){
								Node kindNode = xmlBifDoc.createAttribute("type");
								kindNode.setNodeValue(kindName.toLowerCase());
								varNode.getAttributes().setNamedItem(kindNode);
							}
							else{
								kindName = null;
							}
							
						}
						
						//State names
						else if(asName.equalsIgnoreCase("states")){
							for(BifDneParser.FullValueContext arrayItem :
								asCtx.fullValue().array().innerArray().fullValue()){
								Node outcomeNode = xmlBifDoc.createElement("OUTCOME");
								Node outcomeText = xmlBifDoc.createTextNode(arrayItem.getText());
								outcomeNode.appendChild(outcomeText);
								varNode.appendChild(outcomeNode);
							}
						}
						
						//Set up the Parents in the DEFINITION node
						else if(asName.equalsIgnoreCase("parents")){
							if(asCtx.fullValue().array().innerArray() != null){
								for(BifDneParser.FullValueContext value : asCtx.fullValue().array().innerArray().fullValue()){
									if(value.ID() != null){
										Node valueText = xmlBifDoc.createTextNode(value.ID().getText());
										Node valueNode = xmlBifDoc.createElement("GIVEN");
										valueNode.appendChild(valueText);
										defNode.appendChild(valueNode);
									}
								}
							}
						}//end parents
						
						else if(asName.equalsIgnoreCase("probs")){
							if(		asCtx.fullValue().array() != null && 
									asCtx.fullValue().array().innerArray() != null){
								List<String> probabilityValues = new ArrayList<String>();
								getArrayNumbers(asCtx.fullValue().array(), probabilityValues);
								String joinedString = probabilityValues.stream()
										.collect(Collectors.joining(","));
								Node tableText = xmlBifDoc.createTextNode(joinedString);
								Node tableNode = xmlBifDoc.createElement("TABLE");
								tableNode.appendChild(tableText);
								defNode.appendChild(tableNode);
							}
						}
					}
					if(kindName != null){
						netNode.appendChild(varNode);
						netNode.appendChild(defNode);
					}
					
				}
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

	private void getArrayNumbers(BifDneParser.ArrayContext array, List<String> currentNumbers){
		for(BifDneParser.FullValueContext fvctx : array.innerArray().fullValue()){
			if(fvctx.array() != null){
				getArrayNumbers(fvctx.array(), currentNumbers);
				
			}
			else{
				currentNumbers.add(fvctx.NUM().getText());					
			}
		}
	}
}
