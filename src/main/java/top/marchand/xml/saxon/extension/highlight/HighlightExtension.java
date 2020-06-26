/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.xml.saxon.extension.highlight;

import java.io.IOException;
import java.io.InputStreamReader;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
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
    public static final SequenceType[] PARAMETERS_TYPES = new SequenceType[] {SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING};
    public static final String JS = "js";
    
    private static final String JS_RESOURCE = "/top/marchand/xml/saxon/extension/highlight/highlight.min.js";

    private Context context;
    private Value function;

    public HighlightExtension() {
        super();
        try {
        context.eval(
            Source.newBuilder(
                    JS, 
                    new InputStreamReader(getClass().getResourceAsStream(JS_RESOURCE)),
                    "highlight.min.js").build());
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
        return SequenceType.SINGLE_STRING;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ExtensionFunctionCall() {
            @Override
            public Sequence call(XPathContext xpc, Sequence[] parameters) throws XPathException {
                if(parameters.length!=2) {
                    throw new XPathException(EXT_PREFIX+":"+EXT_FUNCTION_NAME+" requires 2 parameters: (language, sourceCode)");
                }
                String language = parameters[0].toString();
                if(language.isEmpty()) {
                    throw new XPathException(EXT_PREFIX+":"+EXT_FUNCTION_NAME+" first parameter language must not be empty");
                }
                String sourceCode = parameters[1].toString();
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
