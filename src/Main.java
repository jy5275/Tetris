
////////
import java.util.*;
import java.io.*;

////////
public class Main {
	// ####
	public static void main(String[] args) throws IOException, InterruptedException {
		int seed = new Random().nextInt(128);
		Random rand = new Random(seed);
		HW1_1600013239 tetris = new HW1_1600013239();
		int score = tetris.run(rand); // HW对象.run返回得分
		System.out.println(tetris.id + ": score=" + score);
	}
}