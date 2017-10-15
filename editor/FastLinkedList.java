package editor;

import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by ruizhishi on 9/7/17.
 */
public class FastLinkedList implements Iterable<Text> {
    class Node {
        private Text nodeText;
        private Node prev, next;

        Node(Text nodeText) {
            this.nodeText = nodeText;
            this.nodeText.setTextOrigin(VPos.TOP);
        }
    }

    private Node sentinel;
//    private int currentPos;
    private Node currentNode;
    private int size;
    public int fontSize = 12;
    public String fontName = "Verdana";
    private Group textRoot;
    public ArrayList<Node> lineReferences;
//    public ArrayList<Integer> linePos;
    public int window_width = 500;
    public int window_height = 500;
    public int left_margin = 5;
    public int right_margin = 5;
    FastLinkedList(Group textRoot) {
        sentinel = new Node(new Text(left_margin, 0, ""));
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
//        currentPos = 0;
        currentNode = sentinel;
        lineReferences = new ArrayList<>();
        size = 0;
        this.textRoot = textRoot;
    }
    public void addChar(char x) {
        Node newNode = new Node(new Text(String.valueOf(x)));
        newNode.prev = currentNode;
        newNode.next = currentNode.next;
        currentNode.next.prev = newNode;
        currentNode.next = newNode;
        currentNode = newNode;
//        currentPos++;
        size++;
        textRoot.getChildren().add(currentNode.nodeText);
    }
    public void deleteChar() {
        if (currentNode == sentinel) return;
        currentNode.next.prev = currentNode.prev;
        currentNode.prev.next = currentNode.next;
        textRoot.getChildren().remove(currentNode.nodeText);
        currentNode = currentNode.prev;
//        currentPos--;
        size--;
    }
    public int size() {
        return size;
    }
//    public int getCurrentPos() {
//        return currentPos;
//    }
    public Node getCurrentNode() {
        return currentNode;
    }
    public int getCursorX() {
        return (int) Math.round(currentNode.nodeText.getX() + currentNode.nodeText.getLayoutBounds().getWidth());
    }
    public int getCursorY() {
        return (int) Math.round(currentNode.nodeText.getY());
    }
    public int getCursorHeight() {
        return (int)Math.round(currentNode.nodeText.getLayoutBounds().getHeight());
    }
    public Iterator<Text> iterator(){
        return new FastLinkedListIterator();
    }
    private class FastLinkedListIterator implements Iterator<Text> {
        int index = 0;
        Node head = sentinel;
        public boolean hasNext() {
            return index < size;
        }
        public Text next() {
            head = head.next;
            index++;
            return head.nodeText;
        }
    }
    public void display() {
        if (size == 0) return;
        lineReferences = new ArrayList<>();
        int pos = 0;
        sentinel.nodeText.setFont(Font.font(fontName, fontSize));
        Node lastSpace = sentinel;
        int lasSpacePos = 0;
        Node head = sentinel.next;
        lineReferences.add(head);
        while (pos < size) {
            if (head.nodeText.getText().equals(" ")) {
                lastSpace = head;
                lasSpacePos = pos;
            }
            if (head.nodeText.getText().equals("\r") || head.nodeText.getText().equals("\n") || head.nodeText.getText().equals("\r\n")) {
                startNewline(head);
            }
            else if (isLineFull(head)) {
//                startNewline(head);
                moveWord(lastSpace, head, lasSpacePos, pos);
            }
            else {
                displayInline(head);
            }
            head.nodeText.setFont(Font.font(fontName, fontSize));
            head = head.next;
            pos++;
        }
    }
    private boolean isLineFull(Node head) {
        Node last = head.prev;
        return last.nodeText.getX() + last.nodeText.getLayoutBounds().getWidth() + head.nodeText.getLayoutBounds().getWidth() + left_margin + right_margin > window_width;
    }
    private void startNewline(Node head) {
        Node last = head.prev;
        head.nodeText.setX(left_margin);
        head.nodeText.setY((int) Math.round(last.nodeText.getY() + fontHeight()));
        lineReferences.add(head);
        return;
    }
    private void displayInline(Node head) {
        Node last = head.prev;
        head.nodeText.setX((int) Math.round(last.nodeText.getX() + last.nodeText.getLayoutBounds().getWidth() + 1));
        head.nodeText.setY((int) Math.round(last.nodeText.getY()));
        return;
    }
    private void moveWord(Node lastSpace, Node head, int lastSpacePos, int pos) {
        Node letter = lastSpace.next;
        if (letter.nodeText.getY() + head.prev.nodeText.getLayoutBounds().getHeight() < head.prev.nodeText.getY() || letter.nodeText.getX() == left_margin) {
            startNewline(head);
            return;
        }
        startNewline(letter);
        letter = letter.next;
        for (int i = lastSpacePos + 2; i <= pos; i++) {
            displayInline(letter);
            letter = letter.next;
        }
        return;
    }
    public void changeCurrentNode(int lineNum, int x) {
        if (lineNum >= lineReferences.size()) {
            currentNode = sentinel.prev;
            return;
        }
        if (lineNum < 0) {
            currentNode = sentinel.next;
            return;
        }
        Node lineHead = lineReferences.get(lineNum);
//        System.out.println(lineHead.nodeText);
        Node head = lineHead;
        Node closest = lineHead;
        int lineY = (int) lineHead.nodeText.getY();
        int minVal = Math.abs((int) Math.round(closest.nodeText.getX() + closest.nodeText.getLayoutBounds().getWidth()) - x);
        while (head != null && head.nodeText.getY() == lineY && head != sentinel) {
            int distance = Math.abs((int) Math.round(head.nodeText.getX() + head.nodeText.getLayoutBounds().getWidth()) - x);
            if (distance < minVal) {
                closest = head;
                minVal = distance;
            }
            head = head.next;
        }
        currentNode = closest;
        return;
    }
    public int fontHeight() {
        Text testHeight = new Text("a");
        testHeight.setFont(Font.font(fontName, fontSize));
        return (int) Math.round(testHeight.getLayoutBounds().getHeight());
    }
    public void currentNodeMoveRight() {
        if (currentNode == sentinel || currentNode == sentinel.prev) return;
        currentNode = currentNode.next;
    }
    public void currentNodeMoveLeft() {
        if (currentNode == sentinel || currentNode == sentinel.next) return;
        currentNode = currentNode.prev;
    }


}
