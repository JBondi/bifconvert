grammar BifDne;

@header{
package org.jb.bifconvert.generated;
}
assignment : ID '=' fullValue;
struct     : ID ID? '{' (assignment ';' | struct ';')* '}';
array      : ('(' innerArray  ')') | ('(' ')') ;
innerArray : (fullValue ',')* fullValue ;
fullValue  : array | struct | ID | QSTRING | NUM ;

ID 		 : [A-Za-z][A-Za-z0-9_]* ;   
QSTRING  : '"'( ~[\"] | '\\"' | '\\'[\n\r])* '"' ;
WS       : [ \t] -> skip;
NUM 	 : INT|HEX|FLOAT;
fragment INT		 : [0-9]+;
fragment HEX		 : '0x' [A-Fa-f0-9]+ ;
fragment FLOAT 	 : INT? '.' INT ;
COMMENT  :  '//' ~( '\r' | '\n' )*  -> skip;
EOL		 : [\n\r] -> skip;
