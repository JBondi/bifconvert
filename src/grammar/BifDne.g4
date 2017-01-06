grammar BifDne;

@header{
package org.jb.bifconvert.generated;
}
dne		   : struct*;
assignment : ID '=' (fullValue ';' | struct);
struct     : ID (ID|NUM)? '{' (assignment | struct )* '}' ';';
array      : ('(' innerArray  ')') | ('(' ')') ;
innerArray : ((fullValue ',')* fullValue) | ((fullValue ' ')* fullValue);
fullValue  : array | ID | QSTRING | NUM ;

ID 		 : [A-Za-z][A-Za-z0-9_]*;   
QSTRING  : '"'( ~[\"] | '\\"' | '\\'[\n\r])* '"' ;
WS       : '\t' -> skip;
SPACE	 : ' ' -> skip;
NUM 	 : INT|HEX|FLOAT;
INT		 : [0-9]+;
HEX		 : '0x' [A-Fa-f0-9]+ ;
FLOAT 	 : ([0-9]+)? '.' [0-9]+ ('e'('+'|'-')?[0-9]+)?;
COMMENT  :  '//' ~( '\r' | '\n' )*  -> skip;
EOL		 : [\n\r] -> skip;
