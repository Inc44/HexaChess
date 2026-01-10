package im.bpu.hexachess.model;

public enum PieceType {
	KING(200, 'k'),
	QUEEN(9, 'q'),
	ROOK(5, 'r'),
	BISHOP(3, 'b'),
	KNIGHT(3, 'n'),
	PAWN(1, 'p');
	public final int value;
	public final char code;
	PieceType(int value, char code) {
		this.value = value;
		this.code = code;
	}
}