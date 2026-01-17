package im.bpu.hexachess.ui;

import im.bpu.hexachess.SettingsManager;
import im.bpu.hexachess.State;
import im.bpu.hexachess.model.AxialCoordinate;
import im.bpu.hexachess.model.Board;
import im.bpu.hexachess.model.Piece;

import java.util.List;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
// import javafx.scene.paint.Paint;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
// import javafx.scene.layout.Region;
// import javafx.scene.Parent;

class HexRenderer {
	private static final Color SANDYBROWN = Color.rgb(232, 171, 111);
	private static final Color NAVAJOWHITE = Color.rgb(255, 206, 158);
	private static final Color PERU = Color.rgb(209, 139, 71);
	private static final Color LEGOYELLOW = Color.rgb(255, 215, 0, 0.63);
	private static final Color RED = Color.rgb(237, 53, 36, 0.63);
	private static final Color GREEN = Color.rgb(46, 218, 119, 0.63);
	private static final Color[] HEX_COLORS = {SANDYBROWN, NAVAJOWHITE, PERU};
	private static final int[][] HEX_NEIGHBOR_OFFSETS = {
		{-1, -1}, {0, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 0}};
	private static final double PIECE_IMAGE_SCALE = 1.5;
	private static final double FONT_SIZE_SCALE = 0.666;
	private static final int TEXT_Y_OFFSET = 1;
	private static final int BORDER_LINE_WIDTH = 3;
	private static final int RADIUS = 5;
	private final HexGeometry geometry;
	private Board board;
	HexRenderer(final HexGeometry geometry, final Board board) {
		this.geometry = geometry;
		this.board = board;
	}
	private void drawPieceImage(
		final GraphicsContext gc, final double x, final double y, final Image image) {
		final double size = geometry.getHexSize() * PIECE_IMAGE_SCALE;
		final double offset = size / 2;
		gc.drawImage(image, x - offset, y - offset, size, size);
	}
	private void drawPieceFallback(
		final GraphicsContext gc, final double x, final double y, final Piece piece) {
		final double size = geometry.getHexSize();
		final double offset = size / 2;
		gc.setFill(piece.isWhite ? Color.WHITE : Color.BLACK);
		gc.fillOval(x - offset, y - offset, size, size);
		gc.setStroke(piece.isWhite ? Color.BLACK : Color.WHITE);
		gc.setLineWidth(2);
		gc.strokeOval(x - offset, y - offset, size, size);
		gc.setFill(piece.isWhite ? Color.BLACK : Color.WHITE);
		gc.setFont(Font.font(size * FONT_SIZE_SCALE));
		final String label = String.valueOf(Character.toUpperCase(piece.type.code));
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setTextBaseline(VPos.CENTER);
		gc.fillText(label, x, y - TEXT_Y_OFFSET);
	}
	private void drawPiece(
		final GraphicsContext gc, final double x, final double y, final Piece piece) {
		final Image image = PieceImageLoader.get((piece.isWhite ? "w" : "b") + piece.type.code);
		if (PieceImageLoader.isLoaded())
			drawPieceImage(gc, x, y, image);
		else
			drawPieceFallback(gc, x, y, piece);
	}
	private void drawCoordinates(
		final GraphicsContext gc, final double x, final double y, final AxialCoordinate coord) {
		final double size = geometry.getHexSize();
		gc.setFill(Color.RED);
		gc.setFont(Font.font(size * FONT_SIZE_SCALE));
		final String label = coord.q + "," + coord.r;
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setTextBaseline(VPos.CENTER);
		gc.fillText(label, x, y - TEXT_Y_OFFSET);
	}
	private void drawPath(final GraphicsContext gc, final Path path) {
		gc.beginPath();
		for (final PathElement elem : path.getElements()) {
			if (elem instanceof MoveTo move)
				gc.moveTo(move.getX(), move.getY());
			else if (elem instanceof LineTo line)
				gc.lineTo(line.getX(), line.getY());
			else if (elem instanceof ClosePath)
				gc.closePath();
		}
	}
	void drawHex(final GraphicsContext gc, final double cx, final double cy,
		final AxialCoordinate coord, final AxialCoordinate selected,
		final List<AxialCoordinate> highlighted, final AxialCoordinate kingInCheck) {
		final Point2D center = geometry.hexToPixel(coord.q, coord.r, cx, cy);
		final Path hexPath = geometry.createHexPath(center);
		gc.setFill(HEX_COLORS[Math.floorMod(coord.q + coord.r, 3)]);
		drawPath(gc, hexPath);
		gc.fill();
		if (coord.equals(selected)) {
			gc.setFill(LEGOYELLOW);
			gc.fill();
		} else if (kingInCheck != null && coord.equals(kingInCheck)) {
			gc.setFill(RED);
			gc.fill();
		} else if (highlighted.contains(coord)) {
			gc.setFill(GREEN);
			gc.fill();
		}
		final Piece piece = board.getPiece(coord);
		if (piece != null)
			drawPiece(gc, center.getX(), center.getY(), piece);
		if (State.getState().isDeveloperMode)
			drawCoordinates(gc, center.getX(), center.getY(), coord);
	}
	private void drawCellBorder(
		final GraphicsContext gc, final double cx, final double cy, final int q, final int r) {
		final AxialCoordinate coord = new AxialCoordinate(q, r);
		if (!coord.isValid())
			return;
		final Point2D center = geometry.hexToPixel(q, r, cx, cy);
		final double radius = geometry.getHexSize();
		for (int i = 0; i < 6; i++) {
			final AxialCoordinate neighbor =
				new AxialCoordinate(q + HEX_NEIGHBOR_OFFSETS[i][0], r + HEX_NEIGHBOR_OFFSETS[i][1]);
			if (neighbor.isValid())
				continue;
			final int v1 = (i + 4) % 6;
			final int v2 = (i + 5) % 6;
			final double x1 = center.getX() + radius * HexGeometry.HEX_COS[v1];
			final double y1 = center.getY() + radius * HexGeometry.HEX_SIN[v1];
			final double x2 = center.getX() + radius * HexGeometry.HEX_COS[v2];
			final double y2 = center.getY() + radius * HexGeometry.HEX_SIN[v2];
			gc.strokeLine(x1, y1, x2, y2);
		}
	}
	/*
	private Color getSceneBackgroundColor(GraphicsContext gc) {
		if (gc.getCanvas().getScene() == null)
			return Color.WHITE;
		Parent root = gc.getCanvas().getScene().getRoot();
		if (root instanceof Region region && region.getBackground() != null
			&& !region.getBackground().getFills().isEmpty()) {
			Paint fill = region.getBackground().getFills().get(0).getFill();
			if (fill instanceof Color color)
				return color;
		}
		return Color.WHITE;
	}
	private double calculateHslLightness(Color color) {
		double red = color.getRed();
		double green = color.getGreen();
		double blue = color.getBlue();
		double max = Math.max(red, Math.max(green, blue));
		double min = Math.min(red, Math.min(green, blue));
		return (max + min) / 2.0;
	}
	*/
	void drawBoardBorder(final GraphicsContext gc, final double cx, final double cy) {
		/*
		Color backgroundColor = getSceneBackgroundColor(gc);
		double backgroundColorLightness = calculateHslLightness(backgroundColor);
		gc.setStroke(backgroundColorLightness > 0.5 ? Color.BLACK : Color.WHITE);
		*/
		final String theme = SettingsManager.theme;
		final Color borderColor = theme.equals("Light") ? Color.BLACK : Color.WHITE;
		gc.setStroke(borderColor);
		gc.setLineWidth(BORDER_LINE_WIDTH);
		for (int q = -RADIUS; q <= RADIUS; q++)
			for (int r = -RADIUS; r <= RADIUS; r++) drawCellBorder(gc, cx, cy, q, r);
	}
	void setBoard(final Board board) {
		this.board = board;
	}
}