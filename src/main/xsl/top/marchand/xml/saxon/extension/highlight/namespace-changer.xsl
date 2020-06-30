<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:math="http://www.w3.org/2005/xpath-functions/math"
  xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  exclude-result-prefixes="xs math xd"
  version="3.0">
  <xd:doc scope="stylesheet">
    <xd:desc>
      <xd:p>Change all elements namespace to parameter-defined namespace</xd:p>
    </xd:desc>
  </xd:doc>
  
  <xsl:param name="targetNS" as="xs:string"/>
  
  <xsl:mode on-no-match="shallow-copy"/>

  <xsl:template match="*">
    <xsl:element name="{local-name(.)}" namespace="{$targetNS}">
      <xsl:apply-templates select="node() | @*" mode="#current"/>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>