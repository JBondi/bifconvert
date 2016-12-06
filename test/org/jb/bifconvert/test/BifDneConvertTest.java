package org.jb.bifconvert.test;

import static org.junit.Assert.*;

import java.io.FileInputStream;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.jb.bifconvert.BifDneListenerImpl;
import org.jb.bifconvert.generated.BifDneLexer;
import org.jb.bifconvert.generated.BifDneListener;
import org.jb.bifconvert.generated.BifDneParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BifDneConvertTest {

	BifDneListener listener;
	BifDneParser parser;
	
	@Before
	public void setUp() throws Exception {
		listener = new BifDneListenerImpl();
		ANTLRInputStream in = new ANTLRFileStream("samples/Animals.dne");
		BifDneLexer lexer = new BifDneLexer(in);
		TokenStream tokens = new BufferedTokenStream(lexer);
		parser = new BifDneParser(tokens);
		parser.addParseListener(listener);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		parser.struct();
	}

}
