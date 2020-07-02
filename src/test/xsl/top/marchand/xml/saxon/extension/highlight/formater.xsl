<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:math="http://www.w3.org/2005/xpath-functions/math"
  xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  xmlns:chm="top:marchand:xml:extfunctions"
  xmlns:html="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all"
  version="3.0">
  <xd:doc scope="stylesheet">
    <xd:desc>
      <xd:p><xd:b>Created on:</xd:b> Jul 2, 2020</xd:p>
      <xd:p><xd:b>Author:</xd:b> cmarchand</xd:p>
      <xd:p></xd:p>
    </xd:desc>
  </xd:doc>
  
  <xsl:output method="html" indent="true"/>
  
  <xsl:template match="/data">
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <link href="src/test/resources/top/marchand/xml/saxon/extension/highlight/default.min.css" rel="stylesheet" type="text/css"/>
      </head>
      <body>
        <h1>Source code examples</h1>
        <p>Highlight by chm:highlight extension function.</p>
        <p>It uses <a href="https://highlightjs.org/">highlight.js</a> under <a href="LICENSE/highlight-js_BSD-3.txt">BSD License</a>.</p>
      </body>
      <xsl:apply-templates/>
    </html>
  </xsl:template>
  
  <xsl:template match="code" expand-text="true">
    <h2>Highliting {@lang}...</h2>
    <div xmlns="http://www.w3.org/1999/xhtml">
      <pre><code class="language-{@lang} hljs"><xsl:sequence select="chm:highlight(@lang,./text())"/></code></pre>
    </div>
  </xsl:template>
</xsl:stylesheet>