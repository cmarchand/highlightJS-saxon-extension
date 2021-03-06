/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.xml.saxon.extension.highlight;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
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
    
    private static final String JS_RESOURCE_FILENAME = "highlight.pack.js";
    private static final String JS_RESOURCE = "/top/marchand/xml/saxon/extension/highlight/"+JS_RESOURCE_FILENAME;
    public static final String DEFAULT_RESULT_NAMESPACE = "http://www.w3.org/1999/xhtml";
    public static final AtomicValue NAMESPACE_ENTRY = new StringValue("result-ns");
    public static final QName PARAM_TARGET_NS = new QName("targetNS");

    private Context context;
    private Value function;

    public HighlightExtension() {
        super();
        try {
            context = Context.create();
            context.eval(
                Source.newBuilder(
                        JS, 
                        new InputStreamReader(getClass().getResourceAsStream(JS_RESOURCE)),
                        JS_RESOURCE_FILENAME
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
                    MapItem config = (MapItem)parameters[2].head();
                    if(config.get(NAMESPACE_ENTRY)!=null) {
                        resultNamespace = config.get(NAMESPACE_ENTRY).getStringValue();
                    }
                }
                
                String escaped = function.execute(language,sourceCode).getMember("value").asString();
                String wrapped = "<wrapper>"+escaped+"</wrapper>";
                Processor proc = new Processor(xpc.getConfiguration());
                try {
                    XdmNode tree = proc.newDocumentBuilder().build(new StreamSource(new StringReader(wrapped)));
                    XsltTransformer tr = proc.newXsltCompiler().compile(new StreamSource(getClass().getResourceAsStream("/top/marchand/xml/saxon/extension/highlight/namespace-changer.sef"))).load();
                    tr.setInitialContextNode(tree);
                    tr.setParameter(PARAM_TARGET_NS, XdmValue.makeValue(resultNamespace));
                    XdmDestination xslResult = new XdmDestination();
                    tr.setDestination(xslResult);
                    tr.transform();
                    XPathCompiler compiler = proc.newXPathCompiler();
                    compiler.declareNamespace("h", resultNamespace);
                    XPathSelector xs = compiler.compile("/h:wrapper/node()").load();
                    xs.setContextItem(xslResult.getXdmNode());
                    XdmValue ret = xs.evaluate();
                    return ret.getUnderlyingValue();
                } catch(SaxonApiException ex) {
                    throw new XPathException(ex);
                }
            }
        };
    }

    @Override
    public int getMaximumNumberOfArguments() {
        return 3;
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
