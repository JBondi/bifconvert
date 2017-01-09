package org.jb.bifconvert;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

import edu.emory.mathcs.backport.java.util.Arrays;
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
		//rootBif.getAttributes().setNamedItemNS(schemaLocationAtt);				
	}

	public Document getXMLDocument(){
		return xmlBifDoc;
	}
	
	@Override
	public void exitStruct(BifDneParser.StructContext ctx) {
		if(ctx.ID(0).getText().equalsIgnoreCase("bnet")){
			Node netNode  = xmlBifDoc.createElement("NETWORK");
			Node nameNode = xmlBifDoc.createElement("NAME");
			nameNode.setTextContent(ctx.ID(1) != null ? ctx.ID(1).getText() : ctx.NUM().getText());
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
					String nodeName = struct.ID(1) != null ? struct.ID(1).getText() : struct.NUM().getText();
					Node varNode  = xmlBifDoc.createElement("VARIABLE");
					Node varName  = xmlBifDoc.createElement("NAME");
					Node textNode = xmlBifDoc.createTextNode(nodeName);
					Node defNode  = xmlBifDoc.createElement("DEFINITION");
					Node defName  = xmlBifDoc.createElement("FOR");
					String kindName = "";
					List<String> states = new ArrayList<String>();
					List<String> functable = new ArrayList<String>();
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
								Node kindNode = xmlBifDoc.createAttribute("TYPE");
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
								states.add(arrayItem.getText());
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
						
						//Build probability table
						else if(asName.equalsIgnoreCase("probs")){
							if(		asCtx.fullValue().array() != null && 
									asCtx.fullValue().array().innerArray() != null){
								List<String> probabilityValues = new ArrayList<String>();
								getArrayNumbers(asCtx.fullValue().array(), probabilityValues);
								String joinedString = probabilityValues.stream()
										.collect(Collectors.joining(" "));
								Node tableText = xmlBifDoc.createTextNode(joinedString);
								Node tableNode = xmlBifDoc.createElement("TABLE");
								tableNode.appendChild(tableText);
								defNode.appendChild(tableNode);
							}
						}//end probability table
						
						else if (asName.equalsIgnoreCase("functable")){
							if(asCtx.fullValue().array().innerArray() != null){
								for(BifDneParser.FullValueContext func : asCtx.fullValue().array().innerArray().fullValue()){
									functable.add(func.getText());
								}
							}
						}//end functable
						
					}//end node values parse
               for(BifDneParser.StructContext stCtx : struct.struct()){
                  String structType = stCtx.ID(0).getText();
                  if(structType.equalsIgnoreCase("visual")){
                    Optional<BifDneParser.AssignmentContext> center = stCtx.assignment().stream()
                        .filter(child -> child.ID().getText().equalsIgnoreCase("center"))
                        .findFirst();
                        
                    if(center.isPresent()){
                       Node propNode = xmlBifDoc.createElement("PROPERTY");
                       String position = "position = " + center.get().fullValue().array().getText();
                       propNode.setTextContent(position);
                       varNode.appendChild(propNode);
                    }
                  }
               }
					
					//Create a probability table out of the function table (if there is one)
					if(functable.size() > 0){
						List<Integer> probs = new ArrayList<Integer>();
						for(String func : functable){
							for(String state : states){
								if(state.equalsIgnoreCase(func))
									probs.add(1);
								else
									probs.add(0);
							}
						}
						String joinedString = probs.stream()
								.map(i -> Integer.toString(i))
								.collect(Collectors.joining(" "));
						Node tableText = xmlBifDoc.createTextNode(joinedString);
						Node tableNode = xmlBifDoc.createElement("TABLE");
						tableNode.appendChild(tableText);
						defNode.appendChild(tableNode);						
					}
					
					if(kindName != ""){
						netNode.appendChild(varNode);
						netNode.appendChild(defNode);
					}
					
				}//end single node parse
			}//end main for-loop
			xmlBifDoc.appendChild(rootBif);
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
