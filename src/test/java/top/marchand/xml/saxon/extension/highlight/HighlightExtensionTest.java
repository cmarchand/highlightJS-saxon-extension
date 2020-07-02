/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.xml.saxon.extension.highlight;

import java.io.File;
import java.io.FileInputStream;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltTransformer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author cmarchand
 */
public class HighlightExtensionTest {
    
    @Test
    public void declareFunctionTest() throws Exception {
        Configuration configuration = Configuration.newConfiguration();
        Processor proc = new Processor(configuration);
        proc.registerExtensionFunction(new HighlightExtension());
        assertTrue(true);
    }
    
    @Test()
    public void useFunctionNotLoadedTest() throws Exception {
        Configuration configuration = Configuration.newConfiguration();
        Processor proc = new Processor(configuration);
        XPathCompiler comp = proc.newXPathCompiler();
        comp.declareNamespace(HighlightExtension.EXT_PREFIX, HighlightExtension.EXT_NAMESPACE_URI);
        Exception ex = assertThrows(
                SaxonApiException.class,
                () -> comp.compile("chm:highlight('xml','<test>value</test>')")
        );
        assertEquals("Cannot find a 2-argument function named Q{top:marchand:xml:extfunctions}highlight()", ex.getMessage());
    }
    
    @Test
    public void useFunctionLoadedXmlTest() throws Exception {
        Configuration configuration = Configuration.newConfiguration();
        Processor proc = new Processor(configuration);
        proc.registerExtensionFunction(new HighlightExtension());
        XPathCompiler comp = proc.newXPathCompiler();
        comp.declareNamespace(HighlightExtension.EXT_PREFIX, HighlightExtension.EXT_NAMESPACE_URI);
        XPathSelector select = comp.compile("chm:highlight('xml','<test>value</test>')").load();
        XdmValue ret = select.evaluate();
        assertEquals(3,ret.size());
        XdmItem item = ret.itemAt(0);
        assertTrue(item.isNode());
        XdmNode node = (XdmNode)item;
        assertEquals("span", node.getNodeName().getLocalName());
        assertEquals(HighlightExtension.DEFAULT_RESULT_NAMESPACE, node.getNodeName().getNamespaceURI());
        Serializer ser = proc.newSerializer(System.out);
        ser.serializeXdmValue(ret);
        ser.close();
    }
    @Test
    public void useFunctionLoadedXmlOtherNSTest() throws Exception {
        Configuration configuration = Configuration.newConfiguration();
        Processor proc = new Processor(configuration);
        proc.registerExtensionFunction(new HighlightExtension());
        XPathCompiler comp = proc.newXPathCompiler();
        comp.declareNamespace(HighlightExtension.EXT_PREFIX, HighlightExtension.EXT_NAMESPACE_URI);
        XPathSelector select = comp.compile("chm:highlight('xml','<test>value</test>',map{'result-ns':'top:marchand:xml'})").load();
        XdmValue ret = select.evaluate();
        assertEquals(3,ret.size());
        XdmItem item = ret.itemAt(0);
        assertTrue(item.isNode());
        XdmNode node = (XdmNode)item;
        assertEquals("span", node.getNodeName().getLocalName());
        assertEquals("top:marchand:xml", node.getNodeName().getNamespaceURI());
    }
    
    @Test
    public void useFunctionLoadedJavaTest() throws Exception {
        Configuration configuration = Configuration.newConfiguration();
        Processor proc = new Processor(configuration);
        proc.registerExtensionFunction(new HighlightExtension());
        XPathCompiler comp = proc.newXPathCompiler();
        comp.declareNamespace(HighlightExtension.EXT_PREFIX, HighlightExtension.EXT_NAMESPACE_URI);
        String javaCode = "public class Test {\n" +
                "\tpublic static void main(String[] args) {\n"+
                "\t\tSystem.out.println(\"Hello World!\");\n"+
                "\t}\n"+
                "}";
        XPathSelector select = comp.compile("chm:highlight('java','"+javaCode+"')").load();
        XdmValue ret = select.evaluate();
        XdmNode node = (XdmNode)ret.itemAt(0);
        assertEquals("span", node.getNodeName().getLocalName());
        assertEquals(HighlightExtension.DEFAULT_RESULT_NAMESPACE, node.getNodeName().getNamespaceURI());
    }
    
    @Test
    public void transformXmlDocument() throws Exception {
        Configuration configuration = Configuration.newConfiguration();
        Processor proc = new Processor(configuration);
        proc.registerExtensionFunction(new HighlightExtension());
        XsltTransformer tr = proc.newXsltCompiler().compile(
                new StreamSource(new FileInputStream("src/test/xsl/top/marchand/xml/saxon/extension/highlight/formater.xsl"))
        ).load();
        tr.setSource(new StreamSource(new FileInputStream("src/test/xml/top/marchand/xml/saxon/extension/highlight/source-code.xml")));
        Serializer ser = proc.newSerializer(new File("result.html"));
        ser.setOutputProperty(Serializer.Property.INDENT, "true");
        ser.setOutputProperty(Serializer.Property.METHOD, "html");
        tr.setDestination(ser);
        tr.transform();
        System.out.println("Open result.html in your browser...");
    }
}
