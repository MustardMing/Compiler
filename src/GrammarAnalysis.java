import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GrammarAnalysis {
    int j = 2;              //计数器
    int ch;             //当前字符
    String init;        //初始字符
    String followC;     //所求FOLLOW的字符
    Table[] sym = new Table[50];  //Table类的数组，用于对SELECT集的储存
    ArrayList<String> had = new ArrayList<>();      //已经进行过FIRST或FOLLOW计算的非终结符所储存的数组
    ArrayList<String> vt = new ArrayList<>();       //所有终结符所储存的数组
    ArrayList<String> vn = new ArrayList<>();       //所有非终结符所储存的数组
    ArrayList<String> list = new ArrayList<>();     //求FOLLOW集所需变量
    ArrayList<String> listAll = new ArrayList<>();  //求FIRST集所需变量
    Map<String,ArrayList<String>> rsMap = new HashMap<>();      //用于存储非终极符及其相应推导式
    Map<String,ArrayList<String>> firstMap = new HashMap<>();   //储存FIRST集
    Map<String,ArrayList<String>> followMap = new HashMap<>();  //用于存储FOLLOW集

    /**
     * 判断是否为终结符
     * @param ch
     * @return
     */
    public boolean IsEnd(int ch){
        if ((ch >= 97) && (ch <= 122)) return true;
        if ((ch >= 33) && (ch <= 43)) return true;
        return false;
    }

    /**
     * 判断是否为非终结符
     * @param ch
     * @return
     */
    public boolean IsNotEnd(int ch){
        if ((ch >= 65) && (ch <= 90)) return true;
        return false;
    }

    /**
     * 计算该非终结符的FIRST集
     * @param symbol
     * @return
     */
    public ArrayList<String> first(String symbol){
        int num ;       //当前字母下标
        ArrayList<String> current;  //记录当前文法的first集
        ArrayList<String> rsList = rsMap.get(symbol);//获取结果集
        list = new ArrayList<>(); //记录当前非终结符的first集
        for(String line:rsList){
            num = 0;
            current = new ArrayList<>();
            while(num<line.length()) {
                ch = line.charAt(num);
                if (IsEnd(ch)) {        //若为终结符
                    if(current.indexOf(String.valueOf((char) ch))==-1) {
                        current.add(String.valueOf((char) ch)); //加入到current中
                    }
                    if(num!=0) {        //若终结符下标不为零，说明之前存在非终结符，消除'~'
                        while(listAll.indexOf("~")!=-1){
                            current.remove("~");
                            list.remove("~");
                            listAll.remove("~");
                        }
                    }
                    list.addAll(current);   //将current中的元素加入list中
                    break;
                }else if(IsNotEnd(ch)){  //若为非终结符
                    /*将准备将继续求first集的非终结符存入had数组，防止无限递归*/
                    if(had.indexOf(String.valueOf((char) ch))==-1&&IsNotEnd(ch)) {
                        had.add(String.valueOf((char) ch));
                        current = first(String.valueOf((char) ch));
                    }
                    list.addAll(current);
                    if (current.indexOf("~")==-1) break;    //若该非终结符的FIRST集中不存在空，则直接跳出
                    if (current.indexOf("~")!=-1) {         //若该非终结符的FIRST集中存在空，则继续查看下一字符
                        num++;
                    }
                }else if (ch == 126){       //若为空则加入
                    if(current.indexOf(String.valueOf((char) ch))==-1) current.add(String.valueOf((char) ch));
                    list.addAll(current);
                    break;
                }
            }
        }
        listAll.addAll(list);
        return list;
    }

    /**
     * 计算该终结符的FOLLOW集
     * @param symbol
     * @return
     */
    public ArrayList<String> follow(String symbol){
        String t = new String();
        ArrayList<String> l;
        ArrayList<String> rsList = new ArrayList<>();       //数组容器
        ArrayList<String> contents = new ArrayList<>();     //得到的需要处理的字符串
        ArrayList<String> toSave = new ArrayList<>();       //最终的数组
        for(String key : rsMap.keySet()){                   //结果集中的所有右边数组
            for(String str : rsMap.get(key)){               //数组中的所有字符串
                for(int i = 0;i<str.length();i++){          //将字符串的字符依次与所求字符比较
                    if(str.charAt(i) == symbol.charAt(0)){
                        t = t + key;                        //将该字符的->左边的字符加入要处理的数组中
                        if(str.length()>=i+2){              //如果该字符右边有终结符或非终结符，加入
                            t = t + str.substring(i+1,str.length()); //右边是非终结符直接加入
                            if(IsEnd(str.charAt(i + 1)))    //右边是非终结符直接取代t
                                t = String.valueOf(str.charAt(i + 1));
                        }
                        contents.add(t);                    //将每一个含有该字符的所求字符串加入contents数组中
                        t = new String();
                    }
                }
            }
        }

        for(String str : contents){
            if(str.length() == 1){          //所求字符右边是非终结符或右边为空的情况
                if(IsEnd(str.charAt(0))){   //对于终结符直接得到FOLLOW，跳出
                    rsList.add(str);
                    continue;
                }
                //若had数组中无->左边符号
                //求->左边符号的FOLLOW集并将该符号存入had数组中
                if(had.indexOf(String.valueOf(str.charAt(0)))==-1){
                    had.add(String.valueOf(str.charAt(0)));
                    rsList = follow(String.valueOf(str.charAt(0)));
                    for(String addition : rsList){
                        if (toSave.indexOf(addition)==-1) toSave.add(addition);
                    }
                }
            } else if(str.length() >= 2){                          //所求字符右边有非终结符情况
                l = firstMap.get(String.valueOf(str.charAt(1)));   //l为右边字符的FIRST集
                if (l!=null&&l.indexOf("~")==-1){                  //若FIRST不含'~'，则FIRST集为所求FOLLOW集
                    toSave = firstMap.get(String.valueOf(str.charAt(1)));
                }else if(l!=null&&l.indexOf("~")!=-1){//若FIRST含'~'，则与处理->左方字符的FOLLOW
                    for(String addition : l){
                        if (toSave.indexOf(addition)==-1) toSave.add(addition);
                    }
                    while (j<str.length()&&l.indexOf("~")!=-1) {
                        if(IsEnd(str.charAt(j))){
                            toSave.add(String.valueOf(str.charAt(j)));
                            break;
                        } else if(IsNotEnd(str.charAt(j))){
                            l = firstMap.get(String.valueOf(str.charAt(j)));
                            j++;
                        }
                    }
                    j--;
                    if(had.indexOf(String.valueOf(str.charAt(0)))==-1&&IsNotEnd(str.charAt(j))) {
                        had.add(String.valueOf(str.charAt(0)));
                        rsList = follow(String.valueOf(str.charAt(0)));
                        for(String addition : rsList){
                            if (toSave.indexOf(addition)==-1) toSave.add(addition);
                        }
                    }
                }
            }
        }
        //若所求的FOLLOW中存在初始字符，加入'#'
        if(had.indexOf(init)!=-1&&rsList.indexOf("#")==-1)
            rsList.add("#");
        if(symbol.equals(init))
            rsList.add("#");
        for(String addition : rsList){
            if (toSave.indexOf(addition)==-1) toSave.add(addition);
        }
        if(toSave.indexOf("~")!=-1) toSave.remove("~");
        return toSave;
    }

    /**
     *计算SELECT集
     */
    public void select(){
        int num = 0;
        String x = "Z";
        boolean signal;
        ArrayList<String> y ;

        for(int i = 0;i<sym.length;i++){
            sym[i] = new Table(null,null);
        }
        for(String letter:vn){
            for(String str : rsMap.get(letter)){
                sym[num].setVnToken(letter);
                y = new ArrayList<>();
                y.add(str);
                sym[num].setM(y.get(0));
                rsMap.put(x,y);

                listAll = new ArrayList<>();
                had.clear();
                first("Z");
                Set<String> ll = new HashSet<>();
                ll.addAll(listAll);
                ArrayList<String> l = new ArrayList<>();
                l.addAll(ll);
                if(l.size()==1&&l.indexOf("~")==0)
                    l = followMap.get(letter);
                else
                    l.remove("~");
                sym[num].setVtToken(l);
                System.out.println(sym[num].toString());
                num++;
            }
        }
        vt.add("#");
        vt.remove("~");
        System.out.println();
        System.out.println("预测分析表：");
        System.out.print("\t");
        for(String vt : vt){    //输出表格第一排的终结符
            System.out.print(vt + "\t\t");
        }
        System.out.println();
        for(String vn:vn) {     //输出表格中非终结符及其对应信息
            System.out.print(vn);
            for (String vt : vt) {
                signal = false;
                for (int i = 0; i < num; i++) {
                    if (sym[i].getVnToken().equals(vn) && sym[i].getVtToken().indexOf(vt) != -1){
                        System.out.print("\t" +vn + "->" + sym[i].m);
                        signal = true;
                    }
                }
                if(signal == false){
                    System.out.print("\t" + "error");
                }
            }
            System.out.println();
        }
        System.out.println();

    }

    /**
     * 计算分析过程
     */
    public void analysis(String sentence){
        int num = 1;
        String mes = sentence;
        String last = mes + "#";
        String stack = "#" + init;
        boolean signal = false;

        System.out.println("分析"+"\""+sentence+"\""+"过程");
        System.out.println("步骤"+"\t\t\t"+"分析栈"+"\t\t\t"+"剩余输入串"+"\t\t\t"+"推导所用产生式或匹配");
        while (!stack.equals("#")){
            System.out.print(num+"\t\t\t"+stack);
            if(stack.length()<4)
                System.out.print("\t");
            System.out.print("\t\t\t"+last);
            signal = false;
            for (int i = 0; i < sym.length;i++) {
                if (String.valueOf(stack.charAt(stack.length()-1)).equals(sym[i].getVnToken())
                        && sym[i].getVtToken().indexOf(String.valueOf(last.charAt(0))) != -1){
                    if(last.length()<4) System.out.print("\t");
                    System.out.print("\t\t\t\t"+String.valueOf(stack.charAt(stack.length()-1))
                            + "->" + sym[i].getM());
                    stack = stack.substring(0,stack.length()-1);
                    stack = stack + backString(sym[i].getM());
                    signal = true;
                    if(String.valueOf(stack.charAt(stack.length()-1)).equals("~")){
                        stack = stack.substring(0,stack.length()-1);
                    }
                    num++;
                    break;
                } else if(stack.charAt(stack.length()-1)==last.charAt(0)){
                    if(last.length()<4)
                        System.out.print("\t");
                    System.out.print("\t\t\t\t" + "\"" + last.charAt(0) + "\""+ "匹配");
                    last = last.substring(1,last.length());
                    stack = stack.substring(0,stack.length()-1);
                    signal = true;
                    num++;
                    break;
                }
            }
            if(signal==false){
                System.out.println();
                System.out.println(sentence + "不符合该文法!");
                break;
            }
            System.out.println();
        }
        if (signal==true){
            System.out.println(num+"\t\t\t"+"#"+"\t\t\t\t"+"#"+"\t\t\t\t\t"+"接受");
        }
    }

    /**
     * 获取FIRST集
     */
    public void getFirst(){
        for (String vn : vn) {
            had.clear();
            listAll = new ArrayList<>();
            first(vn);
            Set<String> ll = new HashSet<>();    /*消除重复元素*/
            ll.addAll(listAll);
            ArrayList<String> l = new ArrayList<>();
            l.addAll(ll);
            firstMap.put(vn,l);
        }
    }

    /**
     * 获取FOLLOW集
     */
    public void getFollow(){
        for (String vn : vn) {
            had.clear();
            followC = vn;
            ArrayList<String> l = follow(vn);
            followMap.put(vn,l);
        }
    }

    /**
     * 获取该字符串的逆序排列
     * @param x
     * @return 逆序的字符串
     */
    public String backString(String x){
        if(x.length()==1)
            return x;
        else
            return backString(x.substring(1)) + x.charAt(0);
    }

    /**
     * 将字符串中含有的终结符与非终结符分别放入对应的数组中
     * @param x
     */
    public void divide(String x){
        int c;
        int num = 0;
        String get;
        ArrayList<String> nul = new ArrayList<>();
        while (num < x.length()) {
            c = x.charAt(num);
            if (IsEnd(c)) {
                if (vt.indexOf(String.valueOf((char) c)) == -1) vt.add(String.valueOf((char) c));
            } else if (IsEnd(c)) {
                if (vt.indexOf(String.valueOf((char) c)) == -1) vt.add(String.valueOf((char) c));
            } else if (c == 126){
                if (vt.indexOf(String.valueOf((char) c)) == -1) vt.add(String.valueOf((char) c));
            }else if (IsNotEnd(c)) {
                if (vn.indexOf(String.valueOf((char) c)) == -1) {
                    vn.add(String.valueOf((char) c));
                    get = String.valueOf((char) c);
                    rsMap.put(get,nul);
                    firstMap.put(get,nul);
                    followMap.put(get,nul);
                }
            }
            num++;
        }
//        for(String xxx:vt ){
//            System.out.print(","+xxx);
//        }
//        System.out.println();
//        for(String xx:vn ){
//            System.out.print(","+xx);
//        }
    }

    /**
     * 对每行文法进行非终结符与相关文法的储存
     */
    public void detach(){
        init = "";          //初始化首个非终结界符
        String line;
        String current = "";  //当前行非终结符
        ArrayList<String> newline = new ArrayList<>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("grammar.txt"));
            while ((line = br.readLine()) != null) {    //按行读取文件，存入字符串中
                if(init.equals("")) init = String.valueOf(line.charAt(0)); //记录首个非终结符
                if(!current.equals("")&&!current.equals(String.valueOf(line.charAt(0)))){
                    rsMap.put(current,newline);   /*若当前非终结符改变，则将之前非终结符与文法储存*/
                    newline = new ArrayList<>();  //重新定义newline
                }
                divide(line);   //保存终结符与非终结符
                current = String.valueOf(line.charAt(0)); //记录当前非终结符
                newline.add(line.substring(3,line.length()));//
            }
            rsMap.put(current,newline);//储存最后一个终结符以及文法
            System.out.println("表达式");
            for (Map.Entry<String, ArrayList<String>> entry : rsMap.entrySet()) {
                System.out.println(entry.getKey() + " ：" + entry.getValue());
            }
        }catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}