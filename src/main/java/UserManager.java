import java.util.ArrayList;

public class UserManager {

    static ArrayList<User> users = new ArrayList<>();

    static {

        // Default admin
        users.add(new User("admin","admin123",UserRole.ADMIN));

        // Sample accounts
        createUser("manager1",UserRole.MANAGER);
        createUser("staff1",UserRole.STAFF);

    }

    public static void createUser(String username, UserRole role){

        String password = PasswordGenerator.generate();

        User user = new User(username,password,role);

        users.add(user);

        System.out.println("Account created:");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        System.out.println("Role: " + role);

    }

    public static User login(String username,String password){

        for(User user : users){

            if(user.username.equals(username) && user.password.equals(password)){

                return user;

            }

        }

        return null;
    }

}
