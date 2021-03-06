package net.tofweb.starlite;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CostBlockManagerTest {

	private CellSpace space;

	@Before
	public void setup() {
		space = new CellSpace();
		space.setGoalCell(10, 10, 10);
		space.setStartCell(-5, -5, -5);
	}

	@Test
	public void test() {
		CostBlockManager manager = new CostBlockManager(space);
		Cell cell = space.makeNewCell(3, 3, 3);
		assertFalse(manager.isBlocked(cell));

		CellInfo returnedInfo = space.getInfo(cell);
		assertNotNull(returnedInfo);
		assertTrue(1.0 == returnedInfo.getCost());
	}

	@Test
	public void testBlocked() {
		CostBlockManager manager = new CostBlockManager(space);
		Cell cell = space.makeNewCell(3, 3, 3);
		manager.blockCell(cell);
		assertTrue(manager.isBlocked(cell));

		CellInfo returnedInfo = space.getInfo(cell);
		assertNotNull(returnedInfo);
		assertTrue(-1.0 == returnedInfo.getCost());
	}
}
