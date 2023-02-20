# Language-Processor
This is a Java project implementing a language processor for some basic functionalities of JavaScript programming language. The program receives a text file with the source code written in JavaScript and it gives as output all the tokens that were detected, all the grammar rules of the syntax grammar that were used (in the same order as they were used) and the symbol tables of global and local range. If any type of error (lexical, syntax or semantic) is detected through the process, an error message with some relative information is printed.

***Important notice***
In order for the program to run on your computer you need to specify the paths of following files:
1. The file that contains the source code written in JavaScript. The path needs to be placed in line 6 of Main.java
2. The file in which you wish to print the list with the tokens. The path needs to be placed in line 13 of SyntacticAnalyzer.java
3. The file in which you wish to print the list of the syntax grammar rules that were used. The path needs to be placed in line 14 of SyntacticAnalyzer.java

##Introduction to the project
A language processor consists of 5 parts:
- Lexical Analyzer. It reads the characters of the source code one by one and constructs tokens. The tokens are being formed with the help of a Lexical Grammar that shows which combinations of characters are valid to construct a specific token and which are not.
- Syntactic Analyzer. It initiates the analysis of the source code by asking for tokens from the Lexical Analyzer. There is a Syntax Grammar that shows which combination of tokens can form a syntactically valid sentence. Each token that is received, is being filtered through that grammar in order to verify whether the token can be placed in this position, depending on the previous tokens. The Syntax Grammar is crucial for the implementation of the Language Processor and it is presented later in the text.
- Semantic Analyzer. It is responsible for checking whether an expression has a valid meaning in the language, by keeping track of some attributes for the symbols in the Syntax Grammar. That procedure includes the verification of storing the correct values in variables according to their type, whether a function call has as many parameters as arguments in the function declaration, etc..
- Symbol Table. It is a data structure that holds all the variables (including function names and arguments) of the source code. There is a local symbol table that stores the variables that exist within the implementaion of a function and can only be read by that function, and a global symbol table that stores all the global variables, the function names and the function arguments.
- Error Handler. It communicates with the Lexical, the Syntactic and the Semantic Analyzer in order to deal with any detected errors.

##Tokens
In this project valid tokens are considered; integers in [0, 32767],  
