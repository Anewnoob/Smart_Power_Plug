import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.io.PrintWriter;
import java.util.TimerTask;

public class MyServer {
    public static void main(String args[])throws Exception {
        System.out.println("你好");
        Thread db = new DataBase();
        db.start();
        Thread ad = new android();
        ad.start();
        Thread sc = new sock();
        sc.start();
    }
}

class DataBase extends Thread {
    public void run()
    {
        try {
            java.util.Timer timer = new java.util.Timer();
            timer.schedule(new DB_insert(), 100000, 86400000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class DB_insert extends TimerTask {
    public void run()
    {
        Connection conn = null;
        Statement stmt = null;
        String connectionString = "jdbc:mysql://localhost:3306/TEST_DATABASE?user=root&password=123456";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(connectionString);
            stmt = conn.createStatement();
            String sql = "INSERT INTO POWER (DA,COMSUMPTION ) VALUES('"+Timer.get_time()+"',"+ Power.get_power()+")";
            int i = stmt.executeUpdate(sql);
            if (i > 0) {
                System.out.println("inserted!");
            } else {
                System.out.println("may failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



class android extends Thread {
    public void run() {
        try{
            ServerSocket server = new ServerSocket(10000);
            while (true) {
                Multi_android mc = new Multi_android(server.accept());
                System.out.println("Android 已连接");
                mc.start();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

class Multi_android extends Thread {
    private Socket client;

    public Multi_android(Socket c) {
        this.client = c;
    }

    public void run() {
        try {
            int result;
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while (true) {
                String str = in.readLine();
                if(str==null)
                {
                    str="0";
                    continue;
                }
                System.out.println(str+"from android");
                if (str == "1") {
                    System.out.println("Android 请求开启插座");
                    Multi_sock.turn_on();
                }
                else if (str=="2") {
                    System.out.println("Android 请求关闭插座");
                    Multi_sock.turn_off();
                }
            }
        } catch (IOException ex) {
        }
    }
}

 class sock extends Thread{
     public void run() {
         try{
             ServerSocket server = new ServerSocket(10001);
             while (true) {
                 Multi_sock ms = new Multi_sock(server.accept());
                 System.out.println("插座 Socket已连接");
                 ms.start();
             }
         }catch(IOException e){
             e.printStackTrace();
         }
     }
 }

class Multi_sock extends Thread
{
    private  static Socket client;
    private static PrintWriter out;
    public Multi_sock(Socket c)throws IOException
    {
        this.client=c;
    }
    public void run()
    {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream());
            while(true) {
                String str = in.readLine();
                System.out.println("插座端上传数据："+str);
                int open,current,voltage;
                int i = Integer.parseInt(str);
                open = i%10;
                if(open==1) {
                    System.out.println("");
                    i=i/10;
                    current =i%10000;
                    i=i/100000;
                    voltage=i;
                    Power.add_power(current,voltage);
                }
                else
                {
                    System.out.println("power not on from linux!");
                }
            }
            } catch (Exception e) {
                System.out.println("linux socket closed!");
                client=null;
            }
    }
    public static void turn_on()throws IOException
    {
        if(client==null)
        {
            System.out.println("not connected to linux");
        }
        else{
            System.out.println("try to open electric source");
            out.println("1");
            System.out.println("electric source opened");
            out.flush();
        }
    }
    public static void turn_off()throws IOException
    {
        if(client==null)
        {
            System.out.println("not connected to linux");
        }
        else{
            System.out.println("try to close electric source");
            out.println("2");
            System.out.println("electric source closed");
            out.flush();
        }
    }
}

class Power
{
    private static int  power_today;
    public static void add_power(int ma,int v)
    {
        power_today=power_today+(ma*v);
        System.out.println("comsumptation added");
    }
    public static int get_power()
    {
        int power_yesterday=power_today/10;
        power_today=0;

        return power_yesterday;
    }
}

class Timer {
    public static String get_time() throws ParseException {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateNowStr = sdf.format(d);
        return dateNowStr;
    }
}