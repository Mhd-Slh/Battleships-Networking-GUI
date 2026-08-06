public class Board
{
    public enum CellState
    {
        WATER,
        SHIP,
        HIT,
        MISS
    }

    private final CellState[][] board;

    public Board()
    {
        board = new CellState[10][10];

        for(int i = 0; i < 10; i++)
        {
            for(int j = 0; j < 10; j++)
            {
                board[i][j] = Board.CellState.WATER;
            }
        }
    }

    public boolean isInBounds(int x, int y) { return x >= 0 && x < 10 && y >= 0 && y < 10; }

    public void clear()
    {
        for(int i = 0; i < 10; i++)
            for(int j = 0; j < 10; j++)
                board[i][j] = CellState.WATER;
    }

    public CellState getCell(int x, int y) { return board[x][y]; }

    public void setCell(int x, int y, CellState state) { board[x][y] = state; }

    public boolean canPlaceShip(Ship ship, int x, int y, boolean horizontal)
    {
        for (int i = 0; i < ship.getLength(); i++)
        {
            int r = x + (horizontal ? 0 : i);
            int c = y + (horizontal ? i : 0);

            if (!isInBounds(r, c) || (board[r][c] != CellState.WATER)) { return false; }
        }
        return true;
    }

    public void placeShip(Ship ship, int x, int y, boolean horizontal)
    {
        if (!canPlaceShip(ship, x, y, horizontal)) { return; }

        for (int i = 0; i < ship.getLength(); i++)
        {
            int r = x + (horizontal ? 0 : i);
            int c = y + (horizontal ? i : 0);
            board[r][c] = CellState.SHIP;
        }
    }

    public void clearShip(Ship ship)
    {
        if (!ship.isPlaced()) return;

        for (int i = 0; i < ship.getLength(); i++)
        {
            int r = ship.getStartX() + (ship.isHorizontal() ? 0 : i);
            int c = ship.getStartY() + (ship.isHorizontal() ? i : 0);

            if (isInBounds(r, c) && board[r][c] == CellState.SHIP)
            {
                board[r][c] = CellState.WATER;
            }
        }

        ship.setPlaced(false);
    }

    public boolean attack(int x, int y){
        if (!isInBounds(x, y)) {return false;}
        CellState state = getCell(x, y);
        if (state == CellState.SHIP) {
            setCell(x, y, CellState.HIT);
            return true;   //hit
        }

        else if (state == CellState.WATER) {
            setCell(x, y, CellState.MISS);
            return false;  //miss
        }
        return false;
    }
}
