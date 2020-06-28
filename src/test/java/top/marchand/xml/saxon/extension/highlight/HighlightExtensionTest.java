/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.xml.saxon.extension.highlight;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmValue;
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
    public void useFunctionLoadedTest() throws Exception {
        Configuration configuration = Configuration.newConfiguration();
        Processor proc = new Processor(configuration);
        proc.registerExtensionFunction(new HighlightExtension());
        XPathCompiler comp = proc.newXPathCompiler();
        comp.declareNamespace(HighlightExtension.EXT_PREFIX, HighlightExtension.EXT_NAMESPACE_URI);
        XPathSelector select = comp.compile("chm:highlight('xml','<test>value</test>')").load();
        XdmValue ret = select.evaluate();
        System.out.println(ret.toString());
    }
    
}
