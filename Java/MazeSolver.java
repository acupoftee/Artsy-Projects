package net.bethydiakabana.maze;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;

/**
 * Creates a random maze, then solves it by finding a path from the upper left corner
 * to the lower right corner. It generates a new maze after solving one maze.
 * @author Bethy Diakabana
 * @since 25/3/2016
 *
 */
//TODO add accessor and mutators for fields used in applet tag
public class MazeSolver extends Applet implements Runnable {
	
	/**
	 * Generated for serialization purposes
	 */
	private static final long serialVersionUID = 9105953388755986036L;

	/**
	 * A maze is made up of walls and corridors. <tt>maze[i][j]</tt> 
	 * A cell is either part of a wall or part of a corridor. A cell is represented 
	 * by the followingg constants:
	 * <ul>
	 * <li><tt>pathCode</tt> - a cell that is a part of a corridor if it is part of the current path 
	 * through the maze</li>
	 * <li><tt>visitedCode</tt>  - a cell that has already been explored without finding a solution</li>
	 * <li><tt>emptyCode</tt> - if the solver has not traversed through the cell</li>
	 */
	private int[][] maze;
	
	/**
	 * {@link Color} array that is associated with the <tt>backgroundCode</tt>, <tt>wallCode</tt>, 
	 * <tt>pathCode</tt>, <tt>emptyCode</tt>, <tt>visitedCode</tt>
	 */
	private Color[] mazeColors = new Color[5];
	
	/**
	 * Sets to true when {@link maze} is valid. This is used in 
	 * <tt>redrawMaze()</tt>, and is set to true in <tt>createMaze()</tt>.
	 * Resets to <tt>false</tt> when
	 */
	private boolean mazeExists = false;
	
	/**
	 * Thread for creating and solving the maze
	 */
	private Thread mazeThread;
	
	/**
	 * Used by the applet to control the maze thread
	 */
	private int status = 0;
	
	private final static int backgroundCode = 0;
	private final static int wallCode = 1;
	private final static int pathCode = 2;
	private final static int emptyCode = 3;
	private final static int visitedCode = 4;

	private int rows = 21;
	private int columns = 21;
	private int borderPixels = 0;
	private int sleepTime = 5000;
	private int speedSleep = 50;

	private int width = -1;
	private int height = -1;
	private int totalWidth;
	private int totalHeight;
	private int left;
	private int top;
	
	/**
	 * Thread statuses
	 */
	private final static int GO = 0, SUSPEND = 1, TERMINATE = 2;
	
	public void init() {
		Integer parameter;
		parameter = getIntegerParameter("rows");
		if (parameter!= null && parameter.intValue() > 4 && parameter.intValue() <= 500) {
			rows = parameter.intValue();
			if (rows % 2 == 0) 
				rows++;
		} // end if
		
		parameter = getIntegerParameter("columns");
		if (parameter!= null && parameter.intValue() > 4 && parameter.intValue() <= 500) {
			columns = parameter.intValue();
			if (columns % 2 == 0) 
				columns++;
		} // end if
		
		parameter = getIntegerParameter("borderPixels");
		if (parameter != null && parameter.intValue() > 0 && parameter.intValue() <= 100)
			borderPixels = parameter.intValue();
		
		parameter = getIntegerParameter("sleepTime");
		if (parameter != null && parameter.intValue() > 0)
			sleepTime = parameter.intValue();
		
		parameter = getIntegerParameter("speed");
		if (parameter != null && parameter.intValue() > 0 && parameter.intValue() < 0) {
			switch (parameter.intValue()) {
			case 1:
				speedSleep = 1;
				break;
			case 2:
				speedSleep = 25;
				break;
			case 3:
				speedSleep = 50;
				break;
			case 4:
				speedSleep = 100;
				break;
			case 5:
				speedSleep = 200;
				break;
			} // end switch
		} // end if
		
		mazeColors[wallCode] = getColorParameter("wallColor");
		if (mazeColors[wallCode] == null)
			mazeColors[wallCode] = Color.BLACK;
		
		mazeColors[pathCode] = getColorParameter("pathColor");
		if (mazeColors[pathCode] == null)
			mazeColors[pathCode] = Color.MAGENTA;
		
		mazeColors[emptyCode] = getColorParameter("emptyColor");
		if (mazeColors[emptyCode] == null)
			mazeColors[emptyCode] = Color.BLUE;
		
		mazeColors[backgroundCode] = getColorParameter("borderColor");
		if (mazeColors[backgroundCode] == null)
			mazeColors[backgroundCode] = Color.WHITE;
		
		mazeColors[visitedCode] = getColorParameter("visitedColor");
		if (mazeColors[visitedCode] == null)
			mazeColors[visitedCode] = mazeColors[emptyCode];
		
		setBackground(mazeColors[backgroundCode]);
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} // end try-catch
		while (true) {
			if (checkStatus() == TERMINATE)
				break;
			buildMaze();
			if (checkStatus() == TERMINATE)
				break;
			solveMaze(1, 1); // beginning of maze
			if (checkStatus() == TERMINATE)
				break;
			synchronized(this) {
				try {
					wait(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} // end try-catch
			} // end synchronized
			if (checkStatus() == TERMINATE)
				break;
			mazeExists = false;
			checkSize();
			Graphics me = getGraphics();
			redrawMaze(me);
			me.dispose();
		} // end while
	} // end method run
	
