package im.bpu.hexachess.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof final Piece other))
			return false;
		return type == other.type && isWhite == other.isWhite;
	}
	@Override
	public int hashCode() {
		return Objects.hash(type, isWhite);
	}
}