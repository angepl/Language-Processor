public class Error {
    public boolean lexicalError(int e, char character, int line) {
        switch(e) {
            case 1: System.out.println("Lexical error - There is no expression starting with '" + character + "' (line " + line + ").");
                    break;
            case 2: System.out.println("Lexical error - There is no expression starting with '/" + character + "' (line " + line + ")." +
                    " If you want to start a comment type '/*comment*/'.");
                    break;
            case 3: System.out.println("Lexical error - There is no expression starting with '!" + character + "' (line " + line + ").");
                    break;
            case 4: System.out.println("Lexical error - There is no expression starting with '&" + character + "' (line " + line + ").");
                    break;
            case 5: System.out.println("Error - integers can only take a value inside the [0, 32767] domain (line " + line + ").");
                    break;
        }

        return true;
    }

    public void syntaxError(Token token, int line) {
        String tokenToString = null;
        switch(token.code) {
            case 100: tokenToString = token.attribute; break;
            case 101: tokenToString = token.attribute; break;
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
            case 130: tokenToString = token.attribute; break;
        }
        System.out.println("Syntax error - unexpected token '" + tokenToString + "' in line " + line + ".");
        System.exit(0);
    }
}
