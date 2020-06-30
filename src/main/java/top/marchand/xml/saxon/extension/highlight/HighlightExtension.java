/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.xml.saxon.extension.highlight;

import java.io.IOException;
import java.io.InputStreamReader;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/**
 * A function that escapes source code to HTML, using highlight.JS.
 * @author cmarchand
 */
public class HighlightExtension extends ExtensionFunctionDefinition {
    public static final String EXT_NAMESPACE_URI = "top:marchand:xml:extfunctions";
    public static final String EXT_FUNCTION_NAME = "highlight";
    public static final String EXT_PREFIX = "chm";
    public static final StructuredQName FUNCTION_QNAME = new StructuredQName(EXT_PREFIX, EXT_NAMESPACE_URI, EXT_FUNCTION_NAME);
    public static final SequenceType[] PARAMETERS_TYPES = new SequenceType[] {
        SequenceType.SINGLE_STRING, 
        SequenceType.SINGLE_STRING,
        MapType.OPTIONAL_MAP_ITEM
    };
    public static final String JS = "js";
    
    private static final String JS_RESOURCE = "/top/marchand/xml/saxon/extension/highlight/highlight.min.js";
    private static final String DEFAULT_RESULT_NAMESPACE = "http://www.w3.org/1999/xhtml";
    public static final AtomicValue NAMESPACE_ENTRY = new StringValue("result-ns");

    private Context context;
    private Value function;
    private DocumentBuilder builder;
    private XsltTransformer transf;

    public HighlightExtension() {
        super();
        try {
            context = Context.create();
            context.eval(
                Source.newBuilder(
                        JS, 
                        new InputStreamReader(getClass().getResourceAsStream(JS_RESOURCE)),
                        "highlight.min.js"
                ).build()
            );
            Value obj = context.getBindings(JS).getMember("hljs");
            function = obj.getMember("highlight");
        } catch(IOException ex) {
            ex.printStackTrace(System.err);
        }
        
    }
    @Override
    public StructuredQName getFunctionQName() {
        return FUNCTION_QNAME;
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return PARAMETERS_TYPES;
    }

    @Override
    public SequenceType getResultType(SequenceType[] sts) {
        return SequenceType.OPTIONAL_ITEM;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ExtensionFunctionCall() {
            @Override
            public Sequence call(XPathContext xpc, Sequence[] parameters) throws XPathException {
                if(parameters.length<2) {
                    throw new XPathException(EXT_PREFIX+":"+EXT_FUNCTION_NAME+" requires 2 parameters at least: (language as xs:string, sourceCode as xs:string, config as map()?)");
                }
                String language = parameters[0].head().getStringValue();
                if(language.isEmpty()) {
                    throw new XPathException(EXT_PREFIX+":"+EXT_FUNCTION_NAME+" first parameter language must not be empty");
                }
                String sourceCode = parameters[1].head().getStringValue();
                String resultNamespace = DEFAULT_RESULT_NAMESPACE;
                if(parameters.length==3) {
                    HashTrieMap config = (HashTrieMap)parameters[2].head();
                    if(config.get(NAMESPACE_ENTRY)!=null) {
                        resultNamespace = config.get(NAMESPACE_ENTRY).getStringValue();
                    }
                }
                // TODO
                return new StringValue(
                        function.execute(language,sourceCode).getMember("value").asString()
                );
            }
        };
    }

    @Override
    public int getMaximumNumberOfArguments() {
        return 2;
    }

    @Override
    public int getMinimumNumberOfArguments() {
        return 2;
    }

    @Override
    public boolean trustResultType() {
        return true;
    }
    
    
}
