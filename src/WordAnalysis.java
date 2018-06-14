import java.io.*;
import java.util.ArrayList;

/**
 * 1 关键字
 * 2 标识符
 * 3 常数
 * 4 运算符
 * 5 界符
 */

public class WordAnalysis {

    int ch;                 //当前字符
    int code;               //关键字状态码
    int num = 0;            //当前字符位置变量
    int line_num = 1;       //行数记录变量
    String line = null;     //存放每行内容的字符串
    boolean error = false;  //控制识别异常的信号变量
    Boolean signal = true;  //控制识别常量的信号变量

    StringBuffer strToken = new StringBuffer(); //存放当前构成单词符号的字符串
    ArrayList<Depot> list = new ArrayList<>();  //存放分词结果的动态数组
    ArrayList<String> note = new ArrayList<>(); //存放注释的动态数组
    ArrayList<String> tag = new ArrayList<>();  //存放标识符的动态数组
    ArrayList<String> sentence = new ArrayList<>();

    String[] retainWord = new String[]{"abstract","assert","boolean","break","byte","case",
            "catch","char","class","const","continue","default","do","double","else","enum",
            "extends","final","finally","float","for","if","implements","import","instanceof",
            "int","interface","long","native","new","package","private","protected","public",
            " return","short","static","strictfp","super","switch","synchronized","out","println",
            "this","throw","throws","transient","try","void","volatile","while","main","String","System"};//保留字

    //判断是否是空格、换行和TAB
    public boolean IfBreak(int ch){
        if(ch == 32||ch == 13||ch==9){
            return true;
        }
        return false;
    }

    //判断是否是字母
    public boolean IsLetter(){
        if((ch>=65 && ch <= 90) || (ch >= 97 && ch <=122)){
            return true;
        }
        return false;
    }

    //判断是否是数字
    public boolean IsDigit(){
        if(ch>=48 && ch <= 57){
            return true;
        }
        return false;
    }

    // 判断是否含有非单字符(!,<,=,>,&,|)
    public boolean IsNN(int ch) {
        if (ch == 33 || ch == 60 ||ch==61|| ch == 62 || ch == 38
                || ch == 124) {
            return true;
        }
        return false;
    }

    //判断是否为界符
    public boolean ISBorder(int ch){
        if((char) ch == '('||(char) ch == ')'||(char) ch == '['||(char) ch == ']'
                ||(char) ch == '{' ||(char) ch == '}'||(char) ch == ','||
                (char) ch == '.'||(char) ch == ':'||(char) ch == ';'){
            return true;
        }
        return false;
    }

    //连接字符
    public void Concat(char ch){
        strToken.append(ch);
    }

    //判断字符串属性
    public int Reserve(){
        for(int i = 0;i < retainWord.length;i++){
            if(strToken.toString().equals(retainWord[i])){
                return 1;
            }
        }
        if(strToken.length() != 0){
            if(strToken.charAt(0)>='0' && strToken.charAt(0)<='9'){
                return 3;
            } else if(strToken.charAt(strToken.length() - 1) == 34){
                list.add(new Depot(3,strToken.substring(0,strToken.length()-1)));
                list.add(new Depot(5,"\""));
                return 0;
            }
            else if(IsNN(strToken.charAt(0))&&strToken.length()<=2) {
                return 4;
            }
        } else if (strToken.length() == 0) {
            return 0;
        }
        return 2;
    }

    //按字符串所属种类进行标识并存储
    public void Retract(){
        if(signal==true) code = Reserve();
        if(code == 1){
            list.add(new Depot(1,strToken.toString()));
        }
        else if(code == 2){
                if(tag.indexOf(strToken.toString())!=-1) {
                    list.add(new Depot(2, String.valueOf(tag.indexOf(strToken.toString()))));
                }
                else {
                    tag.add(strToken.toString());
                    list.add(new Depot(2, String.valueOf(tag.indexOf(strToken.toString()))));
                }
        }
        else if(code == 3){
            list.add(new Depot(3,strToken.toString()));
        }
        else if(code == 4){
            list.add(new Depot(4,strToken.toString()));
        }
        strToken.delete(0, strToken.length());
    }

    //读取文件并进行操作
    public ArrayList<Depot> scanner(){
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("tests.txt"));
            while((line = br.readLine()) != null) {     //按行读取文件，存入字符串中
                while (num < line.length()){
                    ch = line.charAt(num);              //将字符串拆分为字符读取
                    //若字符为非法字符，将error变量置为true
                    if(IfBreak(ch)==false&&IsNN(ch)==false&&IsLetter()==false&&IsDigit()==false
                            &&ISBorder(ch)==false&&ch!='/'&&ch!='"'&&ch!='+'&&ch!='-'&&ch!='*'){
                        error = true;
                    }
                    //若注释信号变量为false且当前字符与下一位字符为'*/'，则将注释内容提取出来
                    if (signal == false&&line.charAt(num)=='*'&&line.charAt(num+1)=='/'){
                            signal = true;
                            note.add(new String(strToken.substring(0, strToken.length())));
                            strToken.delete(0, strToken.length());
                            num = num + 1;
                    }
                    //若常量信号变量为false且当前字符为'"'，则将常量内容提取出来
                    if(signal == false&&(char) ch != '"') {
                        Concat((char) ch);
                    }else if (IfBreak(ch) == false) {    /*首先判断是否为空格、换行和TAB*/
                        if (IsLetter() == true) {        /*字符若为字母，添加到strToken中*/
                            Concat((char) ch);
                        } else if (IsDigit() == true) {  /*字符若为数字，添加到strToken中*/
                            Concat((char) ch);
                        } else if (IsNN(ch) == true) {   /*字符若为非单字符，添加到strToken中*/
                            Concat((char) ch);
                        } else if (ch == 61) {           /*字符若为'='，添加到strToken中*/
                            Concat((char) ch);
                        } else if ((char) ch == '+') {
                            Retract();
                            list.add(new Depot(4, String.valueOf((char) ch)));
                        } else if ((char) ch == '-') {
                            Retract();
                            list.add(new Depot(4, String.valueOf((char) ch)));
                        } else if ((char) ch == '*'&& signal == true) {
                            Retract();
                            list.add(new Depot(4, String.valueOf((char) ch)));
                        } else if ((char) ch == '/') {   //对除法与注释之间的处理
                            if(num ==line.length()-1){
                                list.add(new Depot(0,"'/'异常"));
                            } else if(line.charAt(num+1) == '*'){
                                signal = false;
                                num = num+1;
                            } else if(line.charAt(num+1) == '/'){
                                note.add(new String(line.substring(num+2,line.length())));
                                num = line.length();
                            } else{
                                Retract();
                                list.add(new Depot(4, String.valueOf((char) ch)));
                            }
                        } else if(ISBorder(ch)){
                            Retract();
                            list.add(new Depot(5, String.valueOf((char) ch)));
                        } else if ((char) ch == '"') {      //对引号及其中常量的处理
                            if ((strToken.length() == 0)) {
                                list.add(new Depot(5, "\""));
                                signal = false;
                            } else {
                                Concat((char) ch);
                                signal = true;
                                Retract();
                            }
                        }
                    } else {
                        Retract();
                    }
                    num++;
                }
                if(error == false) sentence.add(line);
                if(error==true){
                    System.out.println("Error in Line " + line_num + " 含有非法字符");
                    error = false;
                }
                num = 0;
                line_num++;
            }
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for(String str : note){
            System.out.println("注释：" + str);
        }
        return list;
    }
}