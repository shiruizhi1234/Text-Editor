package editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;


public class Editor extends Application {

    private final Rectangle textBoundingBox;
    private ScrollBar scrollBar;
    private FastLinkedList buffer;
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 500;
    public Editor() {
        // Create a rectangle to surround the text that gets displayed.  Initialize it with a size
        // of 0, since there isn't any text yet.
        textBoundingBox = new Rectangle(0, 0);
        scrollBar = new ScrollBar();
    }
    private class KeyEventHandler implements EventHandler<KeyEvent> {

        KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                // Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
                // the KEY_TYPED event, javafx handles the "Shift" key and associated
                // capitalization.
                String characterTyped = keyEvent.getCharacter();
                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
                    // Ignore control keys, which have non-zero length, as well as the backspace
                    // key, which is represented as a character of value = 8 on Windows.
                    buffer.addChar(characterTyped.charAt(0));
                    display();
                    keyEvent.consume();
                }

               UpdateBoundingBox();
            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.UP) {
                    buffer.fontSize += 5;
                    display();
                } else if (code == KeyCode.DOWN) {
                    buffer.fontSize = Math.max(0, buffer.fontSize - 5);
                    display();
                } else if (code == KeyCode.BACK_SPACE) {
                    buffer.deleteChar();
                    display();
                } else if (code == KeyCode.COMMAND) {
                    System.out.println(textBoundingBox.getY());
                } else if (code == KeyCode.LEFT) {
                    buffer.currentNodeMoveLeft();
                    UpdateBoundingBox();
                } else if (code == KeyCode.RIGHT) {
                    buffer.currentNodeMoveRight();
                    UpdateBoundingBox();
                }
            }
        }
    }
    /** An event handler that displays the current position of the mouse whenever it is clicked. */
    private class MouseClickEventHandler implements EventHandler<MouseEvent> {
        /** A Text object that will be used to print the current mouse position. */
        Text positionText;
        Group root;

        MouseClickEventHandler(Group root) {
            // For now, since there's no mouse position yet, just create an empty Text object.
            this.root = root;
            positionText = new Text("");
            // We want the text to show up immediately above the position, so set the origin to be
            // VPos.BOTTOM (so the x-position we assign will be the position of the bottom of the
            // text).
            positionText.setTextOrigin(VPos.BOTTOM);

            // Add the positionText to root, so that it will be displayed on the screen.
//            root.getChildren().add(positionText);
        }


        @Override
        public void handle(MouseEvent mouseEvent) {
            // Because we registered this EventHandler using setOnMouseClicked, it will only called
            // with mouse events of type MouseEvent.MOUSE_CLICKED.  A mouse clicked event is
            // generated anytime the mouse is pressed and released on the same JavaFX node.
            double mousePressedX = mouseEvent.getX();
            double mousePressedY = mouseEvent.getY() + root.getLayoutY();

            // Display text right above the click.
            positionText.setText("(" + mousePressedX + ", " + mousePressedY + ")");
            positionText.setX(mousePressedX);
            positionText.setY(mousePressedY);
            int lineNum = (int) Math.round(mousePressedY + scrollBar.getValue()) / buffer.fontHeight();
            buffer.changeCurrentNode(lineNum, (int) Math.round(mousePressedX));
            UpdateBoundingBox();
            System.out.println(lineNum);
        }
    }
    private class RectangleBlinkEventHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex = 0;
        private Color[] boxColors =
                {Color.BLACK, Color.WHITE};

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
    private void UpdateBoundingBox() {
        // Figure out the size of the current text.
        int textHeight = buffer.fontHeight();
        int textWidth = 1;

        // Re-size and re-position the bounding box.
        textBoundingBox.setHeight(textHeight);
        textBoundingBox.setWidth(textWidth);
        // For rectangles, the position is the upper left hand corner.
        textBoundingBox.setX(buffer.getCursorX());
        textBoundingBox.setY(buffer.getCursorY());
        // Many of the JavaFX classes have implemented the toString() function, so that
        // they print nicely by default.
    }
    private void scrollBarDisplay() {
        // Set the height of the scroll bar so that it fills the whole window.
        scrollBar.setPrefHeight(buffer.window_height);

        // Set the range of the scroll bar.
        scrollBar.setMin(0);
        scrollBar.setMax(Math.max(0, buffer.lineReferences.size() * buffer.fontHeight() - buffer.window_height));
        double usableScreenWidth = buffer.window_width - scrollBar.getLayoutBounds().getWidth();
        scrollBar.setLayoutX(usableScreenWidth);
    }
    private void centerCursor() {
        scrollBar.setValue(Math.max(0, textBoundingBox.getY() + buffer.fontHeight() - buffer.window_height));
    }
    private void display() {
        buffer.display();
        UpdateBoundingBox();
        scrollBarDisplay();
        if ((textBoundingBox.getY() - scrollBar.getValue() < 0 ) || (textBoundingBox.getY() + buffer.fontHeight() - scrollBar.getValue() > buffer.window_height)) centerCursor();
    }
    @Override
    public void start(Stage primaryStage) {
        String[] args = getParameters().getRaw().toArray(new String[0]);
        // Create a Node that will be the parent of all things displayed on the screen.
        Group root = new Group();
        Group textRoot = new Group();
        buffer = new FastLinkedList(textRoot);
        root.getChildren().add(textRoot);
        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed.
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);
        scrollBar.setOrientation(Orientation.VERTICAL);
        buffer.right_margin = buffer.right_margin + (int) scrollBar.getLayoutBounds().getWidth();
        scrollBarDisplay();
        root.getChildren().add(scrollBar);

        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        scene.setOnMouseClicked(new MouseClickEventHandler(root));
        textRoot.getChildren().add(textBoundingBox);
        makeRectangleColorChange();
        primaryStage.setTitle("Editor");
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenWidth,
                    Number newScreenWidth) {
                // Re-compute Allen's width.
                buffer.window_width = newScreenWidth.intValue();
                buffer.display();
                UpdateBoundingBox();
                scrollBarDisplay();
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenHeight,
                    Number newScreenHeight) {
                buffer.window_height = newScreenHeight.intValue();
                buffer.display();
                UpdateBoundingBox();
                scrollBarDisplay();
            }
        });
        scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {
                // newValue describes the value of the new position of the scroll bar. The numerical
                // value of the position is based on the position of the scroll bar, and on the min
                // and max we set above. For example, if the scroll bar is exactly in the middle of
                // the scroll area, the position will be:
                //      scroll minimum + (scroll maximum - scroll minimum) / 2
                // Here, we can directly use the value of the scroll bar to set the height of Josh,
                // because of how we set the minimum and maximum above.
                textRoot.setLayoutY(-Math.round(newValue.doubleValue()));
            }
        });

