package hu.aberci.entities.interfaces;

import java.io.Serializable;

/**
 * Serializable version of {@link Piece}.
 * */
public interface SerializablePiece extends Serializable {

    SerializableTile getTile();

    PieceType getPieceType();

    PlayerColor getPlayerColor();

    Integer getID();

    SerializablePiece setTile(SerializableTile serializableTile);
    SerializablePiece setPieceType(PieceType pieceType);
    SerializablePiece setPlayerColor(PlayerColor playerColor);
    SerializablePiece setID(Integer id);

}
