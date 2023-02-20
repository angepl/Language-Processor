# Language-Processor
This is a Java project implementing a language processor for some basic functionalities of JavaScript programming language. The program receives a text file with the source code written in JavaScript and it gives as output all the tokens that were detected, all the grammar rules of the syntax grammar that were used (in the same order as they were used) and the symbol tables of global and local range. If any type of error (lexical, syntax or semantic) is detected through the process, an error message with some relative information is printed.

***Important notice***
In order for the program to run on your computer you need to specify the paths of following files:
1. The file that contains the source code written in JavaScript. The path needs to be placed in line 6 of Main.java
2. The file in which you wish to print the list with the tokens. The path needs to be placed in line 13 of SyntacticAnalyzer.java
3. The file in which you wish to print the list of the syntax grammar rules that were used. The path needs to be placed in line 14 of SyntacticAnalyzer.java

## Introduction to the project
A language processor consists of 5 parts:
- Lexical Analyzer. It reads the characters of the source code one by one and constructs tokens. The tokens are being formed with the help of a Lexical Grammar that shows which combinations of characters are valid to construct a specific token and which are not.
- Syntactic Analyzer. It initiates the analysis of the source code by asking for tokens from the Lexical Analyzer. There is a Syntax Grammar that shows which combination of tokens can form a syntactically valid sentence. Each token that is received, is being filtered through that grammar in order to verify whether the token can be placed in this position, depending on the previous tokens. The Syntax Grammar is crucial for the implementation of the Language Processor and it is presented later in the text.
- Semantic Analyzer. It is responsible for checking whether an expression has a valid meaning in the language, by keeping track of some attributes for the symbols in the Syntax Grammar. That procedure includes the verification of storing the correct values in variables according to their type, whether a function call has as many parameters as arguments in the function declaration, etc..
- Symbol Table. It is a data structure that holds all the variables (including function names and arguments) of the source code. There is a local symbol table that stores the variables that exist within the implementaion of a function and can only be read by that function, and a global symbol table that stores all the global variables, the function names and the function arguments.
- Error Handler. It communicates with the Lexical, the Syntactic and the Semantic Analyzer in order to deal with any detected errors.

## Tokens
In this project valid tokens are considered:
- integers in [0, 32767] 
- phrases (strings) inside double quotation marks (i.e. "Hello World!")
- ++ 
- /+ 
- == 
- = 
- != 
- && 
- ; 
- ( 
- ) 
- { 
- } 
- ,
- identifiers of variables and functions starting with a letter or underscore _ (assuming they are not keywords) 
- the keywords; let, int, string, print, input, return, if, else, for, function

That means than the source code can only contain these expressions. In the text file, where the list of tokens is going to be printed, the tokens are going to have the following format <token_code, token_attribute>. Token code refers to the unique code of the token (in most cases it is the sequence of characters of the token) whereas token attribute is only used to hold the values of integer and phrase tokens, as well as and the names of the identifiers. Tokens are implemented through the Token class.

## Symbol Tables
As mentioned before there is one symbol table for every range (local and global). Both symbol tables are implemented with ArrayLists. Each element in the symbol table is an Element object and therefore the columns of the tables are defined by the fields of the Element class which are name, type, despl (refering to the memory address of the identifier), returnType (for functions), paramTypes (for functions) and paramQuantity (for functions). In the global symbol table only the paramTypes field is not shown. In the local symbol table we can only store local variables (function declaration is not valid inside another function) and therefore we only care about the name, type, and despl fields. The local symbol table gets destroyed exactly after it is printed.

## Syntactic Analyzer and Syntax Grammar
There many types of syntactic analyzers, however this project uses a predictive recursive descent parser. Information about what a predictive recursive descent parser is can be found [here](https://www.tutorialspoint.com/compiler_design/compiler_design_top_down_parser.htm). The syntax grammar that is presented below has been constructed especially for this kind of syntactic analyzer. There is a total of 59 rules in the grammar and each rule may consist of non-terminal symbols (capital letters, some of them followed by a number), terminal symbols (tokens) or both. 
***Lamda (λ) refers to a null character, for example if there is a rule D -> λ that means there is an option to skip the non-terminal symbol D whenever it is seen in the grammar. The terminal symbol 'id' refers to idientifiers (variable or function names).***
The Syntax Grammar is:

> 1) P -> B P                          
> 2) P -> S P
> 3) P -> F P
> 4) P -> λ

> 5) B -> if(E) B1
> 6) B -> let id T;
> 7) B -> for(I; J; U) {N}

> 8) S -> id S1
> 9) S -> print E ;
> 10) S -> input id;
> 11) S -> return X;

> 12) F -> function id H (A) {C}

> 13) E -> R W 
 
> 14) B1 -> S
> 15) B1 -> {N} B2

> 16) T -> int
> 17) T -> string

> 18) I -> id = U
> 19) I -> λ

> 20) J -> id J1

> 21) U -> V Y

> 22) N -> S
> 23) N -> B

> 24) S1 -> = E;
> 25) S1 -> (L);

> 26) X -> E
> 27) X -> λ

> 28) H -> T
> 29) H -> λ

> 30) A -> T id K
> 31) A -> λ

> 32) C -> B C
> 33) C -> S C
> 34) C -> λ

> 35) R -> U Z

> 36) W -> && R
> 37) W -> λ

> 38) B2 -> else {N}
> 39) B2 -> λ

> 40) J1 -> == U
> 41) J1 -> != U

> 42) V -> id V1
> 43) V -> (E)
> 44) V -> integer
> 45) V -> phrase

> 46) Y -> + V
> 47) Y -> λ

> 48) L -> E Q
> 49) L -> λ

> 50) K -> , T id K
> 51) K -> λ

> 52) Z -> == U
> 53) Z -> != U
> 54) Z -> λ

> 55) V1 -> ++
> 56) V1 -> (L)
> 57) V1 -> λ

> 58) Q -> , E Q
> 59) Q -> λ

