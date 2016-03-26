#! /bin/bash

cat ../prg/src/compiler/phase/synan/prev-lr.grammar |\
gawk '\
  {for(F=1;F<=NF;F++){\
     if(match($F,/(->)|\||\./)>0)print($F);\
     else if(match($F,/[a-z]/)>0)print("\\nont{"$F"}");\
     else print("\\term{"tolower($F)"}");}}' |\
gawk '\
  BEGIN{PREV=0;LP="";RP="";}\
  /\./{if(PREV)print "\\smallskip";print "\\production{ "LP" }{"RP" }";PREV=1;LP="";RP="";next;}\
  /\|/{if(PREV)print "\\smallskip";print "\\production{ "LP" }{"RP" }";PREV=0;RP="";next;}\
  /->/{next;}\
  {if(LP=="")LP=$1;else RP=RP" "$1;next;}' |\
sed 's/term{add}/term{\\symbol{"2B}}/g' |\
sed 's/term{and}/term{\\symbol{"26}}/g' |\
sed 's/term{assign}/term{\\symbol{"3D}}/g' |\
sed 's/term{colon}/term{\\symbol{"3A}}/g' |\
sed 's/term{comma}/term{\\symbol{"2C}}/g' |\
sed 's/term{closing_brace}/term{\\symbol{"7D}}/g' |\
sed 's/term{closing_bracket}/term{\\symbol{"5D}}/g' |\
sed 's/term{closing_parenthesis}/term{\\symbol{"29}}/g' |\
sed 's/term{dot}/term{\\symbol{"2E}}/g' |\
sed 's/term{div}/term{\\symbol{"2F}}/g' |\
sed 's/term{equ}/term{\\symbol{"3D}\\symbol{"3D}}/g' |\
sed 's/term{geq}/term{\\symbol{"3E}\\symbol{"3D}}/g' |\
sed 's/term{gth}/term{\\symbol{"3E}}/g' |\
sed 's/term{leq}/term{\\symbol{"3C}\\symbol{"3D}}/g' |\
sed 's/term{lth}/term{\\symbol{"3C}}/g' |\
sed 's/term{mem}/term{\\symbol{"40}}/g' |\
sed 's/term{mod}/term{\\symbol{"25}}/g' |\
sed 's/term{mul}/term{\\symbol{"2A}}/g' |\
sed 's/term{neq}/term{\\symbol{"21}\\symbol{"3D}}/g' |\
sed 's/term{not}/term{\\symbol{"21}}/g' |\
sed 's/term{opening_brace}/term{\\symbol{"7B}}/g' |\
sed 's/term{opening_bracket}/term{\\symbol{"5B}}/g' |\
sed 's/term{opening_parenthesis}/term{\\symbol{"28}}/g' |\
sed 's/term{or}/term{\\symbol{"7C}}/g' |\
sed 's/term{sub}/term{\\symbol{"2D}}/g' |\
sed 's/term{val}/term{\\symbol{"5E}}/g' |\
sed 's/term{const_integer}/term{INTEGER}/g' |\
sed 's/term{const_boolean}/term{BOOLEAN}/g' |\
sed 's/term{const_char}/term{CHAR}/g' |\
sed 's/term{const_string}/term{STRING}/g' |\
sed 's/term{const_null}/term{null}/g' |\
sed 's/term{const_none}/term{none}/g' |\
sed 's/term{identifier}/term{IDENTIFIER}/g' |\
sed 's/term{#}/\term{}/g' > grammar.tex
