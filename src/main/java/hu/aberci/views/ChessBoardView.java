package hu.aberci.views;

import hu.aberci.controllers.ChessGameController;
import hu.aberci.entities.events.ChessPieceEvent;
import hu.aberci.entities.interfaces.BoardState;
import hu.aberci.entities.interfaces.Piece;
import hu.aberci.entities.interfaces.PlayerColor;
import hu.aberci.entities.interfaces.Tile;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom GridPane housing the TileViews of a chess board. The PieceViews of a chess board are
 * stored in the TileViews.
 * */
public class ChessBoardView extends GridPane {

    /**
     * JAVAFX property storing the BoardState.
     * */
    @Getter
    ObjectProperty<BoardState> boardStateProperty = new SimpleObjectProperty<>();
    /**
     * JAVAFX property storing the PieceViews similar to how they are stored in the {@link BoardState}.
     * */
    @Getter
    ObservableMap<PlayerColor, List<PieceView>> pieceViews;
    /**
     * JAVAFX property storing the TileViews similar to how they are stored in the {@link BoardState};
     * */
    @Getter
    ObservableList<ObservableList<TileView>> tileViews;

    /**
     * JAVAFX property storing the currently selected PieceView. On the board all tiles are shown green
     * where the selected piece (the piece belonging to the selected PieceView) can move legally.
     * */
    @Getter
    ObjectProperty<PieceView> selectedPieceView;
    /**
     * JAVAFX property storing the TileViews belonging to the selected Piece's legal move tiles.
     * */
    @Getter
    ListProperty<TileView> selectedPieceLegalMoves;

    /**
     * The chessGameController that determines whether a given Piece can move to a given Tile.
     * */
    @Setter
    ChessGameController chessGameController;

    /**
     * JAVAFX property that determines whether the board should be played with an engine.
     * */
    @Getter
    BooleanProperty chessAIEnabledProperty;

    /**
     * JAVAFX property. Only used when the game is played with an engine. If it is, this determines
     * which side the engine plays.
     * */
    @Getter
    ObjectProperty<PlayerColor> chessAIColorProperty;

    /**
     * JAVAFX property that stores whether this game is still being played.
     * */
    @Getter
    BooleanProperty isGameLive;

    /**
     * Default constructor. Initializes the inner properties, adds its own event handlers.
     * */
    public ChessBoardView() {

        super();

        isGameLive = new SimpleBooleanProperty();

        selectedPieceView = new SimpleObjectProperty<>(null);
        selectedPieceLegalMoves = new SimpleListProperty<>(
                FXCollections.observableList(new ArrayList<>())
        );

        chessAIEnabledProperty = new SimpleBooleanProperty(false);
        chessAIColorProperty = new SimpleObjectProperty<>(
                PlayerColor.BLACK
        );

        addEventHandler(
                ChessPieceEvent.CHESS_PIECE_EVENT_PIECE_MOVING,
                chessPieceEvent -> {

                    if (chessGameController.canPieceMoveToTile(chessPieceEvent.getMove().getPiece(), chessPieceEvent.getMove().getTargetTile())) {

                        chessGameController.movePieceToTile(chessPieceEvent.getMove().getPiece(), chessPieceEvent.getMove().getTargetTile());

                        selectedPieceView = new SimpleObjectProperty<>(null);

                    } else {

                        selectedPieceView = new SimpleObjectProperty<>(null);

                    }

                }
        );

        addEventHandler(
                ChessPieceEvent.CHESS_PIECE_EVENT_PIECE_SELECTED,
                chessPieceEvent -> {

                    selectedPieceLegalMoves.clear();

                    if (chessAIEnabledProperty.get()) {

                        if (chessAIColorProperty.get().equals(
                                boardStateProperty.get().getPlayerTurnProperty().get()
                        )) {

                            selectedPieceView.set(null);
                            return;

                        }

                    }

                    if (selectedPieceView.get() != null &&
                            selectedPieceView.get().getPieceProperty().get().getPlayerColorProperty().get() == boardStateProperty.get().getPlayerTurnProperty().get()) {

                        for (TileView tileView: chessGameController.getLegalMovesOf(chessPieceEvent.getMove().getPiece()).stream().map(
                                tile -> tileViews.get(
                                        tile.getXProperty().get()
                                ).get(
                                        tile.getYProperty().get()
                                )
                        ).collect(Collectors.toList())) {

                            selectedPieceLegalMoves.add(
                                    tileView
                            );

                        }

                    } else {

                        selectedPieceView.set(
                                null
                        );

                    }

                }
        );

    }

    /**
     * Initializer function. Should be used after setting the inner boardStateProperty.
     * Adds change listener to the inner boardStateProperty, redrawing after every change.
     * */
    public void initialize() {

        tileViews = FXCollections.observableList(new ArrayList<>());

        boardStateProperty.addListener(
                (obs, old, val) -> {

                    getChildren().clear();
                    getRowConstraints().clear();
                    getColumnConstraints().clear();

                    tileViews = FXCollections.observableList(
                            new ArrayList<>()
                    );
                    pieceViews = FXCollections.observableMap(
                            new HashMap<>()
                    );

                    for (int i = 0; i < 8; i++) {

                        getRowConstraints().add(
                                new RowConstraints(50)
                        );

                        getColumnConstraints().add(
                                new ColumnConstraints(50)
                        );

                    }

                    for (List<Tile> tileList: val.getTilesProperty().get()) {

                        tileViews.add(FXCollections.observableList(new ArrayList<>()));

                        for (Tile tile: tileList) {

                            TileView tileView = new TileView(this, tile);
                            tileViews.get(tile.getXProperty().get()).add(tileView);

                            // I messed up my coordinate system kind-of, so we display them correctly now
                            add(tileView, tile.getYProperty().get(), 7 - tile.getXProperty().get());

                            tileView.setPrefSize(50, 50);
                            tileView.setMaxSize(50, 50);

                        }

                    }

                    List<PieceView> whitePieceViews = new ArrayList<>();
                    List<PieceView> blackPieceViews = new ArrayList<>();

                    for (Piece whitePiece: val.getPiecesProperty().get().get(PlayerColor.WHITE)) {

                        PieceView pieceView = new PieceView(this, whitePiece);
                        whitePieceViews.add(pieceView);

                        tileViews.get(
                                whitePiece.getTileProperty().get().getXProperty().get()
                        ).get(
                                whitePiece.getTileProperty().get().getYProperty().get()
                        ).getChildren().add(
                                pieceView
                        );

                        pieceView.setPrefSize(50, 50);
                        pieceView.setMaxSize(50, 50);

                    }
                    for (Piece blackPiece: val.getPiecesProperty().get().get(PlayerColor.BLACK)) {

                        PieceView pieceView = new PieceView(this, blackPiece);
                        blackPieceViews.add(pieceView);

                        tileViews.get(
                                blackPiece.getTileProperty().get().getXProperty().get()
                        ).get(
                                blackPiece.getTileProperty().get().getYProperty().get()
                        ).getChildren().add(
                                pieceView
                        );

                        pieceView.setPrefSize(50, 50);
                        pieceView.setMaxSize(50, 50);

                    }

                    pieceViews = FXCollections.observableMap(new HashMap<>());

                    pieceViews.put(PlayerColor.WHITE, whitePieceViews);
                    pieceViews.put(PlayerColor.BLACK, blackPieceViews);

                }
        );

    }
}
