import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class LexicalAnalyzer {
    Error error = new Error(); //Error object to handle errors
    char character;
    int state, value, numValue;
    String string;
    boolean flag = true;
    boolean errorFlag;
    int line = 1;
    public Token lexicalAnalysis(BufferedReader br) throws IOException {
        state = 0; //initial state
        value = 0; //variable to store the value of integers
        string = ""; //variable to store context of strings
        errorFlag = false;

        while(state < 100) { //until you reach a final state
            if(flag == true) { //check if next character has already been read in order to create a token
                numValue = br.read(); //read next character
                character = (char) numValue;

                if(character == '\n') //keep track of the line
                    line++;

                if(numValue == -1)
                    return new Token(200, null); //token to declare the end of text
            } else
                flag = true;

            //---TRANSITION MATRIX---
            switch (state) {
                case 0: if(isDelimiter(character))
                            break;
                        else if(character == '/')
                            state = 1;
                        else if(isDigit(character)) {
                                value = Character.getNumericValue(character);
                                state = 4;
                             }
                        else if(character == '"')
                            state = 5;
                        else if(character == '+')
                            state = 6;
                        else if(character == '=')
                            state = 7;
                        else if(character == '!')
                            state = 8;
                        else if(character == '&')
                            state = 9;
                        else if(isLetter(character) || character == '_') {
                                string = "" + character + "";
                                state = 10;
                             }
                        else if(character == ';') {
                            state = 109;
                            return new Token(109, null);
                        }
                        else if(character == '(') {
                            state = 110;
                            return new Token(110, null);
                        }
                        else if(character == ')') {
                            state = 111;
                            return new Token(111, null);
                        }
                        else if(character == ',') {
                            state = 112;
                            return new Token(112, null);
                        }
                        else if(character == '{') {
                            state = 113;
                            return new Token(113, null);
                        }
                        else if(character == '}') {
                            state = 114;
                            return new Token(114, null);
                        }
                        else
                            errorFlag = error.lexicalError(1, character, line);

                        break;
                case 1: if(character == '*')
                            state = 2;
                        else
                            errorFlag = error.lexicalError(2, character, line);
                        break;
                case 2: if(character == '*')
                            state = 3;
                        else
                            break;
                        break;
                case 3: if(character == '/')
                            state = 0;
                        else if(character == '*')
                            break;
                        else
                            state = 2;
                        break;
                case 4: if(isDigit(character)) {
                            value = 10*value + Character.getNumericValue(character);
                            break;
                        }
                        else {
                            state = 100;
                            flag = false;

                            if(value <= 32767)
                                return new Token(100, String.valueOf(value));
                            else
                                error.lexicalError(5, character, line);
                        }
                case 5: if(character == '"') {
                            state = 101;
                            return new Token(101, string);
                        }
                        else {
                            string = string + character;
                            break;
                        }
                case 6: if(character == '+') {
                            state = 102;
                            return new Token(102, null);
                        } else {
                            state = 103;
                            flag = false;
                            return new Token(103, null);
                        }
                case 7: if(character == '=') {
                            state = 104;
                            return new Token(104, null);
                        } else {
                            state = 105;
                            flag = false;
                            return new Token(105, null);
                        }
                case 8: if(character == '=') {
                            state = 106;
                            return new Token(106, null);
                        } else
                            errorFlag = error.lexicalError(3, character, line);
                        break;
                case 9: if(character == '&') {
                            state = 107;
                            return new Token(107, null);
                        } else
                            errorFlag = error.lexicalError(4, character, line);
                        break;
                case 10: if(isDigit(character) || isLetter(character) || character == '_') {
                            string = string + character;
                            break;
                         }
                         else {
                             state = 108;
                             flag = false;
                             switch(string) {
                                 case "let": return new Token(120, null);
                                 case "int": return new Token(121, null);
                                 case "string": return new Token(122, null);
                                 case "print": return new Token(123, null);
                                 case "input": return new Token(124, null);
                                 case "return": return new Token(125, null);
                                 case "if": return new Token(126, null);
                                 case "else": return new Token(127, null);
                                 case "for": return new Token(128, null);
                                 case "function": return new Token(129, null);
                                 default: return new Token(130, string);
                             }
                         }
            }
            //---END OF TRANSITION MATRIX---
            if(errorFlag == true)
                return null;
        }
        return new Token(0, null);
    }

    public boolean isDelimiter(char character) {
        return (character == ' ' || character == '\t' || character == '\n' || character == '\r');
    }

    public boolean isDigit(char character) {
        return (character >= '0' && character <= '9');
    }

    public boolean isLetter(char character) {
        return ((character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z'));
    }
}