	public synchronized void start() {
		status = GO;
		if (mazeThread == null || !mazeThread.isAlive()) {
			mazeThread = new Thread(this);
			mazeThread.start();
		} else {
			notify();
		} // end if
	} // end method start
	
	public synchronized void stop() {
	     if (mazeThread != null) {
	         status = SUSPEND;
	         notify();
	     } // end if
	} // end metohd stop
	
	public synchronized void destroy() {
	    if (mazeThread != null) {
	        status = TERMINATE;
	        notify();
	     } // end if
	} // end method destroy
	
	public void paint(Graphics g) {
		checkSize();
		redrawMaze(g);
	} // end method paint
	
	public void update(Graphics g) {
		paint(g);
	} // end method update
	
	/**
	 * Reads an applet parameter which is an integer.
	 * Returns null if there is no such parameter, 
	 * or if the value is not a legal integer
	 * @param parameterName parameter in the applet tag
	 * @return an Integer representation of the tag, null if no integer
	 * or not a legal integer
	 */
	private Integer getIntegerParameter(String parameterName) {
		String parameter = getParameter(parameterName);
		if (parameter == null || parameter.length() == 0)
			return null;
		int number;
		try {
			number = Integer.parseInt(parameter);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		} // end try-catch
		return new Integer(number);
	} // end method getIntegerParameter
	
	/**
	 * Reads an appelet parameter which is a colour. 
	 * Returns null if there is no such parameter,
	 * or if the value is not a legal color. 
	 * Colors can be specified by three integers, 
	 * separated by spaces, giving RGB components in the range 0 to 255.
	 * The standard Java color spaces are also acceptable
	 * @param parameterName parameter in the applet tag
	 * @return a Color representation of the applet tag, or null
	 * if no such parameter or not a legal color
	 */
	private Color getColorParameter(String parameterName) {
		String parameter = getParameter(parameterName);
		if (parameter == null || parameter.length() == 0)
			return null;
		
		// parse RGB color
		if (Character.isDigit(parameter.charAt(0))) {
			int r = 0, g = 0, b = 0;
			int position = 0;
			int digit = 0;
			int length = parameter.length();
			
			// checks red
			while (position < length && Character.isDigit(parameter.charAt(position)) && r < 255) {
				digit = Character.digit(parameter.charAt(position), 10);
				r = 10 * r + digit;
				position++;
			} // end while
			if (r > 255)
				return null;
			while (position < length && !Character.isDigit(parameter.charAt(position)))
				position++;
			if (position >= length)
				return null;
			
			// checks green
			while (position < length && Character.isDigit(parameter.charAt(position)) && r < 255) {
				digit = Character.digit(parameter.charAt(position), 10);
				g = 10 * g + digit;
				position++;
			} // end while
			if (g > 255)
				return null;
			while (position < length && !Character.isDigit(parameter.charAt(position)))
				position++;
			if (position >= length)
				return null;
			
			// checks blue. doesn't check position since it's the last number
			while (position < length && Character.isDigit(parameter.charAt(position)) && r < 255) {
				digit = Character.digit(parameter.charAt(position), 10);
				b = 10 * b + digit;
				position++;
			} // end while
			if (b > 255)
				return null;
			
			return new Color(r, g, b);
		} // end if
		
		// checks for standard Java color spaces
		if (parameter.equalsIgnoreCase("black"))
			return Color.BLACK;
		if (parameter.equalsIgnoreCase("white"))
			return Color.WHITE;
		if (parameter.equalsIgnoreCase("red"))
			return Color.RED;
		if (parameter.equalsIgnoreCase("green"))
			return Color.GREEN;
		if (parameter.equalsIgnoreCase("blue"))
			return Color.BLUE;
		if (parameter.equalsIgnoreCase("yellow"))
			return Color.YELLOW;
		if (parameter.equalsIgnoreCase("cyan"))
			return Color.CYAN;
		if (parameter.equalsIgnoreCase("magenta"))
			return Color.MAGENTA;
		if (parameter.equalsIgnoreCase("pink"))
			return Color.PINK;
		if (parameter.equalsIgnoreCase("orange"))
			return Color.ORANGE;
		if (parameter.equalsIgnoreCase("gray"))
			return Color.GRAY;
		if (parameter.equalsIgnoreCase("darkgray"))
			return Color.DARK_GRAY;
		if (parameter.equalsIgnoreCase("lightgray"))
			return Color.LIGHT_GRAY;
		
		// Illegal color
		return null;
	} // end method getColorParameter
	
