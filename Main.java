import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String args[]) throws IOException {
        File text = new File("Path_to_text_file"); //create file
        SyntacticAnalyzer synt = new SyntacticAnalyzer(); //create syntactic Analyzer object
        synt.analysis(text); //send file for analysis
    }
}
