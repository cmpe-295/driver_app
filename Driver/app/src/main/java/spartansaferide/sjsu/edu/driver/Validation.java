package spartansaferide.sjsu.edu.driver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validation {

    private static Pattern pattern;
    private static Matcher matcher;
    //Email Pattern
    //private static final String EMAIL_PATTERN = "^[A-Za-z]+.[A-Za-z0-9]+@sjsu\\.edu";

    private static final String EMAIL_PATTERN = "^[A-Za-z]+.[A-Za-z0-9]+@gmail\\.com";

    public static boolean validate(String email) {
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();

    }

    public static boolean isNotNull(String txt){
        return txt!=null && txt.trim().length()>0 ? true: false;
    }


}


