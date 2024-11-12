class ListNode {
    public ListNode next;
}

public class Z1MList {

    public static void main(String[] args) {
        ListNode head;
        ListNode a;
        int i;
        head = new ListNode();
        for (i = 0; i < 1000000; i++) {
            a = new ListNode();
            a.next = head;
            head = a;
        }
        head = null;
    }
}
