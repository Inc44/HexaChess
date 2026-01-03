package im.bpu.hexachess;

import im.bpu.hexachess.model.Board;

import java.util.Stack;

public class State {
	private static State state;
	public Board board;
	public Stack<Board> history;
	public boolean isWhitePlayer = true;
	public boolean isMultiplayer = false;
	public String gameId = null;
	private State() {
		clear();
	}
	public static State getState() {
		if (state == null) {
			state = new State();
		}
		return state;
	}
	public void clear() {
		board = new Board();
		history = new Stack<>();
		isWhitePlayer = true;
		isMultiplayer = false;
		gameId = null;
	}
}