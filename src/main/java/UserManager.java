import java.util.*;
import java.io.*;

public class UserManager {

    static ArrayList<User> users = new ArrayList<>();

    static final String FILE = "users.txt";

    static {

        loadUsers();

        if(users.isEmpty()){
            users.add(new User("admin","admin123",UserRole.ADMIN));
            saveUsers();
        }

    }

    public static String createUser(String username, UserRole role){

        for(User u : users){
            if(u.username.equalsIgnoreCase(username)){
                return null;
            }
        }

        String password = PasswordGenerator.generate();

        User user = new User(username,password,role);

        users.add(user);

        saveUsers();

        return password;
    }
    public static User login(String username,String password){

        for(User user : users){

            if(user.username.equals(username) && user.password.equals(password)){
                return user;
            }

        }

        return null;
    }


    public static void saveUsers(){

        try(PrintWriter writer = new PrintWriter(new FileWriter(FILE))){

            for(User user : users){

                writer.println(
                        user.username + "," +
                                user.password + "," +
                                user.role + "," +
                                user.firstLogin + "," +
                                user.originalPassword
                );

            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }


    public static void loadUsers(){

        File file = new File(FILE);

        if(!file.exists()) return;

        try(BufferedReader reader = new BufferedReader(new FileReader(FILE))){

            String line;

            while((line = reader.readLine()) != null){

                String[] parts = line.split(",");

                String username = parts[0];
                String password = parts[1];
                UserRole role = UserRole.valueOf(parts[2]);
                boolean firstLogin = Boolean.parseBoolean(parts[3]);

                User user = new User(username, password, role);
                user.firstLogin = firstLogin;

                if(parts.length > 4){
                    user.originalPassword = parts[4];
                } else {
                    user.originalPassword = password;
                }

                users.add(user);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}