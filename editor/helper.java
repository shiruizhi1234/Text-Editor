package editor;
import java.util.LinkedList;

/**
 * Created by ruizhishi on 9/4/17.
 */
public class helper {
    public static void main(String[] args) {

        // create a LinkedList
        LinkedList<String> list = new LinkedList();

        // add some elements
        list.add("Hello");
        list.add("2");
        list.add("Chocolate");
        list.add("10");

        // print the list
        System.out.println("LinkedList:" + list);

        // remove the last element
        System.out.println("Last element:" + list.removeLast());

        // print the list
        System.out.println("LinkedList:" + list);

        // remove the last element
        System.out.println("Last element:" + list.removeLast());
        // print the list
        System.out.println("LinkedList:" + list);
    }
}
