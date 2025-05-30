import java.util.function.Predicate;

public class GenericLinkedList<T> {
    private class Node {
        T data;
        Node next;
        Node(T data) { this.data = data; }
    }

    private Node head;
    private int size = 0;

    public void add(T data) {
        Node newNode = new Node(data);
        if (head == null) head = newNode;
        else {
            Node curr = head;
            while (curr.next != null) curr = curr.next;
            curr.next = newNode;
        }
        size++;
    }

    public T get(int index) {
        Node curr = head;
        for (int i = 0; i < index; i++) curr = curr.next;
        return curr.data;
    }

    public void set(int index, T element) {
        Node curr = head;
        for (int i = 0; i < index; i++) curr = curr.next;
        curr.data = element;
    }

    public int size() { return size; }

    public void clear() { head = null; size = 0; }

    public void addAll(GenericLinkedList<T> other) {
        for (int i = 0; i < other.size(); i++) add(other.get(i));
    }

    public void removeIf(Predicate<T> predicate) {
        while (head != null && predicate.test(head.data)) {
            head = head.next;
            size--;
        }
        Node curr = head;
        while (curr != null && curr.next != null) {
            if (predicate.test(curr.next.data)) {
                curr.next = curr.next.next;
                size--;
            } else {
                curr = curr.next;
            }
        }
    }
}
