package hu.aberci.entities.data;

import hu.aberci.entities.interfaces.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * A Serializable version of {@link PieceImpl}
 * */
@Data
@Accessors(chain = true)
public class SerializablePieceImpl implements SerializablePiece {

    SerializableTile tile;
    Integer ID;
    PlayerColor playerColor;
    PieceType pieceType;

}
