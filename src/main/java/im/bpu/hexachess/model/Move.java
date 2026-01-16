package im.bpu.hexachess.model;

public class Move {
	public final AxialCoordinate from;
	public final AxialCoordinate to;
	public Move(AxialCoordinate from, AxialCoordinate to) {
		this.from = from;
		this.to = to;
	}
}