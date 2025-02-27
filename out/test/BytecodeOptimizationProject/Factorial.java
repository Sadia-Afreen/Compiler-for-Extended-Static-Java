package inputs;

public class Factorial
{
    public static void main(String[] args)
    {
        factorial(4);
    }
    
    static int factorial(int n)
    {
        int result;
        int i;
        
        StaticJavaLib.assertTrue(n >= 1);
        result = 1;
        i = 2;
        while (i <= n)
        {
            result = result * i;
            i = i + 1;
        }
        
        return result;
    }
}
