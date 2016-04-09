package com.voetsjoeba.nonogram;

import com.voetsjoeba.nonogram.puzzle.logicart47.ArcobalenoPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.BalloPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.DragoPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.EgittoPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.FerrariPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.FioriPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.GattiPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.GrammofonoPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.KaratePuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.LeccaLeccaPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.MaisPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.PastorePuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.PellicolaPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.PianistaPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.SpiaggiaPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.UccellinoPuzzle;

public class LogicArt47PuzzleRegressionTest extends PuzzleTestCase {
	
	public void testArcobaleno() throws Exception {
		solvePuzzle(new ArcobalenoPuzzle());
	}
	
	public void testEgitto() throws Exception {
		solvePuzzle(new EgittoPuzzle());
	}
	
	public void testFiori() throws Exception {
		solvePuzzle(new FioriPuzzle());
	}
	
	public void testGrammofono() throws Exception {
		solvePuzzle(new GrammofonoPuzzle());
	}
	
	public void testPastore() throws Exception {
		solvePuzzle(new PastorePuzzle());
	}
	
	public void testLeccaLecca() throws Exception {
		solvePuzzle(new LeccaLeccaPuzzle());
	}
	
	public void testFerrari() throws Exception {
		solvePuzzle(new FerrariPuzzle());
	}
	
	public void testMais() throws Exception {
		solvePuzzle(new MaisPuzzle());
	}
	
	public void testGatti() throws Exception {
		solvePuzzle(new GattiPuzzle());
	}
	
	public void testBallo() throws Exception {
		solvePuzzle(new BalloPuzzle());
	}
	
	public void testDrago() throws Exception {
		solvePuzzle(new DragoPuzzle());
	}
	
	public void testUccellino() throws Exception {
		solvePuzzle(new UccellinoPuzzle());
	}
	
	public void testKarate() throws Exception {
		solvePuzzle(new KaratePuzzle());
	}
	
	public void testPianista() throws Exception {
		solvePuzzle(new PianistaPuzzle());
	}
	
	public void testSpiaggia() throws Exception {
		solvePuzzle(new SpiaggiaPuzzle());
	}
	
	public void testPellicola() throws Exception {
		solvePuzzle(new PellicolaPuzzle());
	}
	
}
