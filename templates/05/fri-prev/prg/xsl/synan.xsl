<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="report">
  <html>
    <style>
      table, tr, td {
      text-align: center;
      vertical-align: top;
      }
    </style>
    <body>
      <table>
	<tr>
	  <td>
	    <xsl:apply-templates select="nont"/>
	  </td>
	</tr>
      </table>
    </body>
  </html>
</xsl:template>

<xsl:template match="nont">
  <td>
    <table style="width:100%">
      <tr>
	<td colspan="100" style="font-family:helvetica;background-color:FCF265;line-height:133%">
	  <text>&#xA0;</text>
	  <xsl:value-of select="@name"/>
	  <text>&#xA0;</text>
	</td>
      </tr>
      <tr>
	<xsl:apply-templates/>
      </tr>
    </table>
  </td>
</xsl:template>

<xsl:template match="term">
  <td>
    <table style="width:100%">
      <tr>
	<td style="font-family:helvetica;background-color:FFC400;line-height:133%">
	  <span style="white-space:nowrap">
	    <text>&#xA0;</text>
	    <xsl:apply-templates select="position"/>
	    <text>&#xA0;</text>
	  </span>
	  <br/>
	  <text>&#xA0;</text>
	  <xsl:value-of select="@name"/>
	  <text>&#xA0;</text>
	  <br/>
	  <xsl:if test="@lexeme!=''">
	    <text>&#xA0;</text>
	    <font style="font-family:courier new">
	      <xsl:value-of select="@lexeme"/>
	    </font>
	    <text>&#xA0;</text>
	  </xsl:if>
	</td>
      </tr>
    </table>
  </td>
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
