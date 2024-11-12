public class ZFibonacci {
    public static void main(String[] args) {
        int n;
        int[] fibArray;
        int i;
        int a;
        int b;
        n = 10;
        fibArray = new int[n];

        if (n >= 1) {
            fibArray[0] = 0;
        }
        if (n >= 2) {
            fibArray[1] = 1;
        }
        for (i = 2; i < n; i++) {
            a = fibArray[i - 1];
            b = fibArray[i - 2];
            fibArray[i] =  a + b;
        }
    }
}
