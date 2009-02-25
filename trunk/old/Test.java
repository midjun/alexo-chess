package old;

import model.Board;

import java.io.*;
public class Test
{
	public static void main(String[] args) throws IOException
	{
		Board board = new Board();
		board.setupStart();
		
		Engine.search(board, 8, true, 0, 0, 0, true);
		
	}
}
