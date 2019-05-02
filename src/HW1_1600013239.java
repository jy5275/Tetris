////////
import java.util.*;
import java.io.*;

////////
public class HW1_1600013239 extends Tetris {
	// enter your student id here
	boolean begin = false;	//是否初始化该piece的bestx与bestrot
	int virPiece_x, virPiece_y;
	boolean virBoard[][] = new boolean[h][w];
	boolean virPiece[][] = new boolean[4][4];
	boolean new_virPiece[][] = new boolean[4][4];
	int bestx = 0, bestrot = 0, actrot = 0;

	public String id = new String("1600013239");

	// To see if virPiece could be put at (x=virx, y=h-1)
	boolean validVirPut(int virx){
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				if (!virPiece[y][x]) continue;
				if (virx+x < 0 || virx+x >= w || virBoard[h-1-y][virx+x])
					return false;	 //初位置不可选这儿,返回false
			}
		}
		return true;
	}

	boolean isHole(int x, int y){
		if(virBoard[y][x]) return false;
		if(y>0 && !virBoard[y-1][x]) return false;	//下空
		if(x>0 && !virBoard[y][x-1]) return false;	//左空
		if(x<w-1 && !virBoard[y][x+1]) return false;	//右空
		if(y<h-1 && !virBoard[y+1][x]) return false;	//上空
		return true;
	}

	boolean inWell(int x, int y){
		if(virBoard[y][x]) return false;	//踩在piece上
		if(x>0 && !virBoard[y][x-1]) return false;	//左空
		if(x<w-1 && !virBoard[y][x+1]) return false;	//右空
		return true;
	}

	int contSum(int x){		//求1-x连续和
		if(x==1) return 1;
		return x+contSum(x-1);
	}

//================================================================
	
	boolean drop_virPiece() {
		// 从board中移除旧piece (set false where piece exists)
		for (int x = 0; x < 4; x++) 
			for (int y = 0; y < 4; y++) 
				if (virPiece[y][x]) 
					virBoard[virPiece_y-y][virPiece_x+x] = false;
		
		// Check if newvirPiece is deployable
		int newvirPiece_y = virPiece_y-1;
		boolean deployable = true;
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				if (!virPiece[y][x]) continue;
				if (virPiece_x+x < 0 || virPiece_x+x >= w
				 || newvirPiece_y-y < 0 || newvirPiece_y-y >= h
				 || virBoard[newvirPiece_y-y][virPiece_x+x]) {
					deployable = false;	 //若不可继续掉,就不更virPiece_y
					break;
				}
			}
		}	if (deployable) virPiece_y--;
		
		// 把下降后的virPiece放到virBoard上.
		// If (!deployable), virPiece原封不动
		for (int x = 0; x < 4; x++) 
			for (int y = 0; y < 4; y++) 
				if (virPiece[y][x]) 
					virBoard[virPiece_y-y][virPiece_x+x] = true;
		return deployable;
	}
	
