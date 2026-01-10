package im.bpu.hexachess.model;

public class Piece {
	public final PieceType type;
	public final boolean isWhite;
	Piece(PieceType type, boolean isWhite) {
		this.type = type;
		this.isWhite = isWhite;
	}
}