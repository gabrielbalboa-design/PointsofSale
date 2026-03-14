import java.util.Random;

public class PasswordGenerator {

    static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String generate(){

        Random r = new Random();

        StringBuilder password = new StringBuilder();

        for(int i = 0; i < 8; i++){

            int index = r.nextInt(chars.length());

            password.append(chars.charAt(index));

        }

        return password.toString();
    }
}
