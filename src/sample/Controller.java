package sample;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {
    private static final int COLUMNS = 7;
    private static final int ROWS = 6;

    private static final int CIRCLE_DIAMETER = 80;

    private static final String disc1Color = "#24303E";
    private static final String disc2Color = "4CAA88";

    private static String PLAYER_ONE = "Player One";
    private static String PLAYER_TWO = "Player Two";

    private Disc [][] insertedDiscArray = new Disc[ROWS][COLUMNS];

    private boolean isPlayerOneTurn = true;
    private boolean isAllowedToInsert = true;


    @FXML
    public GridPane rootGridPane;

    @FXML
    public TextField playerOneTextField;

    @FXML
    public TextField playerTwoTextField;

    @FXML
    public Button setNamesButton;

    @FXML
    public Pane insertedDiscPane;

    @FXML
    public Label playerNameLable;
    
    public void createPlayGround(){
        Shape rectangleWithHoles = createGameStructuralGrid();
        rootGridPane.add(rectangleWithHoles, 0, 1);
        List<Rectangle> list = createClickableColumn();
        for(Rectangle rectangle : list) {
            rootGridPane.add(rectangle, 0, 1);
        }
    }

    private Shape createGameStructuralGrid(){
        Shape rectangleWithHoles = new Rectangle((1 + COLUMNS)*CIRCLE_DIAMETER, (ROWS + 1)*CIRCLE_DIAMETER);
        for(int row = 0; row<ROWS;row++){
            for(int col = 0;col<COLUMNS;col++){
                Circle circle = new Circle();
                circle.setRadius(CIRCLE_DIAMETER / 2);
                circle.setCenterX(CIRCLE_DIAMETER /  2);
                circle.setCenterY(CIRCLE_DIAMETER / 2);
                circle.setSmooth(true);
                circle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);
                circle.setTranslateY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);
                rectangleWithHoles = Shape.subtract(rectangleWithHoles, circle);
            }
        }

        rectangleWithHoles.setFill(Color.WHITE);
        return rectangleWithHoles;
    }

    private List<Rectangle> createClickableColumn(){
        List<Rectangle> list = new ArrayList<>();
        for(int col = 0;col<COLUMNS;col++){
            Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER, (ROWS + 1)*CIRCLE_DIAMETER);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setTranslateX(col * (CIRCLE_DIAMETER +5) + CIRCLE_DIAMETER/4);
            rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee29")));
            rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));
            final int column = col;
            rectangle.setOnMouseClicked(event -> {
                if(isAllowedToInsert){
                    isAllowedToInsert = false;
                    insertDisc(new Disc(isPlayerOneTurn), column);
                }

            });
            list.add(rectangle);
        }
        return list;
    }

    private void insertDisc(Disc disc, int col){
        int row = ROWS - 1;
        while (row>=0){
            if(getDiscIfPresent(row, col) == null)break;
            row--;
        }
        if(row < 0)return;
        insertedDiscArray [row][col] = disc;
        insertedDiscPane.getChildren().add(disc);
        disc.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.7), disc);
        translateTransition.setToY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);
        int currentRow = row;
        translateTransition.setOnFinished(event -> {
            isAllowedToInsert = true;
            if(gameEnded(currentRow, col)){
                gameOver();
                return;
            }
            isPlayerOneTurn = !isPlayerOneTurn;
            playerNameLable.setText(isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO);
        });
        translateTransition.play();
    }

    private void gameOver() {
        String winner = isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connect Four");
        alert.setHeaderText("The winner is : "+ winner);
        alert.setContentText("Want to play again ?");
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No, exit");
        alert.getButtonTypes().setAll(yesButton, noButton);
        Platform.runLater(()->{
            Optional<ButtonType> btnClicked = alert.showAndWait();
            if(btnClicked.isPresent() && btnClicked.get() == yesButton){
                resetGame();
            }else {
                Platform.exit();
                System.exit(0);
            }

        });
    }

    public void resetGame() {
        insertedDiscPane.getChildren().clear();
        for(int row = 0;row<ROWS;row++){
            for(int col = 0;col<COLUMNS;col++){
                insertedDiscArray[row][col] = null;
            }
        }
        isPlayerOneTurn = true;
        playerNameLable.setText(PLAYER_ONE);
        createPlayGround();
    }

    private boolean gameEnded(int currentRow, int col) {
        List<Point2D> verticalPoints = IntStream.rangeClosed(currentRow-3, currentRow+3)
                .mapToObj(r ->new Point2D(r, col))
                .collect(Collectors.toList());

        List<Point2D> horizontalPoints = IntStream.rangeClosed(col-3, col+3)
                .mapToObj(c ->new Point2D(currentRow, c))
                .collect(Collectors.toList());

        Point2D startingPoint1 = new Point2D(currentRow-3,col + 3);
        List<Point2D> diagonalPoints1 = IntStream.rangeClosed(0, 6)
                .mapToObj(i ->startingPoint1.add(i, -i))
                .collect(Collectors.toList());

        Point2D startingPoint2 = new Point2D(currentRow-3,col - 3);
        List<Point2D> diagonalPoints2 = IntStream.rangeClosed(0, 6)
                .mapToObj(i ->startingPoint2.add(i, i))
                .collect(Collectors.toList());

        boolean isEnded = checkCombinations(verticalPoints)
                || checkCombinations(horizontalPoints)
                || checkCombinations(diagonalPoints1)
                || checkCombinations(diagonalPoints2);
        return isEnded;
    }

    private Disc getDiscIfPresent(int row, int col){
        if(row >= ROWS || row<0 || col>=COLUMNS || col<0){
            return null;
        }
        return insertedDiscArray[row][col];
    }

    private boolean checkCombinations(List<Point2D> Points) {
        int chain = 0;
        for (Point2D point : Points){
            int rowIndexForArray = (int)point.getX();
            int columnIndexForArray = (int)point.getY();
            Disc disc = getDiscIfPresent(rowIndexForArray, columnIndexForArray);
            if(disc != null && disc.isPlayerOneMove == isPlayerOneTurn){
                chain++;
                if(chain == 4){
                    return true;
                }
            }else {
                chain = 0;

            }
        }
        return false;
    }

    private static class Disc extends Circle {
        private final boolean isPlayerOneMove;

        public Disc(boolean isPlayerOneMove){
            this.isPlayerOneMove = isPlayerOneMove;
            setRadius(CIRCLE_DIAMETER/2);
            setFill(isPlayerOneMove ? Color.valueOf(disc1Color) : Color.valueOf(disc2Color));
            setCenterX(CIRCLE_DIAMETER/2);
            setCenterY(CIRCLE_DIAMETER/2);
        }
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setNamesButton.setOnAction(event -> {
            PLAYER_ONE = playerOneTextField.getText();
            PLAYER_TWO = playerTwoTextField.getText();
            playerNameLable.setText(isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO);
        });
    }
}
