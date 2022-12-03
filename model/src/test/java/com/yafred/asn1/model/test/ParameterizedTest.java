/*******************************************************************************
 * Copyright (C) 2021 Fred D7e (https://github.com/yafred)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
// https://github.com/junit-team/junit4/wiki/parameterized-tests

// Reflections: needs reflection and guava (see pom.xml)

package com.yafred.asn1.model.test;


import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Set;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import com.yafred.asn1.grammar.ASNLexer;
import com.yafred.asn1.grammar.ASNParser;
import com.yafred.asn1.grammar.ASNExtLexer;
import com.yafred.asn1.grammar.ASNExtParser;
import com.yafred.asn1.model.Specification;
import com.yafred.asn1.parser.Asn1TypeLabeller;
import com.yafred.asn1.parser.Asn1ModelValidator;
import com.yafred.asn1.parser.Asn1SpecificationWriter;
import com.yafred.asn1.parser.SpecificationAntlrVisitor;

@RunWith(Parameterized.class)
public class ParameterizedTest {
	@Parameters(name = "{0}")
	public static Iterable<? extends Object> data() {

		Reflections reflections = new Reflections("com.yafred.asn1.test", new ResourcesScanner());
		Set<String> properties = reflections.getResources(Pattern.compile(".*\\.asn"));
		return properties;
	}

	String resourceName;
	
	public ParameterizedTest(String resourceName) {
		this.resourceName = resourceName;
	}
	
	@Test
    public void buildModel() throws Exception {
    	// load test data
        InputStream inStream = getClass().getClassLoader().getResourceAsStream(resourceName);

        System.out.println(resourceName);
        
        if (inStream == null) {
            throw new RuntimeException("Resource not found: " + resourceName);
        }

        // create a CharStream that reads from standard input
        CharStream input = CharStreams.fromStream(inStream);
        
        // create a lexer that feeds off of input CharStream
        ASNLexer lexer = new ASNLexer(input);
        // create a buffer of tokens pulled from the lexer
        TokenStream tokens = new CommonTokenStream(lexer);
        // create a parser that feeds off the tokens buffer
        ASNParser parser = new ASNParser(tokens);
        ParseTree tree = parser.specification(); // begin parsing at specification rule
        
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        
        SpecificationAntlrVisitor visitor = new SpecificationAntlrVisitor();
        Specification specification = visitor.visit(tree);
        
        inStream.close();
        inStream = getClass().getClassLoader().getResourceAsStream(resourceName);  // reload

        System.out.println("-----------------  TEST DATA  ---------------------------------------------------------------");
        
        System.out.println(convertStreamToString(inStream));
        
        System.out.println("-----------------  DUMP MODEL ---------------------------------------------------------------");
          
        new Asn1SpecificationWriter(System.out).visit(specification);

        System.out.println("-----------------  VALIDATE MODEL -----------------------------------------------------------");
        
        Asn1ModelValidator asn1ModelValidator = new Asn1ModelValidator();
       	asn1ModelValidator.visit(specification);
        boolean hasErrors = false;
        for(String error : asn1ModelValidator.getWarningList()) {
        	System.out.println(error);
        }
        for(String error : asn1ModelValidator.getErrorList()) {
        	hasErrors = true;
        	System.err.println(error);
        }
        
        System.out.println("-----------------  VALIDATED MODEL INFO -----------------------------------------------------");
        asn1ModelValidator.dump();

        System.out.println("-----------------  DUMP MODEL AGAIN ---------------------------------------------------------");
        
        new Asn1SpecificationWriter(System.out).visit(specification);

        if(resourceName.matches((".*(/should_fail/).*"))) {
        	System.out.println("SHOULD FAIL\n\n\n");
        	assert(hasErrors);
        }
        else {
           	System.out.println("SHOULD PASS\n\n\n");
           	assert(!hasErrors);
        }

        System.out.println("-----------------  ATTACH LABELS TO TYPES ---------------------------------------------------");
        new Asn1TypeLabeller(true).visit(specification);

    }

	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is);
	    s.useDelimiter("\\A");
	    String ret = s.hasNext() ? s.next() : "";
	    s.close();
	    return ret;
	}







}
