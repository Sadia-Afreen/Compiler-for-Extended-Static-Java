grammar ExtendedStaticJava;
//RQ1
program
  : simpleClassDeclaration*
    classDefinition
    simpleClassDeclaration*
  ;
//RQ2
simpleClassDeclaration
  : 'class' ID '{'
    publicFieldDeclaration*
    '}'
  ;

//RQ3
publicFieldDeclaration
  : 'public' type ID ';'
  ;

//RQ4
type
  : (basicType | ID) ('[' ']')?
  ;

compilationUnit
  : program EOF
  ;

classDefinition
  : 'public' 'class' ID '{'
    mainMethodDeclaration
    memberDeclaration*
    '}'
  ;

memberDeclaration
  : fieldDeclaration | methodDeclaration
  ;

mainMethodDeclaration
  : 'public' 'static' 'void'
    id1=ID                   { "main".equals($id1.text) }?
    '(' id2=ID               { "String".equals($id2.text) }?
    '[' ']' id3=ID ')'
    '{' methodBody '}'
  ;

fieldDeclaration
  : 'static' type ID ';'
  ;

methodDeclaration
  : 'static' returnType ID '(' params? ')' '{' methodBody '}'
  ;

basicType
  : 'boolean'                #BooleanType
  | 'int'                    #IntType
  ;

returnType
  : 'void'                   #VoidType
  | type                     #NonVoidReturnType
  ;

params
  : param ( ',' param )*
  ;

param
  : type ID
  ;

methodBody
  : localDeclaration* statement*
  ;

localDeclaration
  : type ID ';'
  ;

//RQ5
statement
  : assignStatement
  | ifStatement
  | whileStatement
  | invokeExpStatement
  | returnStatement
  | forStatement
  | doWhileStatement
  | incDecStatement
  ;

//RQ6
incDecStatement
  : incDec ';'
  ;

//RQ7
assignStatement
  : assign ';'
  ;

assign
  : lhs '=' exp
  ;

//RQ8
lhs
  : ID | exp '.' ID | exp '[' exp ']'
  ;

//RQ9
forStatement
  : 'for' '(' forInits? ';' exp? ';' forUpdates? ')'
    '{' statement* '}'
  ;
//RQ10
forInits
  : assign (',' assign)*
  ;

//RQ11
forUpdates
  : incDec ( ',' incDec )*
  ;

//RQ12
incDec
  : lhs '++' | lhs '--'
  ;

//RQ13
doWhileStatement
  : 'do' '{' statement* '}' 'while' '(' exp ')' ';'
  ;
ifStatement
  : 'if' '(' exp ')' '{' ts+=statement* '}'
    ( 'else' '{' fs+=statement* '}' )?
  ;

whileStatement
  : 'while' '(' exp ')' '{' statement* '}'
  ;

invokeExpStatement
  : invoke ';'
  ;

returnStatement
  : 'return' ( exp )? ';'
  ;

//RQ14
exp
  : literalExp | unaryExp | parenExp | invoke | varRef | newExp
  | e1=exp binaryOp e2=exp
  | e1=exp '?' e2=exp ':' e3=exp
  | e1=exp '.' ID
  | e1=exp '[' e2=exp ']'
  ;

//RQ15
literalExp
  : booleanLiteral | NUM | 'null'
  ;

unaryExp
  : unaryOp exp
  ;

//RQ16
unaryOp
  : '+' | '-' | '!' | '~'
  ;

booleanLiteral
  : 'true' | 'false'
  ;

//RQ17
binaryOp
  : '+' | '-' | '*' | '/' | '%' | '>' | '>=' | '==' | '<'
     | '<=' | '!=' | '&&' | '||' | '<<' | '>>' | '>>>'
  ;

parenExp
  : '('exp')'
  ;

invoke
  : ( id1=ID '.' )? id2=ID '(' args? ')'
  ;

args
  : exp ( ',' exp )*
  ;

varRef
  : ID
  ;

//RQ19
newExp
  : 'new' ID '(' ')'
  | 'new' type '[' exp ']'
  | 'new' type '[' ']' arrayInit
  ;

//RQ20
arrayInit
  : '{' exp ( ',' exp )* '}'
  ;

ID
  : ( 'a'..'z' | 'A'..'Z' | '_' | '$' )
    ( 'a'..'z' | 'A'..'Z' | '_' | '0'..'9' | '$' )*
  ;

NUM
  : '0' | ('1'..'9') ('0'..'9')*
  ;

// Whitespace -- ignored
WS
  : [ \r\t\u000C\n]+ -> skip
  ;

// Any other character is an error character
ERROR
  : .
  ;