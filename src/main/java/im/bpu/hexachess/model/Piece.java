package im.bpu.hexachess.model;

import java.util.Objects;

public class Piece {
	public final PieceType type;
	public final boolean isWhite;
	public Piece(PieceType type, boolean isWhite) {
		this.type = type;
		this.isWhite = isWhite;
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