package com.voetsjoeba.nonogram;


import com.voetsjoeba.nonogram.puzzle.logicart3.AbracadabraPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.BiondaEFamosaPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.BuonoPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.CoppiaDiComiciPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.DandyPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.ECattivoPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.HastaSiemprePuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.ImmersionePuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.InPartenzaPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.LupoCativoPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.MezzoBustoPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.MonumentiNaturaliPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.MyHeartWillGoOnPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.PersonaggioDaFavolaPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.SalperaPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart3.ZonaVulcanicaPuzzle;

public class LogicArt3PuzzleRegressionTest extends PuzzleTestCase {
	
	// small puzzles
	
	public void testDandy() throws Exception {
		solvePuzzle(new DandyPuzzle());
	}
	
	public void testMezzoBusto() throws Exception {
		solvePuzzle(new MezzoBustoPuzzle());
	}
	
	public void testSalpera() throws Exception {
		solvePuzzle(new SalperaPuzzle());
	}
	
	public void testECattivo() throws Exception {
		solvePuzzle(new ECattivoPuzzle());
	}
	
	public void testCoppiaDiComiciPuzzle() throws Exception {
		solvePuzzle(new CoppiaDiComiciPuzzle());
	}
	
	// medium puzzles
	
	public void testCoppiaDiComici() throws Exception {
		solvePuzzle(new CoppiaDiComiciPuzzle());
	}
	
	public void testLupoCattivo() throws Exception {
		solvePuzzle(new LupoCativoPuzzle());
	}
	
	public void testImmersione() throws Exception {
		solvePuzzle(new ImmersionePuzzle());
	}
	
	public void testAbracadabra() throws Exception {
		solvePuzzle(new AbracadabraPuzzle());
	}
	
	public void testPersonaggioDaFavola() throws Exception {
		solvePuzzle(new PersonaggioDaFavolaPuzzle());
	}
	
	public void testBuono() throws Exception {
		solvePuzzle(new BuonoPuzzle());
	}
	
	// large puzzles
	
	public void testInPartenza() throws Exception {
		solvePuzzle(new InPartenzaPuzzle());
	}
	
	public void testMonumentiNaturali() throws Exception {
		solvePuzzle(new MonumentiNaturaliPuzzle());
	}
	
	public void testZonaVulcanica() throws Exception {
		solvePuzzle(new ZonaVulcanicaPuzzle());
	}
	
	public void testMyHeartWillGoOn() throws Exception {
		solvePuzzle(new MyHeartWillGoOnPuzzle());
	}
	
	public void testHastaSiempre() throws Exception {
		solvePuzzle(new HastaSiemprePuzzle());
	}
	
	public void testBiondaEFamosa() throws Exception {
		solvePuzzle(new BiondaEFamosaPuzzle());
	}
	
	// fancy puzzles
	
}
