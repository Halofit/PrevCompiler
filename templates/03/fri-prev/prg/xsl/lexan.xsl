<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="report">
  <html>
    <body>
      <xsl:apply-templates select="term"/>
    </body>
  </html>
</xsl:template>

<xsl:template match="term">
  <span style="white-space:nowrap">
    <font style="font-family:helvetica;background-color:FFC400;line-height:133%">
      <xsl:text>&#xA0;</xsl:text>
      <xsl:apply-templates select="position"/>
      <xsl:text>&#xA0;</xsl:text>
      <xsl:value-of select="@name"/>
      <xsl:if test="@lexeme!=''">
	<xsl:text>&#xA0;</xsl:text>
	<font style="font-family:courier new">
	  <xsl:value-of select="@lexeme"/>
	</font>
      </xsl:if>
      <xsl:text>&#xA0;</xsl:text>
    </font>
  </span>
  <xsl:if test="position()!=last()">
    <xsl:text>&#x20;</xsl:text>
  </xsl:if>
  <br/>
</xsl:template>

<xsl:template match="position">
  <span style="white-space:nowrap">
    <xsl:text>[</xsl:text>
    <xsl:value-of select="@begLine"/>
    <xsl:text>.</xsl:text>
    <xsl:value-of select="@begColumn"/>
    <xsl:text>-</xsl:text>
    <xsl:value-of select="@endLine"/>
    <xsl:text>.</xsl:text>
    <xsl:value-of select="@endColumn"/>
    <xsl:text>]</xsl:text>
  </span>
</xsl:template>

</xsl:stylesheet>
