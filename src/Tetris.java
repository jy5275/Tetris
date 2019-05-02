
////////
import java.util.*;
import java.io.*;

////////
public class Tetris {
	public boolean enableDisplay = true; // 是否启动显示功能
	public int displayRefreshInterval = 2; // 刷新速率

	// 高, 宽, 用来缓存新生成方块的行数
	public static final int h = 24, w = 10, nBufferLines = 4;
	public boolean board[][], piece[][]; // 方块定义，大小为4x4

	public int piece_x, piece_y; // 方块【左上角】的坐标

	public int getPieceX() {
		return piece_x;
	}

	public int getPieceY() {
		return piece_y;
	}

	public boolean hasPiece; // 当前是否有正在坠落的方块？

	public int score;

	public enum PieceOperator {
		ShiftLeft, ShiftRight, Rotate, Drop, Keep
	}

	Tetris() {
		board = new boolean[h][w];
	}

	// 返回当前场地状况
	public boolean[][] getBoard() {
		boolean tmp[][] = new boolean[h][w];
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				tmp[y][x] = board[y][x];
		return tmp;
	}

	// 返回当前方块状况
	public boolean[][] getPiece() {
		boolean tmp[][] = new boolean[4][4];
		for (int y = 0; y < 4; y++)
			for (int x = 0; x < 4; x++)
				tmp[y][x] = piece[y][x];
		return tmp;
	}

	// Generate a random piece
	public void initPiece(Random rand) {
		switch (rand.nextInt(7)) {
		case 0: // 【横条】
			piece = new boolean[][] { { false, false, false, false }, { true, true, true, true },
					{ false, false, false, false }, { false, false, false, false } };
			break;
		case 1: // 【z字形】
			piece = new boolean[][] { { false, false, false, false }, { true, true, false, false },
					{ false, true, true, false }, { false, false, false, false } };
			break;
		case 2: // 【反z字形】
			piece = new boolean[][] { { false, false, false, false }, { false, false, true, true },
					{ false, true, true, false }, { false, false, false, false } };
			break;
		case 3: // 【L形】
			piece = new boolean[][] { { false, false, false, false }, { true, true, true, false },
					{ true, false, false, false }, { false, false, false, false } };
			break;
		case 4: // 【反L形】
			piece = new boolean[][] { { false, false, false, false }, { false, true, true, true },
					{ false, false, false, true }, { false, false, false, false } };
			break;
		case 5: // 【陕西形】
			piece = new boolean[][] { { false, false, false, false }, { true, true, true, false },
					{ false, true, false, false }, { false, false, false, false } };
			break;
		case 6: // 【实心正方形】
			piece = new boolean[][] { { false, false, false, false }, { false, true, true, false },
					{ false, true, true, false }, { false, false, false, false } };
			break;
		}

		// deploy, 部署到board, 最初产生在中间
		piece_y = h - 1;
		piece_x = w / 2;
		for (int y = 0; y < 4; y++)
			for (int x = 0; x < 4; x++)
				if (piece[y][x])
					board[piece_y - y][piece_x + x] = true;
	}

	// ####
	public boolean movePiece(PieceOperator op) {
		// 从board中移除旧piece (set false where piece exists)
		for (int x = 0; x < 4; x++)
			for (int y = 0; y < 4; y++)
				if (piece[y][x])
					board[piece_y - y][piece_x + x] = false;

		// 产生新piece
		int new_piece_x = piece_x, new_piece_y = piece_y;
		boolean new_piece[][] = new boolean[4][4];
		for (int y = 0; y < 4; y++)
			for (int x = 0; x < 4; x++)
				new_piece[y][x] = piece[y][x];

		// Piece operation
		switch (op) { // 左? 右? 落? 转pi/2?
		case ShiftLeft:
			new_piece_x--;
			break;
		case ShiftRight:
			new_piece_x++;
			break;
		case Drop:
			new_piece_y--;
			break;
		case Rotate:
			for (int y = 0; y < 4; y++)
				for (int x = 0; x < 4; x++)
					new_piece[y][x] = piece[x][3 - y];
			break;
		}

		// Check if new_piece is deployable
		boolean deployable = true;
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				if (!new_piece[y][x])
					continue;
				if (new_piece_x + x < 0 || new_piece_x + x >= w || new_piece_y - y < 0 || new_piece_y - y >= h
						|| board[new_piece_y - y][new_piece_x + x]) {
					deployable = false; // 一旦不可继续Drop,返回false
					break;
				}
			}
		}

		if (deployable) { // 仅在deployable时, 把piece换成new_piece
			piece_x = new_piece_x;
			piece_y = new_piece_y;
			for (int y = 0; y < 4; y++)
				for (int x = 0; x < 4; x++)
					piece[y][x] = new_piece[y][x];
		}
		// 把piece放到board上. If (!deployable), piece原封不动
		for (int x = 0; x < 4; x++)
			for (int y = 0; y < 4; y++)
				if (piece[y][x])
					board[piece_y - y][piece_x + x] = true;

		return deployable;
	}

	/* 管理方块的下落和消行 return false if game over */
	public boolean updateBoard() {
		if (hasPiece)
			return true;

		// piece has landed, update board
		for (int y = h - nBufferLines; y < h; y++)
			for (int x = 0; x < w; x++)
				if (board[y][x]) // game over
					return false;

		// 依次考察每行是不是可消
		for (int y = 0; y < h - nBufferLines; y++) {
			boolean full = true;
			for (int x = 0; x < w; x++)
				if (!board[y][x]) {
					full = false;
					break;
				}
			if (full) {
				for (int i = y; i < h - nBufferLines; i++)
					for (int j = 0; j < w; j++) {
						board[i][j] = board[i + 1][j];
						board[i + 1][j] = false;
					}
				score++;
				y--;
			}
		}
		return true;
	}

	// 显示方块
	public final void displayBoard() throws IOException, InterruptedException {
		// clear screen, OS dependent
		new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
		for (int y = h - 1; y >= 0; y--) { // 从上往下print,头四行没墙
			System.out.printf("%s", y < h - 4 ? "<!" : "  ");
			for (int x = 0; x < w; x++) // 有就显示'H',没就显示' '
				System.out.printf("%c", board[y][x] ? 'H' : ' ');
			System.out.printf("%s\n", y < h - 4 ? "!>" : "  ");
		}
		System.out.printf("<!");
		for (int x = 0; x < w; x++)
			System.out.printf("=");
		System.out.printf("!>\n");
		System.out.println("\nscore = " + score + "\n");
		System.out.flush();
	}

	// 继承之前的robotPlay, 一次性Drop到底
	public PieceOperator robotPlay() {
		return PieceOperator.Keep;
	}

	// 入口函数
	public final int run(Random rand) throws IOException, InterruptedException {
		score = 0;
		hasPiece = false;
		int dropAlarm = 0;
		while (updateBoard()) {
			if (!hasPiece) { // 刚刚开局或landed会进入这一行
				initPiece(rand);
				hasPiece = true;
			}
			movePiece(robotPlay());
			dropAlarm = (dropAlarm + 1) % 5;
			if (dropAlarm == 0) { // 每5次Loop下落一格
				boolean landed = !movePiece(PieceOperator.Drop);
				if (landed)
					hasPiece = false;
			}
			displayBoard();
			Thread.sleep(displayRefreshInterval);
		}
		return score;
	}
}