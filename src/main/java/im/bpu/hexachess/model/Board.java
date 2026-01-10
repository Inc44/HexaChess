package im.bpu.hexachess.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	final Map<AxialCoordinate, Piece> pieces = new HashMap<>();
	public boolean isWhiteTurn = true;
	private AxialCoordinate enPassant;
	public Board() {
		placeSymmetricPieces(KINGS, PieceType.KING, PieceType.QUEEN);
		placeSymmetricPieces(QUEENS, PieceType.QUEEN, PieceType.KING);
		placeSymmetricPieces(ROOKS, PieceType.ROOK, PieceType.ROOK);
		placeSymmetricPieces(BISHOPS, PieceType.BISHOP, PieceType.BISHOP);
		placeSymmetricPieces(KNIGHTS, PieceType.KNIGHT, PieceType.KNIGHT);
		placeSymmetricPieces(PAWNS, PieceType.PAWN, PieceType.PAWN);
	}
	public Board(Board other) {
		pieces.putAll(other.pieces);
		isWhiteTurn = other.isWhiteTurn;
		enPassant = other.enPassant;
	}
	public Piece getPiece(AxialCoordinate coord) {
		return pieces.get(coord);
	}
	private boolean isPromotionCell(AxialCoordinate pos, boolean isWhite) {
		int direction = isWhite ? -1 : 1;
		return !pos.add(direction, direction).isValid();
	}
	public void movePiece(AxialCoordinate from, AxialCoordinate to) {
		Piece piece = pieces.remove(from);
		if (piece.type == PieceType.PAWN) {
			if (to.equals(enPassant)) {
				int direction = piece.isWhite ? 1 : -1;
				pieces.remove(new AxialCoordinate(to.q + direction, to.r + direction));
			}
			int dr = to.r - from.r;
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
	private void addStepMoves(AxialCoordinate pos, Piece piece, int[][] offsets, List<Move> moves) {
		for (int[] offset : offsets) {
			AxialCoordinate target = pos.add(offset[0], offset[1]);
			if (!target.isValid())
				continue;
			Piece occupant = pieces.get(target);
			if (occupant == null || occupant.isWhite != piece.isWhite)
				moves.add(new Move(pos, target));
		}
	}
	private void addSlidingMoves(
		AxialCoordinate pos, Piece piece, int[][] directions, List<Move> moves) {
		for (int[] direction : directions) {
			AxialCoordinate target = pos.add(direction[0], direction[1]);
			while (target.isValid()) {
				Piece occupant = pieces.get(target);
				if (occupant == null || occupant.isWhite != piece.isWhite)
					moves.add(new Move(pos, target));
				if (occupant != null)
					break;
				target = target.add(direction[0], direction[1]);
			}
		}
	}
	private boolean isPawnStartCell(AxialCoordinate pos, boolean isWhite) {
		int q = pos.q, r = pos.r;
		return isWhite ? ((q == 1 || r == 1) && q >= 1 && r >= 1)
					   : ((q == -1 || r == -1) && q <= -1 && r <= -1);
	}
	private void addPawnMoves(AxialCoordinate pos, Piece piece, List<Move> moves) {
		int direction = piece.isWhite ? -1 : 1;
		AxialCoordinate fwd = pos.add(direction, direction);
		if (fwd.isValid() && pieces.get(fwd) == null) {
			moves.add(new Move(pos, fwd));
			if (isPawnStartCell(pos, piece.isWhite)) {
				AxialCoordinate fwd2 = fwd.add(direction, direction);
				if (fwd2.isValid() && pieces.get(fwd2) == null)
					moves.add(new Move(pos, fwd2));
			}
		}
		for (int[] offset : piece.isWhite ? WHITE_PAWN_CAPTURES : BLACK_PAWN_CAPTURES) {
			AxialCoordinate cap = pos.add(offset[0], offset[1]);
			if (!cap.isValid())
				continue;
			Piece target = pieces.get(cap);
			if ((target != null && target.isWhite != piece.isWhite) || cap.equals(enPassant))
				moves.add(new Move(pos, cap));
		}
	}
	private List<Move> getMoves(AxialCoordinate pos, Piece piece) {
		List<Move> moves = new ArrayList<>();
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
	public List<Move> listMoves(boolean forWhite) {
		List<Move> moves = new ArrayList<>();
		pieces.forEach((coord, piece) -> {
			if (piece.isWhite == forWhite)
				moves.addAll(getMoves(coord, piece));
		});
		return moves;
	}
	private void placePiece(int q, int r, PieceType type, boolean isWhite) {
		pieces.put(new AxialCoordinate(q, r), new Piece(type, isWhite));
	}
	private void placeSymmetricPieces(int[][] positions, PieceType whiteType, PieceType blackType) {
		for (int[] pos : positions) {
			placePiece(pos[0], pos[1], whiteType, true);
			placePiece(-pos[0], -pos[1], blackType, false);
		}
	}
}