class ListNode {
    public ListNode next;
}

public class ZCList {
    public static void main(String[] args) {
        ListNode head;
        ListNode newNode;
        ListNode current;
        int i;
        head = new ListNode();
        current = head;
        for (i = 0; i < 5; i++) {
            newNode = new ListNode();
            current.next = newNode;
            current = newNode;
        }
        current.next = head;
        head = null;
    }
}
