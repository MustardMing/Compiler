import java.util.ArrayList;

public class Table {
    String vnToken;    //存储非终结符
    ArrayList<String> vtToken;  //存储终结符
    String m;           //存储推导式

    public Table(String vnToken, ArrayList<String> vtToken) {
        this.vnToken = vnToken;
        this.vtToken = vtToken;
    }

    public String getVnToken() {
        return vnToken;
    }

    public void setVnToken(String vnToken) {
        this.vnToken = vnToken;
    }

    public ArrayList<String> getVtToken() {
        return vtToken;
    }

    public void setVtToken(ArrayList<String> vtToken) {
        this.vtToken = vtToken;
    }

    public String getM() {
        return m;
    }

    public void setM(String m) {
        this.m = m;
    }

    @Override
    public String toString() {
        return vnToken + "->" + m + ":" + vtToken ;
    }
}