//================================================================
	
	int getSco(){
		int landingHeight = virPiece_y, erodedPieceCellsMetric = 0;
		int boardRowTransitions = 0, boardColTransitions = 0;
		int boardBuriedHoles = 0, boardWells = 0;

		// Reduced erodedPieceCellsMetric
		for (int y = 0; y < h-4; y++) {		// 依次考察每行是否可消
			int x;
			for (x = 0; x<w; x++)
				if (!virBoard[y][x])
					break;
			if(x==w) erodedPieceCellsMetric++;
		}

		boolean inpiece = true;
		for(int y=0; y<h; y++){
			inpiece = true;		// 从左边靠墙开始
			for(int x=0;x<w;x++){
				if(inpiece && !virBoard[y][x]){
					boardRowTransitions++;
					inpiece = false;
				}else if(!inpiece && virBoard[y][x]){
					boardRowTransitions++;
					inpiece = true;
				}
			}
			if(!inpiece) boardRowTransitions++;
		}

		for(int x=0; x<w; x++){
			inpiece = true;		// 从地板开始
			for(int y=0;y<h;y++){
				if(inpiece && !virBoard[y][x]){
					boardColTransitions++;
					inpiece = false;
				}else if(!inpiece && virBoard[y][x]){
					boardColTransitions++;
					inpiece = true;
				}
			}
		}

		for(int y=0; y<h; y++)
			for(int x=0;x<w;x++)
				if(isHole(x,y)) 
					boardBuriedHoles++;

		for(int x=0; x<w; x++){
			boolean preinwell = false;	//前一步在井中？
			int contempty = 0;	//当前所在井的深度
			for(int y=0; y<h; y++){
				if(!preinwell && inWell(x,y)){	//进井
					contempty = 1;
					preinwell = true;
				}
				else if(preinwell && inWell(x,y))	//井中走
					contempty++;
				else if(preinwell && !inWell(x,y)){	//出井
					boardWells += contSum(contempty);
					preinwell = false;
				}
			}
		}

		int Sco = -landingHeight + 2*erodedPieceCellsMetric - 
			boardRowTransitions - 3*boardColTransitions - 
			3*boardBuriedHoles - boardWells;
		return Sco;
	}

//================================================================
	void rotateVirPiece(){
		for (int y = 0; y < 4; y++) 
			for (int x = 0; x < 4; x++)
				new_virPiece[y][x] = virPiece[x][3-y];
		for (int y = 0; y < 4; y++) 
			for (int x = 0; x < 4; x++)
				virPiece[y][x] = new_virPiece[y][x];
	}

	void removeVirPiece(){
		for (int x = 0; x < 4; x++) 
			for (int y = 0; y < 4; y++) 
				if (virPiece_x+x>=0 && virPiece_x+x<w && virPiece[y][x]) 
					virBoard[virPiece_y-y][virPiece_x+x] = false;

	}	

//================================================================

	public PieceOperator robotPlay() {
		int maxsco=-2147483647, tmpsco=0;
		if(piece_y<h-1 || begin==true) {	//已经初始化完毕了
			if(piece_y<h-1) begin = false;
			if(actrot < bestrot) {
				actrot++;
				return PieceOperator.Rotate;
			}
			if(piece_x<bestx) return PieceOperator.ShiftRight;
			if(piece_x>bestx) return PieceOperator.ShiftLeft;
			
			return PieceOperator.Keep;
		}
		actrot = 0;

		// 拷贝board到virBoard
		for(int y=0; y<h; y++)
			for(int x=0; x<w; x++)
				virBoard[y][x]=board[y][x];
		// 拷贝piece到virPiece
		for(int y=0; y<4; y++)
			for(int x=0; x<4; x++)
				virPiece[y][x]=piece[y][x];
		
		// 从virBoard中移除旧piece (set false where virPiece exists)
		for (int x = 0; x < 4; x++) 
			for (int y = 0; y < 4; y++) 
				if (piece[y][x]) 
					virBoard[piece_y-y][piece_x+x] = false;

		for(int tmpx = -1; tmpx<w-1; tmpx++){	//烧脑循环
			for(int rotnum = 0; rotnum<4; rotnum++){

				// 先旋转virPiece
				if(rotnum != 0) 
					rotateVirPiece();

				// 看看能否安放virPiece
				if(!validVirPut(tmpx)) continue;
				// 能以当前姿态放在(x=tmpx, y=h-1)位置,放了再说
				virPiece_y = h-1; virPiece_x=tmpx;
				for (int x = 0; x < 4; x++) 
					for (int y = 0; y < 4; y++) 
						if (virPiece_x+x>=0 && virPiece_x+x<w && virPiece[y][x]) 
							virBoard[virPiece_y-y][virPiece_x+x] = true;

				while(drop_virPiece());		//掉到底
				tmpsco = getSco();
				if(tmpsco>maxsco){
					bestx = tmpx;
					bestrot = rotnum;
					maxsco = tmpsco;
				}
				removeVirPiece();
			}
			rotateVirPiece();
		}
		begin = true;
		return PieceOperator.Keep;
	}
}