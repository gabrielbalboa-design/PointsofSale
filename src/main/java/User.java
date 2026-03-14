public class User {

    String username;
    String password;
    UserRole role;
    boolean firstLogin;

    public User(String username, String password, UserRole role){

        this.username = username;
        this.password = password;
        this.role = role;
        this.firstLogin = true;

    }
}
