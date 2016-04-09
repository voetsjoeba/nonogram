package com.voetsjoeba.nonogram.event;

import java.util.EventListener;

public interface SquareStateSetListener extends EventListener {
	public void squareStateSet(SquareStateSetEvent e);
}
