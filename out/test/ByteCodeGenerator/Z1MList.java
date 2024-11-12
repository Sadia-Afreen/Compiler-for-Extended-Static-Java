public class Z1MList {
    public static void main(String[] args) {
        // Create a linked list of 1 million objects
        ListNode head = null;
        ListNode current = null;

        for (int i = 0; i < 1000000; i++) {
            ListNode newNode = new ListNode(new Object());
            if (head == null) {
                head = newNode;
                current = newNode;
            } else {
                current.next = newNode;
                current = newNode;
            }
        }

        head = null;
    }
}

public class ListNode {
    public Object val;
    public ListNode next;

    public ListNode(Object val) {
        this.val = val;
        this.next = null;
    }
}
