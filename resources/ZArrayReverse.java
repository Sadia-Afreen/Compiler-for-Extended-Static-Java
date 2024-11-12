public class ZArrayReverse {
    public static void main(String[] args) {
        int[] arr;
        int start;
        int end;
        int temp;
        arr = new int[] {1, 2, 3, 4, 5};
        start = 0;
        end = 4;

        while (start < end) {
            temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            start++;
            end--;
        }
    }

}
