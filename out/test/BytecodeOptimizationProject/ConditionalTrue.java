package inputs;

public class ConditionalTrue {
    public static void main(String[] args) {
        int i;

        i = 1;
        i = i > 0 ? 2 : 3;
        StaticJavaLib.assertTrue(i == 2);
    }
}
    
