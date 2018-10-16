
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    public class User {
        private String username;
        private String password;
        private boolean permissao; //false-normal true-editor

        public User(String username, String password, boolean permissao) {
            this.username = username;
            this.password = password;
            this.permissao = permissao;
        }
    }

    private Connection c;
    private PreparedStatement st;
    private ResultSet rs;

    public Database() {
        try {
            Class.forName("org.postgresql.Driver");
            this.c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/DropMusic.Database", "postgres", "surawyk");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private ArrayList<User> users = new ArrayList<>();

    public HashMap<String, String> process(HashMap<String,String> message) {
        HashMap<String,String> reply = new HashMap<String,String>();
        switch (message.get("type")) {
            case "regist":
                reply = regist(message);
                break;
            case "login":
                reply = login(message);
                break;
            case "show all":
                reply = showall(message);
            default:
                break;
        }
        return reply;
    }

    public HashMap<String,String> regist(HashMap<String, String> message) {
        HashMap<String,String> reply = new HashMap<String,String>();
        //exemplo --> type|regist;username|name;password|pass
        try {
            st = c.prepareStatement("select name from public.users where name = '" + message.get("username") + "'");
            rs = st.executeQuery();
            if (rs.next()) {
                reply.put("type", "status");
                reply.put("regist", "failed");
                reply.put("msg", "Username already in use.");
                return reply;
            }
            st = c.prepareStatement("select count(name) from public.users");
            rs = st.executeQuery();
            st = c.prepareStatement("INSERT INTO public.users(id,name, password, permission) VALUES (DEFAULT,?, ?, ?);");
            st.setString(1, message.get("username"));
            st.setString(2, message.get("password"));
            if (rs.next() && rs.getInt(1) == 0) {
                st.setBoolean(3, true);
            } else {
                st.setBoolean(3, false);
            }
            st.executeUpdate();
            reply.put("type", "status");
            reply.put("regist", "successful");
            reply.put("msg", "Registry done.Enjoy DropMusic.");
            return reply;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            reply.put("type", "status");
            reply.put("regist","failed");
            reply.put("msg", e.getMessage());
            return reply;
        }
    }

    public HashMap<String, String> login(HashMap<String, String> message) {
        //exemplo --> type|regist;username|name;password|pass;
        HashMap<String, String> reply = new HashMap<String, String>();
        try {
            st = c.prepareStatement("select exists( select * from public.users where not status and name='" + message.get("username") + "' and password='" + message.get("password") + "' );");
            rs = st.executeQuery();
            if (rs.next() && rs.getBoolean(1)) {
                st = c.prepareStatement("update public.users set status=true where name='" + message.get("username") + "';");
                reply.put("type", "status");
                reply.put("login", "successful");
                reply.put("msg", "You're logged in.");
                return reply;
            }
            reply.put("type", "status");
            reply.put("login", "failed");
            reply.put("msg", "Credentials wrong.");
            return reply;
        } catch (Exception e) {
            reply.put("type", "status");
            reply.put("login", "failed");
            reply.put("msg", e.getMessage());
            return reply;
        }
    }
    public HashMap<String,String> showall(HashMap<String,String> message){
        //exemplo --> type|show all;select|(artists,albums,musics)
        HashMap<String,String> reply = new HashMap<String,String>();
        String m;
        try{
            st = c.prepareStatement("select id,title from public."+message.get("select")+";");
            rs = st.executeQuery();
            while(rs.next()){
                reply.put(String.valueOf(rs.getInt(1)), rs.getString(2));
            }
            return reply;
        }catch (Exception e){
            reply.put("type", "show all");
            reply.put("msg", e.getMessage());
            return reply;
        }
    }

}
