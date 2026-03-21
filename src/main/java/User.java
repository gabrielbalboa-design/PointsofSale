public class User {

    public boolean isGeneratedPassword;
    String username;
    String password;
    UserRole role;
    boolean firstLogin;
    String originalPassword;

    public User(String username, String password, UserRole role){

        this.username = username;
        this.password = password;
        this.originalPassword = password;
        this.role = role;
        this.firstLogin = true;
    }
}
