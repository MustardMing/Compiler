import java.util.ArrayList;
import java.util.Map;

public class Application {
    public static void main(String[] args){
        int num = 0;
        ArrayList<Depot> list;

        WordAnalysis compiler = new WordAnalysis();
        list = compiler.scanner();
        for (Depot depot : list) {
            System.out.println("('" + depot.key + "','" + depot.value + "')");
        }

        GrammarAnalysis grammarAnalysis = new GrammarAnalysis();
        grammarAnalysis.detach();

        grammarAnalysis.getFirst();
        System.out.println();
        System.out.println("FIRST集：");
        for (Map.Entry<String, ArrayList<String>> entry : grammarAnalysis.firstMap.entrySet()) {
            System.out.println(entry.getKey() + " ：" + entry.getValue());
        }

        grammarAnalysis.getFollow();
        System.out.println();
        System.out.println("FOLLOW集:");
        for (Map.Entry<String, ArrayList<String>> entry : grammarAnalysis.followMap.entrySet()) {
            System.out.println(entry.getKey() + " ：" + entry.getValue());
        }

        grammarAnalysis.getFirst();
        System.out.println();
        System.out.println("SELECT集:");
        grammarAnalysis.select();

        for (String str: compiler.sentence) {
            while (!compiler.ISBorder(str.charAt(str.length()-1))&&num<1){
                if(str.substring(str.length()-2).equals("//")){
                    while (!compiler.ISBorder(str.charAt(str.length()-1))){
                        str = str.substring(0,str.length()-1);
                    }
                    num++;
                } else str = str.substring(0,str.length()-1);
            }
            if(compiler.ISBorder(str.charAt(str.length()-1)))
                str = str.substring(0,str.length()-1);
            while (str.charAt(0) == 32){
                if (str.equals(" ")) break;
                str = str.substring(1);
            }
            if (str.equals(" ")) continue;
            grammarAnalysis.analysis(str);
            System.out.println();
        }
    }
}
