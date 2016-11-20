package com.example.huangjing.timerecordtest20161108;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    //列表中显示的项目名称
    private String[] names = new String[]{
            "固定","吃饭","上班","睡觉","锻炼","写随笔","学安卓","看成长帖","学习"};
    //列表中显示的项目说明
    private String[] descs = new String[]{
            "固定就是必需做的事情，算是其它吧！",
            "吃饭包括买菜，做饭，吃水果，不包括饭局。",
            "上班包括正常八小时和加班",
            "睡觉包括晚上睡觉、午休和小憩。",
            "锻炼包括跑步、走路、keep、囚徒、爬楼梯和瑜珈等。",
            "写随笔指每天的千字文，包括查阅相关资料和思考的时间。",
            "学安卓指每天用AndroidStudio进行学习，包括看书和看网页。",
            "看成长帖指刷群论坛和搜索阅读过程中碰到的不懂的问题。",
            "学习包括但不限于看书，看文章。判断是否学习的原则在于是否有笔记输出。没有则只能算固定。"};
    //开始图标和结束图标
    private int images[] =new int[]{R.drawable.startbuttonpic,R.drawable.endbuttonpic};
    //设置列表中初始显示的图标都是开始图标
    private int[] imageIds = new int[]{
                images[0], images[0], images[0],
                images[0],images[0],images[0],
                images[0],images[0],images[0]};;
    //两种状态对应的字条串
    private String[] startOrEnd = new String[]{"start","end"};
    //用于显示的列表
    private List<Map<String,Object>> listItems;
    //提供给ListView的SimpleAdapter
    private SimpleAdapter simpleAdapter;
    //保存记录的文件名称
    final private String FILE_NAME="timeRecordTest20161113.bin";
    //用于保存中间数据的SharedPreferences
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    //用来保存项目信息。-1表示没有项目在进行中，正数表示正在进行的项目序号，从0开始
    private int countNo ;
    //用来保存项目的备注信息
    private String remarks;
    //
    private View dialogView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取isEnd的状态
        preferences = getPreferences(MODE_PRIVATE);
        countNo = preferences.getInt("countNo",-1);
        remarks = preferences.getString("remarks",null);

        listItems = new ArrayList<Map<String,Object>>();
        for(int i = 0; i< names.length;i++){
            Map<String,Object> listItem = new HashMap<String,Object>();
            listItem.put("irons",imageIds[i]);
            listItem.put("whatToDo",names[i]);
            listItem.put("desc",descs[i]);
            listItems.add(listItem);
        }
        simpleAdapter = new SimpleAdapter(MainActivity.this, listItems,
                R.layout.simple_item,new String[]{"irons","whatToDo","desc"},
                new int[]{R.id.header,R.id.name,R.id.desc});
        ListView list = (ListView)findViewById(R.id.mainList);
        list.setAdapter(simpleAdapter);
        list.setOnItemClickListener(new mainListOnItemClickListener());
        list.setOnItemLongClickListener(new mainListOnItemLongClickListener());
        Button read = (Button)findViewById(R.id.read);
        read.setOnClickListener(new MyReadOnClickListener());
        //对列表的状态初始化
        if(countNo!=-1){
            changeImage(countNo);
        }

        Button calWritingTime = (Button)findViewById(R.id.calWritingTime);
        calWritingTime.setOnClickListener(new MyCalWritingTimeOnClickListenre());
        }

    //往文件中写入内容
    public void write(String textIn){
        try{
            FileOutputStream fos = openFileOutput(FILE_NAME,MODE_APPEND);
            PrintStream ps = new PrintStream(fos);
            ps.println(textIn);
            ps.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    //读取记录文件内容
    public String read(){
        try{
            FileInputStream fis = openFileInput(FILE_NAME);
            int hasRead = 0;
            StringBuilder sb = new StringBuilder("");
            byte[] buff = new byte[1024];
            while((hasRead=fis.read(buff))>0){
                sb.append(new String(buff,0,hasRead));
            }
            fis.close();
            return sb.toString();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    //调试用的输出中间变量的方法
    public void print(String str){
        System.out.println(str);
        TextView show = (TextView)findViewById(R.id.show);
        show.setText(str);
    }
    public void print(int i){
        System.out.println(i);
        TextView show = (TextView)findViewById(R.id.show);
        show.setText(i);
    }
    public void print(long i){
        System.out.println(i);
        TextView show = (TextView)findViewById(R.id.show);
        show.setText(""+i);
    }
    //单击列表的监听器
    class mainListOnItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position,long id){
            if(countNo==-1 ){
                startNew(position);
            }else{
                if(countNo==position){
                    endOld();
                }else{
                    endOld();
                    startNew(position);
                }
            }
        }
    }
    //长按列表的监听器
    class mainListOnItemLongClickListener implements AdapterView.OnItemLongClickListener{
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position,long id){
            final EditText inputText = new EditText(MainActivity.this);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle((countNo==-1?"没有正在进行的项目。\n请单击来开始一个项目。\n":"")+((remarks==null)?"请输入备注：":"请修改备注："))
                    .setView(inputText)
                    .setPositiveButton("确定",new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            print("countNo"+countNo);
                            if(countNo!=-1){
                                remarks=inputText.getText().toString();
                                print("remarks:"+remarks);
                            }else{
                                Toast.makeText(MainActivity.this,
                                        "没有正在进行的项目。\n" +
                                                "请单击来开始一个项目。",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setNegativeButton("取消",new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){

                        }
                    })
                    .create()
                    .show();
            return true;
        }
    }
    //countNo为-1时执行的操作
    public void startNew(int position){
        countNo=position;
        remarks="";
        changeImage(position);
        writeRecord(position);
    }
    //countNo不为-1时执行的操作
    public void endOld(){
        int cTemp=countNo;
        countNo=-1;
        changeImage(cTemp);
        writeRecord(cTemp);
    }
    //为显示详细记录设置的监听器。
    class MyReadOnClickListener implements View.OnClickListener{
        TextView textOut = (TextView)findViewById(R.id.textOutput);
        public void onClick(View v){
            textOut.setText(read());
        }
    }
    //更改列表position项项目的状态图标
    public void changeImage(int position){
        Map<String,Object> item = listItems.get(position);
        item.put("irons",images[countNo==-1?0:1]);
        listItems.set(position,item);
        simpleAdapter.notifyDataSetChanged();
    }
    //往文件中写入记录
    public void writeRecord(int position){
        DateFormat dft = new SimpleDateFormat("yyyy-MM-dd-EEE-HH-mm-ss");
        String recordStr = dft.format(new Date())+
                "|"+startOrEnd[(countNo==-1)?1:0]+
                "|"+names[position]+
                "|"+remarks+"|";
        write(recordStr);
        freshCountNo();
    }
    //往SharedPreferences中写入中间变量
    public void freshCountNo(){
        editor = preferences.edit();
        editor.putInt("countNo",countNo);
        editor.putString("remarks",remarks);
        editor.commit();
    }

    class MyCalWritingTimeOnClickListenre implements View.OnClickListener{
        public void onClick(View view){
            try{
                //获得文件对象
                File f = new File(getFilesDir(),FILE_NAME);
                //获得raf对象
                RandomAccessFile raf = new RandomAccessFile(f,"r");
                //获得文件长度，这个长度的单位知道
                long len = raf.length();
                print("len:"+len);
                //获得目前指针位置
                long start = raf.getFilePointer();
                //调整位置到文件末尾
                start=start+len-1;
                print("start:"+start);
                raf.seek(start);
                print("read前的位置："+raf.getFilePointer());
                raf.read();
                print("read后的位置："+raf.getFilePointer());
                //调整start到倒数第十二行末尾换行符前两个字符。
                int count = 0;
                int c ;
                while(count<10){
                    c=raf.read();
                    //print("c:"+(String.valueOf(c)));
                    if(c=='\n') count++;
                    start--;
                    start--;
                    raf.seek(start);
                }
                print("start after while:"+start);
                //设置start为倒数第十一行开头
                start+=3;
                raf.seek(start);
                //处理后十一行，找出写作的开始结束时间，并及时计算用时
                String result=null;
                long totalTime=0;
                while((result=raf.readLine())!=null){
                    result=new String(result.getBytes("8859_1"), "utf-8");
                    String resStr[] = result.split("\\|");
                    Date startTime;
                    Date endTime;
                    DateFormat dft = new SimpleDateFormat("yyyy-MM-dd-EEE-HH-mm-ss");
                    if(resStr[1].equals("start") && resStr[2].equals("写随笔")){
                        startTime = (Date)dft.parse(resStr[0]);
                        result=raf.readLine();
                        if(result!=null)
                            result=new String(result.getBytes("8859_1"), "utf-8");
                        resStr = result.split("\\|");
                        endTime = (Date)dft.parse(resStr[0]);
                        //getTime()返回的是毫秒
                        totalTime+=(endTime.getTime()-startTime.getTime());
                    }
                }
                totalTime/=(1000*60);
                result="写随笔用时 :"+totalTime+" 分钟";
                print(result);
                Toast toast=Toast.makeText(MainActivity.this,result,Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,-100);
                toast.show();
                /*
                //读取下一行。
                String result = raf.readLine();
                result=new String(result.getBytes("8859_1"), "utf-8");
                print("result:"+result);
                //从一行中分离出时间tim，状态sta，标签rem。
                String resStr[] = result.split("\\|");
                for(String temStr:resStr){
                    print(temStr);
                }
                */
                raf.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}
