package im.bpu.hexachess.model;
import java.util.List;
import java.util.ArrayList;

public class Piece {
	public final PieceType type;
	public final boolean isWhite;
	public Piece(PieceType type, boolean isWhite) {
		this.type = type;
		this.isWhite = isWhite;
	}

	public ArrayList<AxialCoordinate> getPossibleMoves(Board board, AxialCoordinate position) {
        List<Move> legalMoves = board.getMoves(position, this);
        ArrayList<AxialCoordinate> destinations = new ArrayList<>();
        for (Move m : legalMoves) {
            destinations.add(m.to);
        }
        
        return destinations;
    }
}