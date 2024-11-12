public class ZComplementArray {
    public static void main(String[] args){
        boolean[] arr;
        int i;
        int j;
        boolean val;

        arr = new boolean[] {true, true, false, true, false, true, false, false, true};
        i = 9;

        for(j=0; j < 8; j++){
            val = arr[j+1];
            arr[j] = !(arr[j] && val);
        }
    }
}