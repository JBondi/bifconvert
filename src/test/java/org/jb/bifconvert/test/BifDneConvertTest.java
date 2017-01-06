package org.jb.bifconvert.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.jb.bifconvert.BifDneConverter;
import org.jb.bifconvert.BifDneListenerImpl;
import org.jb.bifconvert.generated.BifDneBaseListener;
import org.jb.bifconvert.generated.BifDneLexer;
import org.jb.bifconvert.generated.BifDneListener;
import org.jb.bifconvert.generated.BifDneParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class BifDneConvertTest {

	private BifDneListenerImpl listener;
	private BifDneParser parser;
	
	@Before
	public void setUp() throws Exception {
		String resourceFile = URLDecoder.decode(getClass().getResource("/Animals.dne").getPath(), "UTF-8");
		listener = new BifDneListenerImpl();
		ANTLRInputStream in = new ANTLRFileStream(resourceFile);
		BifDneLexer lexer = new BifDneLexer(in);
		TokenStream tokens = new BufferedTokenStream(lexer);
		parser = new BifDneParser(tokens);
		parser.addParseListener(listener);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParse() {
		parser.dne();
		Document doc = listener.getXMLDocument();
	}

	@Test
	public void testSave() throws Exception{
	}
	
	@Test
	public void testMultiParse(){
	}
	private void parseFile(String fileName){
	   System.out.println("Parsing " + fileName);
      try {
         parser.removeParseListeners();
         BifDneListener baseListener = new BifDneBaseListener();
         parser.addParseListener(baseListener);
         ANTLRInputStream in1;
         in1 = new ANTLRFileStream(fileName);
         BifDneLexer lexer = new BifDneLexer(in1);
         TokenStream tokens = new BufferedTokenStream(lexer);
         parser = new BifDneParser(tokens);
         parser.struct();
      }
      catch (IOException e) {
         e.printStackTrace();
         fail(e.getMessage());
      }
	   
	}
}
