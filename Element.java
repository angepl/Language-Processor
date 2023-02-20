import java.util.ArrayList;

public class Element {
    public String name;
    public String type;
    public int despl;
    public String returnType;
    public ArrayList<String> paramTypes;
    public int paramQuantity;

    public Element(String name, String type, int despl, String returnType, ArrayList<String> paramTypes, int paramQuantity) {
        this.name = name;
        this.type = type;
        this.despl = despl;
        this.returnType = returnType;
        this.paramTypes = paramTypes;
        this.paramQuantity = paramQuantity;
    }
}
