package editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Iterator;
import java.util.LinkedList;


public class test extends Application {
    private final Rectangle textBoundingBox;
    private FastLinkedList buffer;
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 500;
    public test() {
        // Create a rectangle to surround the text that gets displayed.  Initialize it with a size
        // of 0, since there isn't any text yet.
        textBoundingBox = new Rectangle(0, 0);
    }
    private class KeyEventHandler implements EventHandler<KeyEvent> {
        int textCenterX;
        int textCenterY;

        private static final int STARTING_FONT_SIZE = 20;
        private static final int STARTING_TEXT_POSITION_X = 250;
        private static final int STARTING_TEXT_POSITION_Y = 250;
        private LinkedList<String> stringTyped = new LinkedList<>();



        /** The Text to display on the screen. */
        private Text displayText = new Text(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, "");
        private Text displayText2 = new Text();
        private int fontSize = STARTING_FONT_SIZE;

        private String fontName = "Verdana";

        KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
            textCenterX = 0;
            textCenterY = 0;

            // Initialize some empty text and add it to root so that it will be displayed.
            displayText = new Text(textCenterX, textCenterY, "");
            displayText2 = new Text(textCenterX, textCenterY, "a");
            // Always set the text origin to be VPos.TOP! Setting the origin to be VPos.TOP means
            // that when the text is assigned a y-position, that position corresponds to the
            // highest position across all letters (for example, the top of a letter like "I", as
            // opposed to the top of a letter like "e"), which makes calculating positions much
            // simpler!
            displayText.setTextOrigin(VPos.TOP);
            displayText.setFont(Font.font(fontName, fontSize));
            displayText2.setTextOrigin(VPos.TOP);
            displayText2.setFont(Font.font(fontName, fontSize));
            // All new Nodes need to be added to the root in order to be displayed.
            root.getChildren().add(displayText);
            root.getChildren().add(displayText2);
        }
        private String string2display() {
            Iterator<String> head = stringTyped.iterator();
            String stringDisplayed = "";

            while (head.hasNext()) {
                stringDisplayed = stringDisplayed + head.next();
            }
//            System.out.println(stringDisplayed);
            return stringDisplayed;
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                // Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
                // the KEY_TYPED event, javafx handles the "Shift" key and associated
                // capitalization.
                String characterTyped = keyEvent.getCharacter();
//                stringTyped.add(characterTyped);
                buffer.addChar(characterTyped.charAt(0));
                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
                    // Ignore control keys, which have non-zero length, as well as the backspace
                    // key, which is represented as a character of value = 8 on Windows.
//                    System.out.print(characterTyped);
//                    System.out.println(stringDisplayed);
//                    String a = "abc";

//                    displayText.setText(string2display());
                    buffer.display();
                    keyEvent.consume();
                }

                centerTextAndUpdateBoundingBox();
            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.UP) {
                    fontSize += 5;
//                    displayText.setFont(Font.font(fontName, fontSize));
//                    centerTextAndUpdateBoundingBox();
                    buffer.fontSize += 5;
                    buffer.display();
                } else if (code == KeyCode.DOWN) {
//                    fontSize = Math.max(0, fontSize - 5);
//                    displayText.setFont(Font.font(fontName, fontSize));
//                    centerTextAndUpdateBoundingBox();
                    buffer.fontSize = Math.max(0, buffer.fontSize - 5);
                    buffer.display();
                } else if (code == KeyCode.BACK_SPACE) {
                    buffer.deleteChar();
                    buffer.display();
                } else if (code == KeyCode.COMMAND) {
//                    displayText2.setX(displayText2.getX() + 1);
//                    displayText2.setY(displayText2.getY() + 1);
//                    stringTyped.add(new String("\r"));
//                    displayText2.setText(new String("\r"));
//                    System.out.println(displayText2.getLayoutBounds().getWidth());
//                    System.out.println(displayText2.getLayoutBounds().getHeight());
                    displayText2.setX(displayText.getLayoutBounds().getWidth());
                    displayText2.setY(displayText.getLayoutBounds().getHeight());
                }
            }
        }
        private void centerTextAndUpdateBoundingBox() {
            // Figure out the size of the current text.
            double textHeight = displayText.getLayoutBounds().getHeight();
            double textWidth = displayText.getLayoutBounds().getWidth();

            // Calculate the position so that the text will be centered on the screen.
            double textTop = 0;
            double textLeft = 0;

            // Re-position the text.
            displayText.setX(textLeft);
            displayText.setY(textTop);

            // Re-size and re-position the bounding box.
            textBoundingBox.setHeight(textHeight);
            textBoundingBox.setWidth(textWidth);

            // For rectangles, the position is the upper left hand corner.
            textBoundingBox.setX(textLeft);
            textBoundingBox.setY(textTop);
            // Many of the JavaFX classes have implemented the toString() function, so that
            // they print nicely by default.
            System.out.println("Bounding box: " + textBoundingBox);

            // Make sure the text appears in front of the rectangle.
            displayText.toFront();
        }
    }
    private class RectangleBlinkEventHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex = 0;
        private Color[] boxColors =
                {Color.LIGHTPINK, Color.ORANGE, Color.YELLOW,
                        Color.GREEN, Color.LIGHTBLUE, Color.PURPLE};

        RectangleBlinkEventHandler() {
            // Set the color to be the first color in the list.
            changeColor();
        }

        private void changeColor() {
            textBoundingBox.setFill(boxColors[currentColorIndex]);
            currentColorIndex = (currentColorIndex + 1) % boxColors.length;
        }

        @Override
        public void handle(ActionEvent event) {
            changeColor();
        }
    }
    public void makeRectangleColorChange() {
        // Create a Timeline that will call the "handle" function of RectangleBlinkEventHandler
        // every 1 second.
        final Timeline timeline = new Timeline();
        // The rectangle should continue blinking forever.
        timeline.setCycleCount(Timeline.INDEFINITE);
        RectangleBlinkEventHandler cursorChange = new RectangleBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }
    @Override
    public void start(Stage primaryStage) {
        // Create a Node that will be the parent of all things displayed on the screen.
        Group root = new Group();
        Group textRoot = new Group();
        buffer = new FastLinkedList(textRoot);
        root.getChildren().add(textRoot);
        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed.
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);

        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        root.getChildren().add(textBoundingBox);
        makeRectangleColorChange();
        primaryStage.setTitle("Editor");


        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}