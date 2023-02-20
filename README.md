# Language-Processor
This is a Java project implementing a language processor for some basic functionalities of JavaScript programming language. The program receives a text file with the source code written in JavaScript and it gives as output all the tokens that were detected, all the grammar rules of the syntax grammar that were used (in the same order as they were used) and the symbol tables of global and local range. If any type of error (lexical, syntax or semantic) is detected through the process, an error message with some relative information is printed.

***Important notice***
In order for the program to run on your computer you need to specify the paths of following files:
1. The file that contains the source code written in JavaScript. The path needs to be placed in line 6 of Main.java
2. The file in which you wish to print the list with the tokens. The path needs to be placed in line 13 of SyntacticAnalyzer.java
3. The file in which you wish to print the list of the syntax grammar rules that were used. The path needs to be placed in line 14 of SyntacticAnalyzer.java