	/**
	 * Checks the applet size and adjusts variables that depend on the size
	 */
	private void checkSize() {
		if (getWidth() != width || getHeight() != height) {
			width = getWidth();
			height = getHeight();
			int w = (width - 2 * borderPixels) / columns;
			int h = (height - 2 * borderPixels) / rows;
			left = (width - w * columns) / 2;
			top = (height - h * rows) / 2;
			totalWidth = w * columns;
			totalHeight = h * rows;
		} // end if
	} // end method checkSize
	
	/**
	 * Returns maze status
	 * @return the integer representation of the maze status
	 */
	private synchronized int checkStatus() {
		while (status == SUSPEND) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} // end try-catch
		} // end while
		return status;
	} // end method return status 
	
	/**
	 * Draws the entire maze based on applet tags
	 * @param g Graphics object
	 * @return true if maze exists and is drawn
	 */
	private synchronized void redrawMaze(Graphics g) {
		g.setColor(mazeColors[backgroundCode]);
		g.fillRect(0, 0, width, height);
		if (mazeExists) {
			int cellWidth = totalWidth / columns; // cell width
			int cellHeight = totalHeight / rows;   // cell height
			for (int j = 0; j < columns; j++) {
				for (int i = 0; i < rows; i++) {
					if (maze[i][j] < 0)
						g.setColor(mazeColors[emptyCode]);
					else
						g.setColor(mazeColors[maze[i][j]]);
					g.fillRect((j * cellWidth) + left, (i * cellHeight) + top, cellWidth, cellHeight);
				} // end for
			} // end for
		} // end if
		//return mazeExists;
	} // end method redrawMaze
	
	/**
	 * Draws one cell of the maze to a graphics context
	 * @param row cell row
	 * @param column cell column
	 * @param colorNumber color Code
	 */
	private synchronized void drawSquare(int row, int column, int colorNumber) {
		checkSize();
		int cellWidth = totalWidth / columns; 
		int cellHeight = totalHeight / rows;
		Graphics me = getGraphics();
		me.setColor(mazeColors[colorNumber]);
		me.fillRect((column * cellWidth) + left, (row * cellHeight) + top, cellWidth, cellHeight);	
		me.dispose();
	} // end method drawSquare
	
	/**
	 * Generates a random maze. The strategy is to start with
	 * a grid of disconnected rooms separated by walls. Then look at each 
	 * of the separating walls, in a random order. If tearing down a wall would not create a loop
	 * in the maze, then tear it down. Otherwise, leave it in place.
	 * @return true if the maze is built
	 */
	private void buildMaze() {
		if (maze == null)
			maze = new int[rows][columns];
		int i, j;
		int roomCount = 0;
		int wallCount = 0;
		int[] wallRows = new int [(rows * columns) / 2];
		int[] wallColumns = new int[(rows * columns) / 2];
		for (i = 0; i < rows; i++) {
			for (j = 0; j < columns; j++)
				maze[i][j] = wallCode;
		} // end for
		
		// makes a grid of empty rooms
		for (i = 1; i < rows - 1; i += 2) { 
			for (j = 1; j < columns - 1; j += 2) {
				roomCount++;
				maze[i][j] = -roomCount; // rooms represented by a different negative number
				
				// checks walls below the room
				if (i < rows - 2) { 
					wallRows[wallCount] = i + 1;
					wallColumns[wallCount] = j;
					wallCount++;
				} // end if
				
				// checks walls to the right of the room
				if (j < columns - 2) {
					wallRows[wallCount] = i;
					wallColumns[wallCount] = j + 1;
					wallCount++;
				} // end if
			} // end for
		} // end for
		
		mazeExists = true;
		checkSize();
		if (checkStatus() == TERMINATE)
			return;
		Graphics me = getGraphics();
		redrawMaze(me);
		me.dispose();
		int randomWall;
		for (i = wallCount - 1; i > 0; i--) {
			randomWall = (int) (Math.random() * i);
			if (checkStatus() == TERMINATE)
				return;
			tearDown(wallRows[randomWall], wallColumns[randomWall]);
			wallRows[randomWall] = wallRows[i];
			wallColumns[randomWall] = wallColumns[i];
		} // end for
		
		// replace negative values in maze[][] with emptyCode
		for (i = 1; i < rows - 1; i++) {
			for (j = 1; j < columns - 1; j++) {
				if (maze[i][j] < 0)
					maze[i][j] = emptyCode;
			} // end for
		} // end for
	} // end method buildMaze

	/**
	 * Tears down a wall by joining two rooms into one ro,. When a wall
	 * is torn down the room codes on one side are converted to match those on the other side,
	 * so all spaces in a room have the same code. 
	 * @param row maze row
	 * @param column maze column
	 */
	private synchronized void tearDown(int row, int column) {
		// if row is odd, separate room horizontally
		if (row % 2 == 1 && maze[row][column - 1] != maze[row][column + 1]) {
			fill(row, column - 1,  maze[row][column - 1],  maze[row][column + 1]);
			maze[row][column] = maze[row][column + 1];
			drawSquare(row, column, emptyCode);
			try {
				wait(speedSleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} // end try catch
		} // end if
		
		// if row is even, separate room vertically
		else if (row % 2 == 0 && maze[row - 1][column] != maze[row + 1][column]) {
			fill(row - 1, column,  maze[row - 1][column],  maze[row + 1][column]);
			maze[row][column] = maze[row + 1][column];
			drawSquare(row, column, emptyCode);
			try {
				wait(speedSleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} // end try catch
		}
	}

	/**
	 * Changes room codes
	 * @param row
	 * @param col
	 * @param replace
	 * @param replaceWith
	 */
	 private void fill(int row, int col, int replace, int replaceWith) {
	     if (maze[row][col] == replace) {
	         maze[row][col] = replaceWith;
	         fill(row+1,col,replace,replaceWith);
	         fill(row-1,col,replace,replaceWith);
	         fill(row,col+1,replace,replaceWith);
	         fill(row,col-1,replace,replaceWith);
	     }
	 }
	 
	 /**
	  * Solves maze by traversing through possibe paths. 
	  * The maze is solved if it reaches the lower right cell.
	  * @param row maze row
	  * @param column maze column
	  * @return true if the maze is solved
	  */
	 private boolean solveMaze(int row, int column) { 
		 if (maze[row][column] == emptyCode) {
			 maze[row][column] = pathCode;
			 if (checkStatus() == TERMINATE)
				 return false;
			 drawSquare(row, column, pathCode);
			 if (row == rows - 2 && column == columns - 2) 
				 return true; // GOAL!!
			 try {
				 Thread.sleep(speedSleep);
			 } catch (InterruptedException e) {
				 e.printStackTrace(); 
			 } // end try-catch
			 
			 // solves maze by traversing through each path in every possible direction 
			 if ( (solveMaze(row - 1, column) && checkStatus() != TERMINATE) ||
				  (solveMaze(row, column - 1) && checkStatus() != TERMINATE) ||
				  (solveMaze(row + 1, column) && checkStatus() != TERMINATE) ||
				  solveMaze(row, column + 1))
				 return true;
			 if (checkStatus() == TERMINATE)
				 return false;
			 maze[row][column] = visitedCode;
			 drawSquare(row, column, visitedCode);
			 synchronized(this) {
				 try {
					 Thread.sleep(speedSleep);
				 } catch (InterruptedException e) {
					 e.printStackTrace();
				 } // end try-catch
			 } // end synchronized
			 if (checkStatus() == TERMINATE)
				 return true; 
		 } // end if
		 return false;
	 } // end method solveMaze
}
