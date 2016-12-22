package org.jb.bifconvert;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jb.bifconvert.generated.BifDneLexer;
import org.jb.bifconvert.generated.BifDneParser;

/**
 * Main class. Convert DNE files to BIFXML. 
 * @author Joseph Bondi
 *
 */
public class BifDneConverter {

	public static void main(String[] args) throws ParserConfigurationException, IOException, ParseException {
		OutputStream outStream;
		Options cmdOptions = new Options();		
		Option inFileOpt = Option
				.builder("in")
				.required()
				.hasArg()
				.build();
		Option outFileOpt = Option
				.builder("out")
				.required(false)
				.hasArg()
				.build();
		cmdOptions.addOption(inFileOpt);
		cmdOptions.addOption(outFileOpt);
		CommandLineParser optionsParser = new DefaultParser();
		CommandLine cmdLine = optionsParser.parse(cmdOptions, args);
		
		if(cmdLine.hasOption("out")){
			File outputFile = new File(cmdLine.getOptionValue("out"));
			outStream = new BufferedOutputStream(new FileOutputStream(outputFile));
		}
		else{
			outStream = System.out;
		}
		BifDneListenerImpl listener = new BifDneListenerImpl();		
		ANTLRInputStream in = new ANTLRFileStream(cmdLine.getOptionValue("in"));
		BifDneLexer lexer = new BifDneLexer(in);
		TokenStream tokens = new BufferedTokenStream(lexer);
		BifDneParser parser = new BifDneParser(tokens);
		parser.addParseListener(listener);
		parser.struct();
		
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			//initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(outStream);
			DOMSource source = new DOMSource(listener.getXMLDocument());
			transformer.transform(source, result);
			
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