//        String inputFilename = args[0];
////        String outputFilename = args[1];
//        try {
//            File inputFile = new File(inputFilename);
//            // Check to make sure that the input file exists!
//            if (!inputFile.exists()) {
//                System.out.println("Unable to copy because file with name " + inputFilename
//                        + " does not exist");
//                return;
//            }
//            FileReader reader = new FileReader(inputFile);
//            // It's good practice to read files using a buffered reader.  A buffered reader reads
//            // big chunks of the file from the disk, and then buffers them in memory.  Otherwise,
//            // if you read one character at a time from the file using FileReader, each character
//            // read causes a separate read from disk.  You'll learn more about this if you take more
//            // CS classes, but for now, take our word for it!
//            BufferedReader bufferedReader = new BufferedReader(reader);
//
//            // Create a FileWriter to write to outputFilename. FileWriter will overwrite any data
//            // already in outputFilename.
////            FileWriter writer = new FileWriter(outputFilename);
//
//            int intRead = -1;
//            // Keep reading from the file input read() returns -1, which means the end of the file
//            // was reached.
//            while ((intRead = bufferedReader.read()) != -1) {
//                // The integer read can be cast to a char, because we're assuming ASCII.
//                char charRead = (char) intRead;
//                buffer.addChar(charRead);
////                writer.write(charRead);
//            }
//            display();
//
////            System.out.println("Successfully copied file " + inputFilename + " to "
////                    + outputFilename);
//
//            // Close the reader and writer.
////            bufferedReader.close();
////            writer.close();
//        } catch (FileNotFoundException fileNotFoundException) {
//            System.out.println("File not found! Exception was: " + fileNotFoundException);
//        } catch (IOException ioException) {
//            System.out.println("Error when copying; exception was: " + ioException);
//        }

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}