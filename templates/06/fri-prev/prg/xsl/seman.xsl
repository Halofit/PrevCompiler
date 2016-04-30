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
	  <xsl:apply-templates select="ast"/>
	</tr>
      </table>
    </body>
  </html>
</xsl:template>

<xsl:template match="ast">
  <td>
    <table style="width:100%">
      <tr>
	<td colspan="100" style="background-color:#FCF265;font-family:helvetica;line-height:133%">
	  <text>&#xA0;</text>
	  <xsl:apply-templates select="position"/>
	  <text>&#xA0;</text>
	  <br/>
	  <text>&#xA0;</text>
	  <xsl:value-of select="@kind"/>
	  <text>&#xA0;</text>
	  <xsl:if test="@name!=''">
	    <br/>
	    <text>&#xA0;</text>
	    <font style="font-family:courier new">
	      <xsl:value-of select="@name"/>
	    </font>
	    <text>&#xA0;</text>
	  </xsl:if>
	  <xsl:apply-templates select="seman"/>
	</td>
      </tr>
      <tr>
	<xsl:apply-templates select="ast"/>
      </tr>
    </table>
  </td>
</xsl:template>

<xsl:template match="seman">
  <table style="width:100%;background-color:#D4CA90;border:3px solid #FCF265;width:100%;font-size:80%">
    <xsl:if test="@value!=''">
      <tr style="background-color:#D4CA90">
 	<td>
	  <text>&#xA0;</text>
 	  <span style="white-space:nowrap">
	    <xsl:text>value=</xsl:text>
	    <xsl:value-of select="@value"/>
 	  </span>
 	  <text>&#xA0;</text>
	</td>
      </tr>
    </xsl:if>
    <xsl:if test="@mem!=''">
      <tr style="background-color:#D4CA90">
 	<td>
	  <text>&#xA0;</text>
 	  <span style="white-space:nowrap">
	    <xsl:text>mem</xsl:text>
 	  </span>
 	  <text>&#xA0;</text>
	</td>
      </tr>
    </xsl:if>
    <xsl:if test="@decl!=''">
      <tr>
	<td>
	  <text>&#xA0;</text>
	  <span style="white-space:nowrap">
	    <xsl:text>@</xsl:text>
	    <xsl:value-of select="@decl"/>
	  </span>
	  <text>&#xA0;</text>
	</td>
      </tr>
    </xsl:if>
    <tr>
      <xsl:apply-templates select="typ"/>
    </tr>
  </table>
</xsl:template>

<xsl:template match="typ">
  <td>
    <table style="width:100%;border:1px solid black;width:100%;font-size:100%">
      <tr>
	<td colspan="100">
	  <span style="white-space:nowrap">
	    <text>&#xA0;</text>
	    <xsl:value-of select="@kind"/>
	    <text>&#xA0;</text>
	  </span>
	</td>
      </tr>
      <tr>
	<xsl:apply-templates select="typ"/>
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
