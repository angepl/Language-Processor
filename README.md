# Language-Processor
This is a Java project implementing a language processor for some basic functionalities of JavaScript programming language. The program receives a text file with the source code written in JavaScript and it gives as output all the tokens that were detected, all the grammar rules of the syntax grammar that were used (in the same order as they were used) and the symbol tables of global and local range. If any type of error (lexical, syntax or semantic) is detected through the process, an error message with some relevant information is printed.

***Important notice:***
In order for the program to run on your computer you need to specify the paths of the following files:
1. The file that contains the source code written in JavaScript. The path needs to be placed in line 6 of Main.java
2. The file in which you wish to print the list with the tokens. The path needs to be placed in line 13 of SyntacticAnalyzer.java
3. The file in which you wish to print the list of the syntax grammar rules that were used. The path needs to be placed in line 14 of SyntacticAnalyzer.java

## Introduction to the project
A language processor consists of 5 parts:
- Lexical Analyzer. It reads the characters of the source code one by one and constructs tokens. The tokens are being formed with the help of a Lexical Grammar and a Transition Matrix that shows which combinations of characters are valid to construct a specific token and which are not.
- Syntactic Analyzer. It initiates the analysis of the source code by asking for tokens from the Lexical Analyzer. There is a Syntax Grammar that shows which combination of tokens can form a syntactically valid sentence. Each token that is received, is being filtered through that grammar in order to verify whether the token can be placed in this position, depending on the previous tokens. The Syntax Grammar is crucial for the implementation of the Language Processor and it is presented later in the text.
- Semantic Analyzer. It is responsible for checking whether an expression has a valid meaning in the language, by keeping track of some attributes for the symbols in the Syntax Grammar. That procedure includes the verification of storing the correct values in variables according to their type, whether a function call has as many parameters as arguments in the function declaration, etc..
- Symbol Table. It is a data structure that holds all the variables (including function names and arguments) of the source code. There is a local symbol table that stores the variables that exist within the implementation of a function and can only be read by that function, and a global symbol table that stores all the global variables, the function names and the function arguments.
- Error Handler. It communicates with the Lexical, the Syntactic and the Semantic Analyzer in order to deal with any detected errors.

## Functionalities of this Language Processor
This Language Processor implements only some basic functionalities of JavaScript. These are:
- Comments using the format ```/*Type comment here*/```
- Integer numbers
- Phrases (strings)
- Arithmetic operator ```+```, applicable on integer expressions, giving an integer as result
- Relational operator ```==```, applicable on integer, string or boolean expressions, giving a boolean as a result 
- Relational operator ```!=```, applicable on integer, string or boolean expressions, giving a boolean as a result
- Logical operator ```&&```, applicable on boolean expressions, giving a boolean as a result
- Increasing operator ```++```, applicable on integer expressions, giving an integer as a result
- Asignation operator ```=``` 
- Variables names, which can start with a letter or underscore. If a variable that has not been declared is used, it is considered global and of integer type. JavaScript is case-sensitive
- Variable declaration with the expression ```let variable_name type;```
- Data types ```int``` and ```string```. Integer variables occupy 16 bits in the memory, while strings occupy 1024 bits
- Print command with the expression ```print expression ;```
- Input command with the expression ```input variable_name ;```
- Function calls with the name of the function followed by the correct number and type of parameters
- Return statement with the expression ```return expression ;```. The expression in the return statement needs to match the return type of the function
- If-statement, followed by a condition, with the format ```if(condition) {...}```. The condition needs to be a boolean expression.
- Else-statement used after an if-statement with the format ```if(condition) {...} else {...}```
- For-loop using the format ```for(initialization; condition; action;) {...}```. The initialization can be an asignation sentence or nothing and the condition needs to be a boolean expression
- Function declaration with the expression ```function function_name type (type1 arg1, type2 arg2,...)```. There might be no arguments in the function. A function can call itself. A function cannot be declared inside another function.

## Tokens
In this project valid tokens are considered:
- integers in [0, 32767] 
- phrases (strings) inside double quotation marks (i.e. "Hello World!")
- ++ 
- \+ 
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

That means than the source code can only contain these expressions. In the text file, where the list of tokens is going to be printed, the tokens are going to have the following format <token_code, token_attribute>. Token code refers to the unique code of the token (in most cases it is the sequence of characters of the token) whereas token attribute is only used to hold the values of integer and phrase tokens, as well as the names of the identifiers. Tokens are implemented through the Token class. Characters used for comments are completely ignored by the Lexical Analyzer and cannot form a token!

## Symbol Tables
As mentioned before there is one symbol table for every range (local and global). Both symbol tables are implemented with ArrayLists. Each element in the symbol table is an Element object and therefore the columns of the tables are defined by the fields of the Element class which are name, type, despl (refering to the memory address of the identifier), returnType (for functions), paramTypes (for functions) and paramQuantity (for functions). In the global symbol table only the paramTypes field is not shown. In the local symbol table we can only store local variables (function declaration is not valid inside another function) and therefore we only care about the name, type, and despl fields. The local symbol table gets destroyed exactly after it is printed.

