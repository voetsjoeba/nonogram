package com.voetsjoeba.nonogram;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import com.voetsjoeba.nonogram.algorithm.Solver;
import com.voetsjoeba.nonogram.exception.UnsolvablePuzzleException;
import com.voetsjoeba.nonogram.puzzle.logicart3.MonumentiNaturaliPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.BalloPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.DragoPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.FerrariPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.GattiPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.KaratePuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.LeccaLeccaPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.MaisPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.PellicolaPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.PianistaPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.SpiaggiaPuzzle;
import com.voetsjoeba.nonogram.puzzle.logicart47.UccellinoPuzzle;
import com.voetsjoeba.nonogram.puzzle.webpbn.SwingPuzzle;
import com.voetsjoeba.nonogram.structure.api.Puzzle;
import com.voetsjoeba.nonogram.ui.StandardUiField;

/**
 * Main class for the nonogram solver.
 * 
 * @author Jeroen De Ridder
 */
public class Nonogram {
	
	private static final int containerPanelSpacing = 20;
	
	static {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception ex){
			
		}
		
	}
	
	public static void main(String[] args){
		
		Puzzle puzzle;
		//puzzle = new GrammofonoPuzzle();
		//puzzle = new EgittoPuzzle();
		//puzzle = new DandyPuzzle();
		//puzzle = new MezzoBustoPuzzle();
		//puzzle = new SalperaPuzzle();
		//puzzle = new ECattivoPuzzle();
		
		//puzzle = new CoppiaDiComiciPuzzle();
		//puzzle = new FioriPuzzle();
		//puzzle = new ArcobalenoPuzzle();
		//puzzle = new PastorePuzzle();
		//puzzle = new LupoCativoPuzzle();
		//puzzle = new ImmersionePuzzle();
		//puzzle = new BuonoPuzzle();
		//puzzle = new AbracadabraPuzzle();
		
		//puzzle = new InPartenzaPuzzle();
		//puzzle = new ZonaVulcanicaPuzzle();
		//puzzle = new MyHeartWillGoOnPuzzle();
		//puzzle = new HastaSiemprePuzzle();
		//puzzle = new BiondaEFamosaPuzzle();
		puzzle = new MonumentiNaturaliPuzzle(); // TODO: can't be solved 100%
		//puzzle = new PersonaggioDaFavolaPuzzle();
		
		puzzle = new LeccaLeccaPuzzle();
		puzzle = new FerrariPuzzle();
		puzzle = new MaisPuzzle();
		puzzle = new GattiPuzzle();
		puzzle = new BalloPuzzle();
		puzzle = new DragoPuzzle();
		puzzle = new UccellinoPuzzle();
		puzzle = new KaratePuzzle();
		puzzle = new PianistaPuzzle();
		puzzle = new SpiaggiaPuzzle();
		puzzle = new PellicolaPuzzle();
		
		//puzzle = new BalancePuzzle();
		//puzzle = new ArtistPuzzle();
		//puzzle = new TirPuzzle();
		
		//puzzle = new DancerPuzzle();
		//puzzle = new CatPuzzle();
		//puzzle = new SkidPuzzle();
		//puzzle = new BucksPuzzle(); // TODO: can't be solved 100%
		//puzzle = new EdgePuzzle();
		//puzzle = new KnotPuzzle();
		puzzle = new SwingPuzzle();
		
		//puzzle = new Puzzle63();
		
		new Nonogram(puzzle);
		
	}
	
	public Nonogram(Puzzle puzzle){
		
		StandardUiField uiField = new StandardUiField(puzzle);
		
		JPanel containerPanel = new JPanel();
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setBorder(BorderFactory.createEmptyBorder(containerPanelSpacing, containerPanelSpacing, containerPanelSpacing, containerPanelSpacing));
		containerPanel.add(uiField);
		
		JFrame frame = new JFrame("Nonogram");
		frame.add(new JScrollPane(containerPanel));
		frame.pack();
		//frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		Solver solver = new Solver(puzzle, uiField);
		
		try {
			solver.solve();
		}
		catch(UnsolvablePuzzleException upex){
			throw upex;
		}
		
	}
	
}
