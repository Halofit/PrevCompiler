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
	  <xsl:apply-templates select="frame"/>
	  <xsl:apply-templates select="access"/>
	  <xsl:apply-templates select="fragment"/>
	  <xsl:apply-templates select="imcode"/>
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

<xsl:template match="frame">
  <table style="width:100%;width:100%;font-size:80%">
    <tr style="background-color:#E8C31A">
      <td>
	<text>&#xA0;</text>
 	<span style="white-space:nowrap">
	  <xsl:text>level=</xsl:text>
	  <xsl:value-of select="@level"/>
	  <text>&#xA0;</text>
	  <xsl:text>label=</xsl:text>
	  <xsl:value-of select="@label"/>
	  <text>&#xA0;</text>
	  <xsl:text>size=</xsl:text>
	  <xsl:value-of select="@size"/>
 	</span>
 	<text>&#xA0;</text>
      </td>
    </tr>
    <tr style="background-color:#E8C31A">
      <td>
	<text>&#xA0;</text>
	<span style="white-space:nowrap">
	  <xsl:text>inpCall=</xsl:text>
	  <xsl:value-of select="@inpCallSize"/>
	  <text>&#xA0;</text>
	  <xsl:text>locVars=</xsl:text>
	  <xsl:value-of select="@locVarsSize"/>
	  <text>&#xA0;</text>
	  <xsl:text>tmpVars=</xsl:text>
	  <xsl:value-of select="@tmpVarsSize"/>
	  <text>&#xA0;</text>
	  <xsl:text>hidRegs=</xsl:text>
	  <xsl:value-of select="@hidRegsSize"/>
	  <text>&#xA0;</text>
	  <xsl:text>outCall=</xsl:text>
	  <xsl:value-of select="@outCallSize"/>
	</span>
	<text>&#xA0;</text>
      </td>
    </tr>
  </table>
</xsl:template>

<xsl:template match="access">
  <table style="width:100%;width:100%;font-size:80%">
    <tr style="background-color:#E8C31A">
      <td>
	<text>&#xA0;</text>
 	<span style="white-space:nowrap">
	  <xsl:if test="@level!=''">
	    <xsl:text>level=</xsl:text>
	    <xsl:value-of select="@level"/>
	    <text>&#xA0;</text>
	  </xsl:if>
	  <xsl:if test="@label!=''">
	    <xsl:text>label=</xsl:text>
	    <xsl:value-of select="@label"/>
	    <text>&#xA0;</text>
	  </xsl:if>
	  <xsl:if test="@offset!=''">
	    <xsl:text>offset=</xsl:text>
	    <xsl:value-of select="@offset"/>
	    <text>&#xA0;</text>
	  </xsl:if>
	  <xsl:text>size=</xsl:text>
	  <xsl:value-of select="@size"/>
 	</span>
 	<text>&#xA0;</text>
      </td>
    </tr>
  </table>
</xsl:template>

<xsl:template match="fragment">
  <table style="width:100%;background-color:#1FB1FE;border:3px solid #FCF265;font-size:80%">
    <tr>
      <xsl:apply-templates select="frg"/>
    </tr>
  </table>
</xsl:template>

<xsl:template match="frg">
  <td>
    <table style="width:100%;font-size:100%">
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
	<xsl:apply-templates select="imc"/>
      </tr>
    </table>
  </td>
</xsl:template>

<xsl:template match="imcode">
  <table style="width:100%;background-color:#8CD5FE;border:3px solid #FCF265;width:100%;font-size:80%">
    <tr>
      <xsl:apply-templates select="imc"/>
    </tr>
  </table>
</xsl:template>

<xsl:template match="imc">
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
	<xsl:apply-templates select="imc"/>
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