## Syntactic Analyzer and Syntax Grammar
There many types of syntactic analyzers, however this project uses a predictive recursive descent parser. Information about what a predictive recursive descent parser is can be found [here](https://www.tutorialspoint.com/compiler_design/compiler_design_top_down_parser.htm). The syntax grammar that is presented below has been constructed especially for this kind of syntactic analyzer. There is a total of 59 rules in the grammar and each rule may consist of non-terminal symbols (capital letters, some of them followed by a number), terminal symbols (tokens) or both. 

***Important notice:***
Lamda (λ) refers to a null character, for example if there is a rule D -> λ, that means there is an option to skip the non-terminal symbol D whenever it is seen in the grammar. The terminal symbol 'id' refers to idientifiers (variable or function names).

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

The non-terminal P is the axiom of the grammar, which means that the analysis of the source code begins from one of the rules of P. In the program implementation, each non-terminal symbol is represented by a function inside the syntacticAnalyzer class and each rule of that non-terminal is represented by an *if case* inside its function. Sometimes, in order to decide the next rule to be followed, we use the FIRST or FOLLOW of that specific non-terminal. Information about what FIRST and FOLLOW is can be found [here](https://www.tutorialspoint.com/what-are-first-and-follow-and-how-they-are-computed).

## Semantic Analyzer
The Semantic Analyzer in this project is implemented inside the Syntactic Analyzer. That is because we want the Semantic Analyzer to act depending on the syntax rule, and therefore the semantic actions take place inside the functions that belong to the non-terminal symbols. It uses attributes of the non-terminal symbols of the Syntax Grammar to check their semantic correctness. The most common attribute - used for every non-terminal - is the type attribute that can be int, string, boolean, function, error or ok, depending on the non-terminal symbol. The semantic analyzer is in charge of many things throughout the analysis of the soure code, such as the creation and destruction of the symbol table, checking if the value of a return statement matches the return type of the function, storing the identifiers' attributes in the symbol table and many more. Although the lexical and syntax errors are being handled by the Error Handler in Error class, in this program the semantic errors are directly printed by the Semantic Analyzer due to their complexity.

## Information about variables and function of the project
- **character** (variable inside LexicalAnalyzer class) - refers to the character that was last read by the Lexical Analyzer and represents the column of the Transmition Matrix in the Lexical Analysis
- **state** (variable inside LexicalAnalyzer class) - refers to the row of the Transition Matrix in the Lexical Analysis
- **line** (variable inside LexicalAnalyzer class) - keeps track of the current line that is being analyzed by the Language Processor
- **name** (field of Element class) - holds the name of the element in the symbol table
- **type** (field of Element class) - holds the type of the element in the symbol table
- **despl** (field of Element class) - holds the memory address of the element in the symbol table
- **returnType** (field of Element class) - holds the return type of the element in the symbol table, in case it is a function
- **paramTypes** (field of Element class) - holds the the types of the arguments of the element in the symbol table, in case it is a function
- **paramQuantity** (field of Element class) - holds the number of arguments of the element in the symbol table, in case it is a function
- **code** (field of Token class) - holds the unique code of the token
- **attribute** (field of Token class) - holds supplementary information about the token (in case it is an integer, a phrase or an identifier - otherwise it is null)
- **symbolTable** (ArrayList in SyntacticAnalyzer class) - refers to the global symbol table
- **localTable** (ArrayList in SyntacticAnalyzer class) - refers to the local symbol table
- **inFunction** (variable of SyntacticAnalyzer class) - it is true when we are currently inside a function implementation
- **declMode** (variable of SyntacticAnalyzer class) - it is true when we are currently using rule 6 (B -> let id T;) which is the syntax rule for variable declaration
- **analysis(File text)** (function of SyntacticAnalyzer class) - it initiates the analysis by asking the Lexical Analyzer for the first token and calling the the P() function, which is the function of the axiom
- **compare(int code)** (function of SintacticAnalyzer class) - it is being called by the functions that belong to the non-terminal symbols, in order to verify that the current token is the token that was expected according to the syntax rule. It also asks the Lexical Analyzer for the next token
- **first and follow functions** (inside SyntacticAnalyzer class) - they check whether the current token belongs to FIRST or FOLLOW set of a specific non-terminal symbol
- **varExists(ArrayList\<Element> table, String name)** (inside SyntacticAnalyzer class) - returns true if there is an element with a specific *name* inside the symbol table defined by *table*
- **searchElement(ArrayList\<Element> table, String name)** (inside SyntacticAnalyzer class) - searches for an element with a specific *name* inside the symbol table defined by *table*. If it finds such an element, it reutrns it
