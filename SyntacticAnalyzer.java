import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SyntacticAnalyzer {
    Token token = new Token(0, null);
    Error error = new Error();
    LexicalAnalyzer lexical = new LexicalAnalyzer();
    ArrayList<Element> symbolTable, localTable;
    int despl, localDespl, index;
    boolean STCreated, inFunction;
    boolean declMode, skip = false;
    FileWriter myWriter = new FileWriter("C:\\Users\\Vangelis\\OneDrive\\Desktop\\Code\\Java\\LanguageProcessing\\src\\token.txt");
    FileWriter parse = new FileWriter("C:\\Users\\Vangelis\\OneDrive\\Desktop\\Code\\Java\\LanguageProcessing\\src\\parse.txt");

    FileReader fr;
    BufferedReader br;
    String tokenToString = null;
    public SyntacticAnalyzer() throws IOException {
    }

    public void analysis(File text) throws IOException, NullPointerException {
        try {
            fr = new FileReader(text); //create reader
            br = new BufferedReader(fr); //create reader
            STCreated = false;

            token = lexical.lexicalAnalysis(br); //ask for the next token

            if (token != null) {
                switch(token.code) {
                    case 100: tokenToString = "integer"; break;
                    case 101: tokenToString = "phrase"; break;
                    case 102: tokenToString = "++"; break;
                    case 103: tokenToString = "+"; break;
                    case 104: tokenToString = "=="; break;
                    case 105: tokenToString = "="; break;
                    case 106: tokenToString = "!="; break;
                    case 107: tokenToString = "&&"; break;
                    //108 is occupied for keywords and variables
                    case 109: tokenToString = ";"; break;
                    case 110: tokenToString = "("; break;
                    case 111: tokenToString = ")"; break;
                    case 112: tokenToString = ","; break;
                    case 113: tokenToString = "{"; break;
                    case 114: tokenToString = "}"; break;

                    case 120: tokenToString = "let"; break;
                    case 121: tokenToString = "int"; break;
                    case 122: tokenToString = "string"; break;
                    case 123: tokenToString = "print"; break;
                    case 124: tokenToString = "input"; break;
                    case 125: tokenToString = "return"; break;
                    case 126: tokenToString = "if"; break;
                    case 127: tokenToString = "else"; break;
                    case 128: tokenToString = "for"; break;
                    case 129: tokenToString = "function"; break;
                    case 130: tokenToString = "identifier"; break;
                }
                myWriter.write("<" + tokenToString + ", " + token.attribute + ">\n");
            } else
                return ; //if there is a lexical error stop execution

            String P_type = P();
            myWriter.close();
            parse.close();

            printTable(symbolTable);

            if(P_type == "ok")
                System.out.println("\n\nText analyzed successfully!");

        } catch(NullPointerException e) {
            System.out.println("Semantic error caused execution termination!");
            System.exit(0);
        }
    }

    //P function
    private String P() throws IOException {
        String P_type = null;
        if(firstB(token)) {
            parse.write("1\n"); //rule 1
            if(STCreated == false) {
                symbolTable = new ArrayList(); //create symbol table
                STCreated = true;
                despl = 0;
            }
            String B_type = B();
            String inner_P_type = P();

            if(B_type.equals("ok")) {
                if(inner_P_type.equals("ok"))
                    P_type = "ok";
                else
                    P_type = "error";
            } else
                P_type = "error";
        } else if(firstS(token)) {
            parse.write("2\n"); //rule 2
            if(STCreated == false) {
                symbolTable = new ArrayList<>(); //create symbol table
                STCreated = true;
                despl = 0;
            }

            if(token.code == 130) { //if token is a variable
                if(varExists(symbolTable, token.attribute))
                    ;
                else //if its name is not in the symbol table then put it
                    symbolTable.add(new Element(token.attribute, "int", 0, null, null, 0));
            }

            String[] S_attributes = S();
            String S_type = S_attributes[0];

            String inner_P_type = P();

            if(S_type.equals("ok")) {
                if(inner_P_type.equals("ok"))
                    P_type = "ok";
                else
                    P_type = "error";
            } else
                P_type = "error";
        } else if(firstF(token)) {
            parse.write("3\n"); //rule 3
            if(STCreated == false) {
                symbolTable = new ArrayList<>(); // create symbol table
                STCreated = true;
                despl = 0;
            }

            inFunction = true;
            String F_type = F();
            inFunction = false;

            String inner_P_type = P();

            if(F_type.equals("ok")) {
                if(inner_P_type.equals("ok"))
                    P_type = "ok";
                else
                    P_type = "error";
            } else
                P_type = "error";
        } else if(followP(token)) {
            parse.write("4\n"); //rule 4
            P_type = "ok";
        } else
            error.syntaxError(token, lexical.line);

        return P_type;
    }

    //B function
    private String B() throws IOException {
        String B_type = null;
        if(token.code == 126) {
            parse.write("5\n"); //rule 5
            compare(126);
            compare(110);
            int line_E = lexical.line; String E_type = E();
            compare(111);
            String B1_type = B1();

            if(E_type.equals("boolean")) { //semantic actions
                if(B1_type.equals("ok"))
                    B_type = "ok";
                else {
                    B_type = "error";
                    System.out.println("Semantic error inside B1, in rule 5 (B -> if(E)B1).");
                }
            } else {
                B_type = "error";
                System.out.println("Semantic error inside E, in rule 5 (B -> if(E)B1) in line " + line_E + ".");
            }
        } else if(token.code == 120) {
            parse.write("6\n"); //rule 6
            declMode = true;
            compare(120);
            String varName = compare(130);

            String[] T_attributes = new String[2]; //array where T attributes are going to be returned

            int line_T = lexical.line; T_attributes = T();

            String T_type = T_attributes[0];
            int T_size = Integer.parseInt(T_attributes[1]);

            declMode = false;

            compare(109);

            //add variable type and position displacement in the table
            if(inFunction) {
                if(skip) {
                    System.out.println("Semantic error in rule 6 (B -> let id T;) in line " + line_T + ". There has already been declared a variable with the name " + varName + "!");
                    skip = false;
                }
                Element element = searchElement(localTable, varName);
                element.type = T_type;
                element.despl = localDespl;
                localDespl += T_size;
            } else {
                if(skip) {
                    System.out.println("Semantic error in rule 6 (B -> let id T;) in line " + line_T + ". There has already been declared a variable with the name " + varName + "!");
                    skip  = false;
                } else {
                    Element element = searchElement(symbolTable, varName);
                    element.type = T_type;
                    element.despl = despl;

                    despl += T_size;
                }
            }
            B_type = "ok";
        } else if(token.code == 128) {
            parse.write("7\n"); //rule 7
            compare(128);
            compare(110);
            int line_I = lexical.line; String I_type = I();
            compare(109);
            int line_J = lexical.line; String J_type = J();
            compare(109);
            int line_U = lexical.line; String U_type = U();
            compare(111);
            compare(113);
            String N_type = N();
            compare(114);

            if(I_type.equals("ok")) {
                if(J_type.equals("boolean")) {
                    if(U_type.equals("int")) {
                        if(N_type.equals("ok"))
                            B_type = "ok";
                        else {
                            B_type = "error";
                            System.out.println("Semantic error inside N, in rule 7 (B -> for(I;J;U){N}).");
                        }
                    } else {
                        B_type = "error";
                        System.out.println("Semantic error inside U, in rule 7 (B -> for(I;J;U){N}) in line " + line_U + ".");
                    }
                } else {
                    B_type = "error";
                    System.out.println("Semantic error inside J, in rule 7 (B -> for(I;J;U){N}) in line " + line_J + ".");
                }
            } else {
                B_type = "error";
                System.out.println("Semantic error inside I, in rule 7 (B -> for(I;J;U){N}) in line " + line_I + ".");
            }
        } else
            error.syntaxError(token, lexical.line);
        return B_type;
    }

    //S function
    private String[] S() throws IOException {
        String[] S_attributes = new String[2]; //S_attributes[0] = S_type, S_attributes[1] = S_returnType (because of S -> return X; rule)
        if(token.code == 130) {
            parse.write("8\n"); //rule 8
            String varName = compare(130);
            int line_S1 = lexical.line; String S1_type = S1(varName);

            if(inFunction) {
                if(varExists(localTable, varName)) {
                    if(searchElement(localTable, varName).type.equals(S1_type) && !S1_type.equals("error")) {
                        S_attributes[0] = "ok";
                        S_attributes[1] = null;
                    } else {
                        S_attributes[0] = "error";
                        S_attributes[1] = null;
                        System.out.println("Semantic error in rule 8 (S -> id S1) in line " + line_S1 + ".");
                    }
                } else {
                    if(searchElement(symbolTable, varName).type.equals(S1_type) && !S1_type.equals("error")) {
                        S_attributes[0] = "ok";
                        S_attributes[1] = null;
                    } else {
                        S_attributes[0] = "error";
                        S_attributes[1] = null;
                        System.out.println("Semantic error in rule 8 (S -> id S1) in line " + line_S1 + ".");
                    }
                }
            } else {
                if(searchElement(symbolTable, varName).type.equals(S1_type) && !S1_type.equals("error")) {
                    S_attributes[0] = "ok";
                    S_attributes[1] = null;
                } else {
                    S_attributes[0] = "error";
                    S_attributes[1] = null;
                    System.out.println("Semantic error in rule 8 (S -> id S1) in line " + line_S1 + ".");
                }
            }
        } else if(token.code == 123) {
            parse.write("9\n"); //rule 9
            compare(123);
            int line_E  = lexical.line; String E_type = E();
            compare(109);

            if(E_type.equals("int") || E_type.equals("string") || E_type.equals("boolean")) {
                S_attributes[0] = "ok";
                S_attributes[1] = null;
            } else {
                S_attributes[0] = "error";
                S_attributes[1] = null;
                System.out.println("Semantic error inside E in rule 9 (S -> print E ;) in line " + line_E + ". The expression to be printed is invalid!");
            }
        } else if(token.code == 124) {
            parse.write("10\n"); //rule 10
            compare(124);
            String varName = compare(130);
            compare(109);

            if(inFunction) {
                if(varExists(localTable, varName)) {
                    Element element = searchElement(localTable, varName); //find the element with this name
                    if(element.type.equals("int") || element.type.equals("string")) {
                        S_attributes[0] = "ok";
                        S_attributes[1] = null;
                    } else {
                        S_attributes[0] = "error";
                        S_attributes[1] = null;
                        System.out.println("Semantic error in rule 10 (S -> input id ;) in line " + lexical.line + ". Only integer and string are valid id types!");
                    }
                } else {
                    Element element = searchElement(symbolTable, varName); //find the element with this name
                    if(element.type.equals("int") || element.type.equals("string")) {
                        S_attributes[0] = "ok";
                        S_attributes[1] = null;
                    } else {
                        S_attributes[0] = "error";
                        S_attributes[1] = null;
                        System.out.println("Semantic error in rule 10 (S -> input id ;) in line " + lexical.line + ". Only integer and string are valid id types!");
                    }
                }
            } else {
                Element element = searchElement(symbolTable, varName); //find the element with this name
                if(element.type.equals("int") || element.type.equals("string")) {
                    S_attributes[0] = "ok";
                    S_attributes[1] = null;
                } else {
                    S_attributes[0] = "error";
                    S_attributes[1] = null;
                    System.out.println("Semantic error in rule 10 (S -> input id ;) in line " + lexical.line + ". Only integer and string are valid id types!");
                }
            }
        } else if(token.code == 125) {
            parse.write("11\n"); //rule 11
            compare(125);
            int line_X = lexical.line; String X_type = X();
            compare(109);

            if(X_type.equals("int") || X_type.equals("string") || X_type.equals("ok")) {
                S_attributes[0] = "ok";
                S_attributes[1] = X_type;
            } else {
                S_attributes[0] = "error";
                S_attributes[1] = null;
                System.out.println("Semantic error inside X in rule 11 (S -> return X ;) in line " + line_X + ".");
            }
        } else
            error.syntaxError(token, lexical.line);

        return S_attributes;
    }

    //F function
    private String F() throws IOException {
        parse.write("12\n"); //rule 12

        String F_type = null;
        localTable = new ArrayList<>(); //create symbol table for local variables
        localDespl = 0;

        compare(129);
        String functionName = compare(130);

        Element element = searchElement(symbolTable, functionName);
        element.paramTypes = new ArrayList<String>();

        String H_type = H();
        compare(110);
        String A_type = A(functionName);
        compare(111);
        compare(113);
        String[] C_attributes = C();
        compare(114);

        if(H_type.equals("int") || H_type.equals("string") || H_type.equals("ok")) {
            if(A_type.equals("ok")) {
                if(C_attributes[0].equals("ok")) {
                    if(H_type.equals("ok"))
                        element.returnType = "void";
                    else
                        element.returnType = H_type;

                    element.type = "function";

                    if(C_attributes[1] != null) {
                        if(C_attributes[1].equals(element.returnType) || (C_attributes[1].equals("ok") && element.returnType.equals("void")))
                            F_type = "ok";
                        else {
                            F_type = "error";
                            System.out.println("Semantic error - function return type of function " + functionName + " doesn't match with the value in the return statement!");
                        }
                    } else {
                        if(H_type.equals("ok"))
                            F_type = "ok";
                        else {
                            F_type = "error";
                            System.out.println("Semantic error - function with name " + functionName + " and return type " + element.returnType + " doesn't contain any return statements!");
                        }
                    }
                } else {
                    F_type = "error";
                    System.out.println("Semantic error inside C in rule 12 (F -> function id H(A) {C}) for function '" + functionName + "'.");
                }
            } else {
                F_type = "error";
                System.out.println("Semantic error inside A in rule 12 (F -> function id H(A) {C}) for function '" + functionName + "'.");
            }
        } else {
            F_type = "error";
            System.out.println("Semantic error inside H in rule 12 (F -> function id H(A) {C}) for function '" + functionName + "'.");
        }

        printTable(localTable);

        localTable = null;
        localDespl = 0;

        return F_type;
    }

    //E function
    private String E() throws IOException {
        parse.write("13\n"); //rule 13

        String E_type = null;
        String R_type =  R();
        String W_type = W();

        if(R_type.equals("boolean"))
            E_type = "boolean";
        else if(R_type.equals("int") || R_type.equals("string")) {
            if(W_type.equals("ok"))
                E_type = R_type;
            else
                E_type = "error";
        } else
            E_type = "error";

        return E_type;
    }

    //B1 function
    private String B1() throws IOException {
        String B1_type = null;
        if(firstS(token)) {
            parse.write("14\n"); //rule 14

            String[] S_attributes = S();
            String S_type = S_attributes[0];

            if(S_type.equals("ok"))
                B1_type = "ok";
            else
                B1_type = "error";
        } else if(token.code == 113) {
            parse.write("15\n"); //rule 15
            compare(113);
            String N_type = N();
            compare(114);
            String B2_type = B2();

            if(N_type.equals("ok")) {
                if(B2_type.equals("ok"))
                    B1_type = "ok";
                else
                    B1_type = "error";
            } else
                B1_type = "error";
        } else
            error.syntaxError(token, lexical.line);

        return B1_type;
    }

    //T function
    private String[] T() throws IOException {
        String[] T_attributes = new String[2]; //T_attributes[0] = T_type, T_attributes[1] = T_size
        if(token.code == 121) {
            parse.write("16\n"); //rule 16
            compare(121);

            T_attributes[0] = "int";
            T_attributes[1] = "16"; //integers occupy 16 bits
        } else if(token.code == 122) {
            parse.write("17\n"); //rule 17
            compare(122);

            T_attributes[0] = "string";
            T_attributes[1] = "1024"; //strings occupy 1024 bits
        } else
            error.syntaxError(token, lexical.line);

        return T_attributes;
    }

    //I function
    private String I() throws IOException {
        String I_type = null;
        if(token.code == 130) {
            parse.write("18\n"); //rule 18
            String varName = compare(130);
            compare(105);
            String U_type = U();

            if(inFunction) {
                if(varExists(localTable, varName)) {
                    if(searchElement(localTable, varName).type.equals(U_type) && U_type.equals("int"))
                        I_type = "ok";
                    else {
                        I_type = "error";
                        System.out.println("Semantic error in rule 18 (I -> id=U) in line " + lexical.line + ". The types of id and U need to be the same and equal to int.");
                    }
                } else {
                    if(searchElement(symbolTable, varName).type.equals(U_type) && U_type.equals("int"))
                        I_type = "ok";
                    else {
                        I_type = "error";
                        System.out.println("Semantic error in rule 18 (I -> id=U) in line " + lexical.line + ". The types of id and U need to be the same and equal to int.");
                    }
                }
            } else {
                if(searchElement(symbolTable, varName).type.equals(U_type) && U_type.equals("int"))
                    I_type = "ok";
                else {
                    I_type = "error";
                    System.out.println("Semantic error in rule 18 (I -> id=U) in line " + lexical.line + ". The types of id and U need to be the same and equal to int.");
                }
            }
        } else if(followX_I_J_J1(token)) {
            parse.write("19\n"); //rule 19
            I_type = "ok";
        } else
            error.syntaxError(token, lexical.line);

        return I_type;
    }

    //J function
    private String J() throws IOException {
        parse.write("20\n"); //rule 20
        String J_type = null;

        String varName = compare(130);
        int line_J1 = lexical.line; String J1_type = J1();

        if(inFunction) {
            if(varExists(localTable, varName)) {
                if(searchElement(localTable, varName).type.equals(J1_type))
                    J_type = "boolean";
                else {
                    J_type = "error";
                    System.out.println("Semantic error inside J1 in rule 20 (J -> id J1) in line " + line_J1 + ".");
                }
            } else {
                if(searchElement(symbolTable, varName).type.equals(J1_type))
                    J_type = "boolean";
                else {
                    J_type = "error";
                    System.out.println("Semantic error inside J1 in rule 20 (J -> id J1) in line " + line_J1 + ".");
                }
            }
        } else {
            if(searchElement(symbolTable, varName).type.equals(J1_type))
                J_type = "boolean";
            else {
                J_type = "error";
                System.out.println("Semantic error inside J1 in rule 20 (J -> id J1) in line " + line_J1 + ".");
            }
        }
        return J_type;
    }

    //U function
    private String U() throws IOException {
        parse.write("21\n"); //rule 21
        String U_type = null;

        String V_type = V();
        String Y_type = Y();

        if(V_type.equals("int"))
            U_type = "int";
        else if(V_type.equals("string") || V_type.equals("boolean") || V_type.equals("void")) {
            if(Y_type.equals("ok"))
                U_type = V_type;
            else
                U_type = "error";
        } else
            U_type = "error";

        return U_type;
    }

    //N function
    private String N() throws IOException {
        String N_type = null;
        if(firstS(token)) {
            parse.write("22\n"); //rule 22

            String[] S_attributes = S();
            String S_type = S_attributes[0];

            N_type = S_type;
        } else if(firstB(token)) {
            parse.write("23\n"); //rule 23
            String B_type = B();

            N_type = B_type;
        } else
            error.syntaxError(token, lexical.line);

        return N_type;
    }

    //S1 function
    private String S1(String varName) throws IOException {
        String S1_type = null;
        if(token.code == 105) {
            parse.write("24\n"); //rule 24
            compare(105);
            String E_type = E();
            compare(109);

            if(inFunction) {
                if(varExists(localTable, varName)) {
                    if(!searchElement(localTable, varName).type.equals("function"))
                        S1_type = E_type;
                    else {
                        S1_type = "error";
                        System.out.println("Semantic error between rules 8 and 24 ((S -> id S1) and (S1 -> = E;)) in line " + lexical.line + ". You cannot assign a value in a variable of function type!");
                    }
                } else {
                    if(!searchElement(symbolTable, varName).type.equals("function"))
                        S1_type = E_type;
                    else {
                        S1_type = "error";
                        System.out.println("Semantic error between rules 8 and 24 ((S -> id S1) and (S1 -> = E;)) in line " + lexical.line + ". You cannot assign a value in a variable of function type!");
                    }
                }
            } else {
                if(!searchElement(symbolTable, varName).type.equals("function"))
                    S1_type = E_type;
                else {
                    S1_type = "error";
                    System.out.println("Semantic error between rules 8 and 24 ((S -> id S1) and (S1 -> = E;)) in line " + lexical.line + ". You cannot assign a value in a variable of function type!");
                }
            }
        } else if(token.code == 110) {
            parse.write("25\n"); //rule 25
            compare(110);
            String L_type = L(varName);
            compare(111);
            compare(109);

            if(inFunction) {
                if(searchElement(localTable, varName).type.equals("function")) {
                    if(L_type.equals("ok"))
                        S1_type = "function";
                    else {
                        S1_type = "error";
                        System.out.println("Semantic error inside L in rule 25 (S1 -> (L);). The parameters entered in the function call probably don't match with the declared attributes of function " + varName + ".");
                    }
                } else {
                    S1_type = "error";
                    System.out.println("Semantic error between rules 8 and 25 ((S -> id S1) and (S1 -> (L);). There is no function with the name " + varName + ".");
                }
            } else {
                if(searchElement(symbolTable, varName).type.equals("function")) {
                    if(L_type.equals("ok"))
                        S1_type = "function";
                    else {
                        S1_type = "error";
                        System.out.println("Semantic error inside L in rule 25 (S1 -> (L);). The parameters entered in the function call probably don't match with the declared attributes of function " + varName + ".");
                    }
                } else {
                    S1_type = "error";
                    System.out.println("Semantic error between rules 8 and 25 ((S -> id S1) and (S1 -> (L);). There is no function with the name " + varName + ".");
                }
            }
        } else
            error.syntaxError(token, lexical.line);

        return S1_type;
    }

    //X function
    private String X() throws IOException {
        String X_type = null;
        if(firstE_R_U_V(token)) {
            parse.write("26\n"); //rule 26
            String E_type = E();

            if(E_type.equals("int") || E_type.equals("string"))
                X_type = E_type;
            else
                X_type = "error";
        } else if(followX_I_J_J1(token)) {
            parse.write("27\n"); //rule 27
            X_type = "ok";
        } else
            error.syntaxError(token, lexical.line);

        return X_type;
    }

    //H function
    private String H() throws IOException {
        String H_type = null;
        if(firstT(token)) {
            parse.write("28\n"); //rule 28

            String[] T_attributes = T();
            String T_type = T_attributes[0];

            H_type = T_type;
        } else if(followH(token)) {
            parse.write("29\n"); //rule 29
            H_type = "ok";
        } else
            error.syntaxError(token, lexical.line);

        return H_type;
    }

    //A function
    private String A(String functionName) throws IOException {
        String A_type = null;
        if(firstT(token)) {
            parse.write("30\n"); //rule 30

            int line_T = lexical.line; String[] T_attributes = T();
            String T_type = T_attributes[0];

            compare(130);
            int line_K = lexical.line; String K_type = K(functionName);

            if(T_type.equals("int") || T_type.equals("string")) {
                if(K_type.equals("ok")) {
                    Element function = searchElement(symbolTable, functionName);
                    function.paramTypes.add(T_type); //add parameter type for this function on the symbol table
                    function.paramQuantity++;
                    A_type = "ok";
                } else {
                    A_type = "error";
                    System.out.println("Semantic error inside K in rule 30 (A -> T id K) in line " + line_K + ".");
                }
            } else {
                A_type = "error";
                System.out.println("Semantic error inside T in rule 30 (A -> T id K) in line " + line_T + ". Only int and string are valid argument types!");
            }
        } else if(followL_Q_A_K(token)) {
            parse.write("31\n"); //rule 31
            A_type = "ok";
        } else
            error.syntaxError(token, lexical.line);

        return A_type;
    }

    //C function
    private String[] C() throws IOException {
        String[] C_attributes = new String[2]; //C_attributes[0] = C_type, C_attributes[1] = C_returnType (holds the type of the return value in the body of the function)
        if(firstB(token)) {
            parse.write("32\n"); //rule 32
            String B_type = B();
            String[] inner_C_attributes = C();

            if(B_type.equals("ok")) {
                if(inner_C_attributes[0].equals("ok")) {
                    C_attributes[0] = "ok";
                    C_attributes[1] = inner_C_attributes[1];
                } else {
                    C_attributes[0] = "error";
                    C_attributes[1] = null;
                }
            } else {
                C_attributes[0] = "error";
                C_attributes[1] = null;
            }
        } else if(firstS(token)) {
            parse.write("33\n"); //rule 33
            String[] S_attributes = S();
            String[] inner_C_attributes = C();

            if(S_attributes[0].equals("ok")) {
                if(inner_C_attributes[0].equals("ok")) {
                    if(S_attributes[1] == null) {
                        C_attributes[0] = "ok";
                        C_attributes[1] = inner_C_attributes[1];
                    } else {
                        C_attributes[0] = "ok";
                        C_attributes[1] = S_attributes[1];
                    }
                } else {
                    C_attributes[0] = "error";
                    C_attributes[1] = null;
                }
            } else {
                C_attributes[0] = "error";
                C_attributes[1] = null;
            }
        } else if(followN_C(token)) {
            parse.write("34\n"); //rule 34
            C_attributes[0] = "ok";
            C_attributes[1] = null;
        } else
            error.syntaxError(token, lexical.line);

        return C_attributes;
    }

    //R function
    private String R() throws IOException {
        String R_type = null;
        parse.write("35\n"); //rule 35
        String U_type = U();
        String Z_type = Z();

        if(Z_type.equals("ok"))
            R_type = U_type;
        else if(Z_type.equals("int") || Z_type.equals("string") || Z_type.equals("boolean")) {
             if(Z_type.equals(U_type))
                 R_type = "boolean";
             else {
                 R_type = "error";
                 System.out.println("Semantic error in rule 35 (R -> U Z). When using rules 52 or 53 to replace Z (Z -> == U or Z -> != U)" +
                         " the values in each side of the relation operator need to be of the same type!");
             }
        } else
             R_type = "error";

        return R_type;
    }

    //W function
    private String W() throws IOException {
        String W_type = null;
        if(token.code == 107) {
            parse.write("36\n"); //rule 36
            compare(107);
            int line_R = lexical.line; String R_type = R();

            if(R_type.equals("boolean"))
                W_type = "boolean";
            else {
                W_type = "error";
                System.out.println("Semantic error in rule 36 (W -> &&R) in line " + line_R + ". The values in each side of the logical operator need to be boolean!");
            }
        } else if(followE_W(token)) {
            parse.write("37\n"); //rule 37
            W_type = "ok";
        } else
            error.syntaxError(token, lexical.line);

        return W_type;
    }

    //B2 function
    private String B2() throws IOException {
        String B2_type = null;
        if(token.code == 127) {
            parse.write("38\n"); //rule 38
            compare(127);
            compare(113);
            String N_type = N();
            compare(114);

            if(N_type.equals("ok"))
                B2_type = "ok";
            else
                B2_type = "error";

        } else if(followS_S1_B_B1_B2(token)) {
            parse.write("39\n"); //rule 39
            B2_type = "ok";
        } else
            error.syntaxError(token, lexical.line);

        return B2_type;
    }

    //J1 fuction
    private String J1() throws IOException {
        String J1_type = null;
        if(token.code == 104) {
            parse.write("40\n"); //rule 40
            compare(104);
            String U_type = U();

            J1_type = U_type;
        } else if(token.code == 106) {
            parse.write("41\n"); //rule 41
            compare(106);
            String U_type = U();

            J1_type = U_type;
        } else
            error.syntaxError(token, lexical.line);

        return J1_type;
    }

    //V function
    private String V() throws IOException {
        String V_type = null;
        if(token.code == 130) {
            parse.write("42\n"); //rule 42
            String varName = compare(130);
            int line_V1 = lexical.line; String V1_type = V1(varName);

            if(inFunction) {
                if(varExists(localTable, varName)) {
                    Element element = searchElement(localTable, varName);
                    if(V1_type.equals("ok")) {
                        if(element.type.equals("function"))
                            V_type = element.returnType;
                        else
                            V_type = element.type;
                    } else {
                        V_type = "error";
                        System.out.println("Semantic error inside V1 in rule 42 (V -> id V1) in line " + line_V1 + ".");
                    }
                } else {
                    Element element = searchElement(symbolTable, varName);
                    if(V1_type.equals("ok")) {
                        if(element.type.equals("function"))
                            V_type = element.returnType;
                        else
                            V_type = element.type;
                    } else {
                        V_type = "error";
                        System.out.println("Semantic error inside V1 in rule 42 (V -> id V1) in line " + line_V1 + ".");
                    }
                }
            } else {
                Element element = searchElement(symbolTable, varName);
                if(V1_type.equals("ok")) {
                    if(element.type.equals("function"))
                        V_type = element.returnType;
                    else
                        V_type = element.type;
                } else {
                    V_type = "error";
                    System.out.println("Semantic error inside V1 in rule 42 (V -> id V1) in line " + line_V1 + ".");
                }
            }
        } else if(token.code == 110) {
            parse.write("43\n"); //rule 43
            compare(110);
            String E_type = E();
            compare(111);

            V_type = E_type;
        } else if(token.code == 100) {
            parse.write("44\n"); //rule 44
            compare(100);

            V_type = "int";
        } else if(token.code == 101) {
            parse.write("45\n"); //rule 45
            compare(101);

            V_type = "string";
        } else
            error.syntaxError(token, lexical.line);

        return V_type;
    }

    //Y function
    private String Y() throws IOException {
        String Y_type = null;
        if(token.code == 103) {
            parse.write("46\n"); //rule 46
            compare(103);
            int line_V = lexical.line; String V_type = V();

            if(V_type.equals("int"))
                Y_type = "ok";
            else {
                Y_type = "error";
                System.out.println("Semantic error inside V rule 46 (Y -> +V) in line " + line_V + ". V needs to hold an integer value!");
            }
        } else if(followU_Y(token)) {
            parse.write("47\n"); //rule 47
            Y_type = "ok";
        } else
            error.syntaxError(token, lexical.line);

        return Y_type;
    }

    //L function
    private String L(String varName) throws IOException {
        String L_type = null;
        if(firstE_R_U_V(token)) {
            parse.write("48\n"); //rule 48
            Element element = searchElement(symbolTable, varName);
            index = 0; //argument types are stored from the last to the first

            String E_type = E();
            String Q_type = Q(varName);

            if(!(index == element.paramQuantity-1)) {
                L_type = "error";
                System.out.println("Semantic error in rule 48 (L -> E Q). The number of parameters used in the function call of function " + varName + " doesn't match with number of arguments in its declaration!");
                return L_type;
            }

            if(E_type.equals(element.paramTypes.get(index))) {
                if(Q_type.equals("ok"))
                    L_type = "ok";
                else {
                    L_type = "error";
                    System.out.println("Semantic error inside Q in rule 48 (L -> E Q). One of the parameters in the function call of function "+ varName + " is probably wrong!");
                }
            } else {
                L_type = "error";
                System.out.println("Semantic error in rule 48 (L -> E Q). One of the parameters in the function call of function " + varName + " is probably wrong!");
            }
        } else if(followL_Q_A_K(token)) {
            parse.write("49\n"); //rule 49

            int paramQuantity = searchElement(symbolTable, varName).paramQuantity;
            if(paramQuantity == 0)
                L_type = "ok";
            else {
                L_type = "error";
                System.out.println("Semantic error in rule 49 (L -> λ). Invalid function call of function" + varName + " because it should contain " + paramQuantity + "parameters!");
            }
        } else
            error.syntaxError(token, lexical.line);

        return L_type;
    }

    //K function
    private String K(String varName) throws IOException {
        String K_type = null;
        if(token.code == 112) {
            parse.write("50\n"); //rule 50
            compare(112);

            int line_T = lexical.line; String[] T_attributes = T();
            String T_type = T_attributes[0];

            compare(130);
            int line_K = lexical.line; String inner_K_type = K(varName);

            Element function = searchElement(symbolTable, varName);
            if(T_type.equals("int") || T_type.equals("string")) {
                if(inner_K_type.equals("ok")) {
                    function.paramTypes.add(T_type);
                    function.paramQuantity++;
                    K_type = "ok";
                } else {
                    K_type = "error";
                    System.out.println("Semantic error inside K in rule 50 (K -> ,T id K) in line " + line_K + ".");
                }
            } else {
                K_type = "error";
                System.out.println("Semantic error inside T in rule 50 (K -> ,T id K) in line " + line_T + ". Only int and string are valid argument types!");
            }
        } else if(followL_Q_A_K(token)) {
            parse.write("51\n"); //rule 51
            K_type = "ok";
        } else
            error.syntaxError(token, lexical.line);

        return K_type;
    }

    //Z function
    private String Z() throws IOException {
        String Z_type = null;
        if(token.code == 104) {
            parse.write("52\n"); //rule 52
            compare(104);
            int line_U = lexical.line; String U_type = U();

            if(U_type.equals("int") || U_type.equals("string") || U_type.equals("boolean"))
                Z_type = U_type;
            else {
                Z_type = "error";
                System.out.println("Semantic error inside U in rule 52 (Z -> == U) in line " + line_U + ". U needs to be of int, string or boolean type!");
            }
        } else if(token.code == 106) {
            parse.write("53\n"); //rule 53
            compare(106);
            int line_U = lexical.line; String U_type = U();

            if(U_type.equals("int") || U_type.equals("string") || U_type.equals("boolean"))
                Z_type = U_type;
            else {
                Z_type = "error";
                System.out.println("Semantic error inside U in rule 52 (Z -> != U) in line " + line_U + ". U needs to be of int, string or boolean type!");
            }
        } else if(followR_Z(token)) {
            parse.write("54\n"); //rule 54
            Z_type = "ok";
        } else
            error.syntaxError(token, lexical.line);

        return Z_type;
    }

    //V1 function
    private String V1(String varName) throws IOException {
        String V1_type = null;
        if(token.code == 102) {
            parse.write("55\n"); //rule 55
            compare(102);

            if(inFunction) {
                if(searchElement(localTable, varName).type.equals("int"))
                    V1_type = "ok";
                else {
                    V1_type = "error";
                    System.out.println("Semantic error in rule 55 (V1 -> ++) in line " + lexical.line + ". Increasing operator can only be applied on integers!");
                }
            } else {
                if(searchElement(symbolTable, varName).type.equals("int"))
                    V1_type = "ok";
                else {
                    V1_type = "error";
                    System.out.println("Semantic error in rule 55 (V1 -> ++) in line " + lexical.line + ". Increasing operator can only be applied on integers!");
                }
            }
        } else if(token.code == 110) {
            parse.write("56\n"); //rule 56
            compare(110);

            String L_type = null;
            if(varExists(symbolTable, varName) && searchElement(symbolTable, varName).type.equals("function"))
                L_type = L(varName);
            else {
                System.out.println("Semantic error - there is no function with the name " + varName + "!");
                System.exit(0);
            }

            compare(111);

            if(searchElement(symbolTable, varName).type.equals("function")) {
                if(L_type.equals("ok"))
                    V1_type = "ok";
                else {
                    V1_type = "error";
                    System.out.println("Semantic error inside L in rule 56 (V1 -> (L)). The parameters in the function of function " + varName + " call don't match the arguments in the function declaration!");
                }
            } else {
                V1_type = "error";
                System.out.println("Semantic error in rule 56 (V1 -> (L)). There is no function with the name " + varName + "!");
            }
        } else if(followV_V1(token)) {
            parse.write("57\n"); //rule 57

            if(!searchElement(symbolTable, varName).type.equals("function"))
                V1_type = "ok";
            else {
                V1_type = "error";
                System.out.println("Semantic error in rule 57 (V1 -> λ). Identifier " + varName + " belongs to a function and therefore the syntax is invalid!");
            }
        } else
            error.syntaxError(token, lexical.line);

        return V1_type;
    }

    //Q function
    private String Q(String varName) throws IOException {
        String Q_type = null;
        if(token.code == 112) {
            parse.write("58\n"); //rule 58
            compare(112);
            int line_E = lexical.line; String E_type = E();
            int line_Q = lexical.line; String inner_Q_type = Q(varName);

            if(searchElement(symbolTable, varName).paramTypes.get(index).equals(E_type)) {
                if(inner_Q_type.equals("ok"))
                    Q_type = "ok";
                else {
                    Q_type = "error";
                    System.out.println("Semantic error inside Q in rule 58 (Q -> ,E Q) in line " + line_Q + ".");
                }
            } else {
                Q_type = "error";
                System.out.println("Semantic error inside E in rule 58 (Q -> ,E Q) in line " + line_E + ". The parameter type doesn't match with the type of the argument in the function declaration of function " + varName + ".");
            }
            index++;
        } else if(followL_Q_A_K(token)) {
            parse.write("59\n"); //rule 59
            Q_type = "ok";
        } else
            error.syntaxError(token, lexical.line);

        return Q_type;
    }


    //-----COMPARE FUNCTION-----
    private String compare(int code) throws IOException {
        Token previousToken = token; //store the previous token in order to return its attribute

        if(token.code == code) {
            token = lexical.lexicalAnalysis(br); //ask for the next token

            if(token == null) //if there is a lexical error stop execution
                System.exit(0);

            if(token.code == 130) { //if token is a variable
                if(inFunction) { //if we are inside a function implementation
                    if(!varExists(localTable, token.attribute)) { //check if the variable name exists in the local table
                        if(declMode)
                            localTable.add(new Element(token.attribute, "int", localDespl, null, null, 0));
                        else {
                            if(!varExists(symbolTable, token.attribute)) {
                                symbolTable.add(new Element(token.attribute, "int", despl, null, null, 0));
                                despl += 16;
                            }
                        }
                    } else {
                        if(declMode)
                            skip = true;
                    }
                } else {
                    if(!varExists(symbolTable, token.attribute)) { //check if the variable name exists in the symbol table
                        if(declMode)
                            symbolTable.add(new Element(token.attribute, "int", despl, null, null, 0));
                        else {
                            symbolTable.add(new Element(token.attribute, "int", despl, null, null, 0));
                            despl += 16;
                        }
                    } else {
                        if(declMode)
                            skip = true;
                    }
                }
            }

            switch(token.code) {
                case 100: tokenToString = "integer"; break;
                case 101: tokenToString = "phrase"; break;
                case 102: tokenToString = "++"; break;
                case 103: tokenToString = "+"; break;
                case 104: tokenToString = "=="; break;
                case 105: tokenToString = "="; break;
                case 106: tokenToString = "!="; break;
                case 107: tokenToString = "&&"; break;
                //108 is occupied for keywords and variables
                case 109: tokenToString = ";"; break;
                case 110: tokenToString = "("; break;
                case 111: tokenToString = ")"; break;
                case 112: tokenToString = ","; break;
                case 113: tokenToString = "{"; break;
                case 114: tokenToString = "}"; break;

                case 120: tokenToString = "let"; break;
                case 121: tokenToString = "int"; break;
                case 122: tokenToString = "string"; break;
                case 123: tokenToString = "print"; break;
                case 124: tokenToString = "input"; break;
                case 125: tokenToString = "return"; break;
                case 126: tokenToString = "if"; break;
                case 127: tokenToString = "else"; break;
                case 128: tokenToString = "for"; break;
                case 129: tokenToString = "function"; break;
                case 130: tokenToString = "identifier"; break;
            }
            myWriter.write("<" + tokenToString + ", " + token.attribute + ">\n");
        } else
            error.syntaxError(previousToken, lexical.line);


        return previousToken.attribute;
    }


    //-----FIRSTS-----
    private boolean firstE_R_U_V(Token token) {
        return (token.code == 130 || token.code == 111 || token.code == 100 || token.code == 101);
    }

    private boolean firstW(Token token) {
        return (token.code == 107 || followE_W(token));
    }

    private boolean firstZ(Token token) {
        return (token.code == 104 || token.code == 106 || followR_Z(token));
    }

    private boolean firstY(Token token) {
        return (token.code == 103 || followU_Y(token));
    }

    private boolean firstV1(Token token) {
        return (token.code == 102 || token.code == 110 || followV_V1(token));
    }

    private boolean firstS(Token token) {
        return (token.code == 130 || token.code == 123 || token.code == 124 || token.code == 125);
    }

    private boolean firstS1(Token token) {
        return (token.code == 105 || token.code == 110);
    }

    private boolean firstL(Token token) {
        return (token.code == 130 || token.code == 110 || token.code == 100 || token.code == 101 || followL_Q_A_K(token));
    }

    private boolean firstQ_K(Token token) {
        return (token.code == 112 || followL_Q_A_K(token));
    }

    private boolean firstX(Token token) {
        return (token.code == 130 || token.code == 110 || token.code == 100 || token.code == 101 || followX_I_J_J1(token));
    }

    private boolean firstB(Token token) {
        return (token.code == 126 || token.code == 128 || token.code == 120);
    }

    private boolean firstB1(Token token) {
        return (token.code == 113 || token.code == 130 || token.code == 123 || token.code == 124 || token.code == 125);
    }

    private boolean firstN(Token token) {
        return (token.code == 130 || token.code == 123 || token.code == 124 || token.code == 125 || token.code == 126 || token.code == 128 || token.code == 120);
    }

    private boolean firstB2(Token token) {
        return (token.code == 127 || followS_S1_B_B1_B2(token));
    }

    private boolean firstT(Token token) {
        return (token.code == 121 || token.code == 122);
    }

    private boolean firstI(Token token) {
        return (token.code == 130 || followX_I_J_J1(token));
    }

    private boolean firstJ(Token token) {
        return (token.code == 130);
    }

    private boolean firstF(Token token) {
        return (token.code == 129);
    }

    private boolean firstH(Token token) {
        return (token.code == 121 || token.code == 122 || followH(token));
    }

    private boolean firstA(Token token) {
        return (token.code == 121 || token.code == 122 || followL_Q_A_K(token));
    }

    private boolean firstC(Token token) {
        return (token.code == 130 || token.code == 123 || token.code == 124 || token.code == 125 || token.code == 126 || token.code == 128 || token.code == 120 || followN_C(token));
    }

    private boolean firstP(Token token) {
        return (token.code == 130 || token.code == 123 || token.code == 124 || token.code == 125 || token.code == 126 || token.code == 128 || token.code == 120 || token.code == 129 || followP(token));
    }

    private boolean firstJ1(Token token) {
        return (token.code == 104 || token.code == 106);
    }


    //-----FOLLOWS-----
    private boolean followE_W(Token token) {
        return (token.code == 111 || token.code == 109 || token.code == 112);
    }

    private boolean followR_Z(Token token) {
        return (token.code == 107 || token.code == 111 || token.code == 109 || token.code == 112);
    }

    private boolean followU_Y(Token token) {
        return (token.code == 107 || token.code == 111 || token.code == 109 || token.code == 112 || token.code == 104 || token.code == 106);
    }

    private boolean followV_V1(Token token) {
        return (token.code == 107 || token.code == 111 || token.code == 109 || token.code == 112 || token.code == 104 || token.code == 106 || token.code == 103);
    }

    private boolean followS_S1_B_B1_B2(Token token) {
        return (token.code == 130 || token.code == 123 || token.code == 124 || token.code == 125 || token.code == 126 || token.code == 120 || token.code == 128 || token.code == 129 || token.code == 114 || token.code == 200);
    }

    private boolean followL_Q_A_K(Token token) {
        return (token.code == 111);
    }

    private boolean followX_I_J_J1(Token token) {
        return (token.code == 109);
    }

    private boolean followN_C(Token token) {
        return (token.code == 114);
    }

    private boolean followT(Token token) {
        return (token.code == 109 || token.code == 130 || token.code == 110);
    }

    private boolean followF(Token token) {
        return (token.code == 130 || token.code == 123 || token.code == 124 || token.code == 125 || token.code == 126 || token.code == 120 || token.code == 128 || token.code == 129 || token.code == 200);
    }

    private boolean followH(Token token) {
        return (token.code == 110);
    }

    private boolean followP(Token token) {
        return (token.code == 200);
    }



    //function that returns true if a specific variable name is on the table
    private boolean varExists(ArrayList<Element> table, String name) {
        for(int i=0; i<table.size(); i++) {
            if(table.get(i).name.equals(name))
                return true;
        }
        return false;
    }

    //function that returns the Element object that holds a specific name inside the table
    private Element searchElement(ArrayList<Element> table, String name) {
        for(int i=0; i<table.size(); i++) {
            if(table.get(i).name.equals(name))
                return table.get(i);
        }
        return null;
    }


    //function to print the data of the symbol tables
    private void printTable(ArrayList<Element> symbolTable) {
        if(inFunction) {
            System.out.println("\n\n\n               ----------LOCAL SYMBOL TABLE----------");
            System.out.println("           Name\t\t\tType\t\tPosition");
            for(int i=0; i<symbolTable.size(); i++)
                System.out.printf("%15s\t\t%8s\t\t%5d\n", symbolTable.get(i).name, symbolTable.get(i).type, symbolTable.get(i).despl);
        } else {
            System.out.println("\n\n\n               ----------GLOBAL SYMBOL TABLE----------");
            System.out.println("           Name\t\t\tType\t\tPosition\t\tReturn type\t\tParameter Quantity");
            for(int i=0; i<symbolTable.size(); i++) {
                if(!symbolTable.get(i).type.equals("function"))
                    symbolTable.get(i).returnType = "null";

                System.out.printf("%15s\t\t%8s\t\t%5d\t\t%13s\t\t%8s\n", symbolTable.get(i).name, symbolTable.get(i).type, symbolTable.get(i).despl, symbolTable.get(i).returnType, symbolTable.get(i).paramQuantity);
            }
        }
        System.out.println("\n");
    }
}
