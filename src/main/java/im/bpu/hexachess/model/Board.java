package im.bpu.hexachess.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Board {
	private static final int[][] ROOK_DIRECTIONS = {
		{-1, -1}, {0, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 0}};
	private static final int[][] BISHOP_DIRECTIONS = {
		{-1, -2}, {1, -1}, {2, 1}, {1, 2}, {-1, 1}, {-2, -1}};
	private static final int[][] KNIGHT_OFFSETS = {
		{-2, -3}, {-1, -3}, {3, 1}, {3, 2}, {2, 3}, {1, 3}, {-3, -1}, {-3, -2}};
	private static final int[][] WHITE_PAWN_CAPTURES = {{0, -1}, {-1, 0}};
	private static final int[][] BLACK_PAWN_CAPTURES = {{0, 1}, {1, 0}};
	private static final int[][] KINGS = {{5, 4}};
	private static final int[][] QUEENS = {{4, 5}};
	private static final int[][] ROOKS = {{2, 5}, {5, 2}};
	private static final int[][] BISHOPS = {{3, 3}, {4, 4}, {5, 5}};
	private static final int[][] KNIGHTS = {{3, 5}, {5, 3}};
	private static final int[][] PAWNS = {
		{1, 1}, {1, 2}, {1, 3}, {1, 4}, {1, 5}, {2, 1}, {3, 1}, {4, 1}, {5, 1}};
	public boolean isWhiteTurn = true;
	private AxialCoordinate enPassant;
	final Map<AxialCoordinate, Piece> pieces = new HashMap<>();
	public Board() {
		placeSymmetricPieces(KINGS, PieceType.KING, PieceType.QUEEN);
		placeSymmetricPieces(QUEENS, PieceType.QUEEN, PieceType.KING);
		placeSymmetricPieces(ROOKS, PieceType.ROOK, PieceType.ROOK);
		placeSymmetricPieces(BISHOPS, PieceType.BISHOP, PieceType.BISHOP);
		placeSymmetricPieces(KNIGHTS, PieceType.KNIGHT, PieceType.KNIGHT);
		placeSymmetricPieces(PAWNS, PieceType.PAWN, PieceType.PAWN);
	}
	public Board(final Board other) {
		pieces.putAll(other.pieces);
		isWhiteTurn = other.isWhiteTurn;
		enPassant = other.enPassant;
	}
	public Piece getPiece(final AxialCoordinate coord) {
		return pieces.get(coord);
	}
	private boolean isPromotionCell(final AxialCoordinate pos, final boolean isWhite) {
		final int direction = isWhite ? -1 : 1;
		return !pos.add(direction, direction).isValid();
	}
	public void movePiece(final AxialCoordinate from, final AxialCoordinate to) {
		Piece piece = pieces.remove(from);
		if (piece.type == PieceType.PAWN) {
			if (to.equals(enPassant)) {
				final int direction = piece.isWhite ? 1 : -1;
				pieces.remove(new AxialCoordinate(to.q + direction, to.r + direction));
			}
			final int dr = to.r - from.r;
			enPassant =
				Math.abs(dr) == 2 ? new AxialCoordinate(from.q + dr / 2, from.r + dr / 2) : null;
			if (isPromotionCell(to, piece.isWhite))
				piece = new Piece(PieceType.QUEEN, piece.isWhite);
		} else {
			enPassant = null;
		}
		pieces.put(to, piece);
		isWhiteTurn = !isWhiteTurn;
	}
	private void addStepMoves(final AxialCoordinate pos, final Piece piece, final int[][] offsets,
		final List<Move> moves) {
		for (final int[] offset : offsets) {
			final AxialCoordinate target = pos.add(offset[0], offset[1]);
			if (!target.isValid())
				continue;
			final Piece occupant = pieces.get(target);
			if (occupant == null || occupant.isWhite != piece.isWhite)
				moves.add(new Move(pos, target));
		}
	}
	private void addSlidingMoves(final AxialCoordinate pos, final Piece piece,
		final int[][] directions, final List<Move> moves) {
		for (final int[] direction : directions) {
			AxialCoordinate target = pos.add(direction[0], direction[1]);
			while (target.isValid()) {
				final Piece occupant = pieces.get(target);
				if (occupant == null || occupant.isWhite != piece.isWhite)
					moves.add(new Move(pos, target));
				if (occupant != null)
					break;
				target = target.add(direction[0], direction[1]);
			}
		}
	}
	private boolean isPawnStartCell(final AxialCoordinate pos, final boolean isWhite) {
		final int q = pos.q, r = pos.r;
		return isWhite ? ((q == 1 || r == 1) && q >= 1 && r >= 1)
					   : ((q == -1 || r == -1) && q <= -1 && r <= -1);
	}
	private void addPawnMoves(
		final AxialCoordinate pos, final Piece piece, final List<Move> moves) {
		final int direction = piece.isWhite ? -1 : 1;
		final AxialCoordinate fwd = pos.add(direction, direction);
		if (fwd.isValid() && pieces.get(fwd) == null) {
			moves.add(new Move(pos, fwd));
			if (isPawnStartCell(pos, piece.isWhite)) {
				final AxialCoordinate fwd2 = fwd.add(direction, direction);
				if (fwd2.isValid() && pieces.get(fwd2) == null)
					moves.add(new Move(pos, fwd2));
			}
		}
		for (final int[] offset : piece.isWhite ? WHITE_PAWN_CAPTURES : BLACK_PAWN_CAPTURES) {
			final AxialCoordinate cap = pos.add(offset[0], offset[1]);
			if (cap.isValid()) {
				final Piece target = pieces.get(cap);
				if ((target != null && target.isWhite != piece.isWhite) || cap.equals(enPassant))
					moves.add(new Move(pos, cap));
			}
		}
	}
	public List<Move> getMoves(final AxialCoordinate pos, final Piece piece) {
		final List<Move> moves = new ArrayList<>();
		switch (piece.type) {
			case KING -> {
				addStepMoves(pos, piece, ROOK_DIRECTIONS, moves);
				addStepMoves(pos, piece, BISHOP_DIRECTIONS, moves);
			}
			case QUEEN -> {
				addSlidingMoves(pos, piece, ROOK_DIRECTIONS, moves);
				addSlidingMoves(pos, piece, BISHOP_DIRECTIONS, moves);
			}
			case ROOK -> addSlidingMoves(pos, piece, ROOK_DIRECTIONS, moves);
			case BISHOP -> addSlidingMoves(pos, piece, BISHOP_DIRECTIONS, moves);
			case KNIGHT -> addStepMoves(pos, piece, KNIGHT_OFFSETS, moves);
			case PAWN -> addPawnMoves(pos, piece, moves);
		}
		return moves;
	}
	public List<Move> listMoves(final boolean forWhite) {
		final List<Move> moves = new ArrayList<>();
		pieces.forEach((coord, piece) -> {
			if (piece.isWhite == forWhite)
				moves.addAll(getMoves(coord, piece));
		});
		return moves;
	}
	private void placePiece(final int q, final int r, final PieceType type, final boolean isWhite) {
		pieces.put(new AxialCoordinate(q, r), new Piece(type, isWhite));
	}
	private void placeSymmetricPieces(
		final int[][] positions, final PieceType whiteType, final PieceType blackType) {
		for (final int[] pos : positions) {
			placePiece(pos[0], pos[1], whiteType, true);
			placePiece(-pos[0], -pos[1], blackType, false);
		}
	}
	// 1. Find the king position
	public AxialCoordinate findKing(boolean isWhite) {
		for (AxialCoordinate c : pieces.keySet()) {
			Piece piece = pieces.get(c);
			if (piece.isWhite == isWhite && piece.type == PieceType.KING) {
				return c;
			}
		}
		return null;
	}
	// 2. Is the square attacked by opponent pieces?
	public boolean isSquareAttacked(AxialCoordinate target, boolean byWhite) {
		for (AxialCoordinate c : pieces.keySet()) {
			Piece piece = pieces.get(c);
			if (piece.isWhite == byWhite) {
				List<Move> moves = getMoves(c, piece);
				for (Move m : moves) {
					if (m.to.equals(target)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	public boolean wouldResultInCheck(Move move) {
		Board tempBoard = new Board(this);
		tempBoard.movePiece(move.from, move.to);
		boolean amIWhite = pieces.get(move.from).isWhite;
		AxialCoordinate myKingPos = tempBoard.findKing(amIWhite);
		return tempBoard.isSquareAttacked(myKingPos, !amIWhite);
	}
	public boolean isInCheck(boolean isWhite) {
		AxialCoordinate kingPos = null;
		for (Map.Entry<AxialCoordinate, Piece> entry : pieces.entrySet()) {
			if (entry.getValue().type == PieceType.KING && entry.getValue().isWhite == isWhite) {
				kingPos = entry.getKey();
				break;
			}
		}
		if (kingPos == null)
			return false;
		for (Map.Entry<AxialCoordinate, Piece> entry : pieces.entrySet()) {
			if (entry.getValue().isWhite != isWhite) {
				for (Move move : getMoves(entry.getKey(), entry.getValue())) {
					if (move.to.equals(kingPos))
						return true;
				}
			}
		}
		return false;
	}
	public boolean hasLegalMoves(final boolean isWhite) {
		final List<Move> moves = listMoves(isWhite);
		for (final Move move : moves) {
			final Board clone = new Board(this);
			clone.movePiece(move.from, move.to);
			if (!clone.isInCheck(isWhite))
				return true;
		}
		return false;
	}
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof final Board other))
			return false;
		return isWhiteTurn == other.isWhiteTurn && Objects.equals(enPassant, other.enPassant)
			&& pieces.equals(other.pieces);
	}
	@Override
	public int hashCode() {
		return Objects.hash(isWhiteTurn, enPassant, pieces);
	}
}