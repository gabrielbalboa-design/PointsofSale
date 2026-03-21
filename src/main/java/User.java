public class User {

    String username;
    String password;
    UserRole role;
    boolean firstLogin;
    boolean isGeneratedPassword;


    public User(String username, String password, UserRole role){

        this.username = username;
        this.password = password;
        this.role = role;
        this.firstLogin = true;
        this.isGeneratedPassword = true;

    }
}