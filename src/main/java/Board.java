public class Board
{
    public enum CellState { WATER, SHIP, HIT, MISS }

    private final CellState[][] cells = new CellState[10][10];
    private final Ship[][] owner = new Ship[10][10];

    public Board()
    {
        for (int r = 0; r < 10; r++)
        {
            for (int c = 0; c < 10; c++)
            {
                cells[r][c] = CellState.WATER;
            }
        }
    }

    public CellState getCell(int r, int c)
    {
        if (!isInBounds(r, c)) return CellState.WATER;
        return cells[r][c];
    }

    public boolean isInBounds(int r, int c) { return r >= 0 && r < 10 && c >= 0 && c < 10; }

    public boolean canPlaceShip(Ship ship, int r, int c, boolean horizontal)
    {
        int len = ship.getLength();
        for (int i = 0; i < len; i++)
        {
            int rr = r + (horizontal ? 0 : i);
            int cc = c + (horizontal ? i : 0);
            if (!isInBounds(rr, cc)) return false;
            if (cells[rr][cc] != CellState.WATER) return false;
        }
        return true;
    }

    public void placeShip(Ship ship, int r, int c, boolean horizontal)
    {
        int len = ship.getLength();
        for (int i = 0; i < len; i++)
        {
            int rr = r + (horizontal ? 0 : i);
            int cc = c + (horizontal ? i : 0);
            cells[rr][cc] = CellState.SHIP;
            owner[rr][cc] = ship;
        }
    }

    public void clearShip(Ship ship)
    {
        for (int r = 0; r < 10; r++)
        {
            for (int c = 0; c < 10; c++)
            {
                if (owner[r][c] == ship)
                {
                    owner[r][c] = null;
                    cells[r][c] = CellState.WATER;
                }
            }
        }
    }

    public boolean shoot(int r, int c)
    {
        if (!isInBounds(r, c)) return false;

        if (cells[r][c] == CellState.HIT || cells[r][c] == CellState.MISS) {
            return false;
        }

        if (cells[r][c] == CellState.SHIP)
        {
            cells[r][c] = CellState.HIT;
            Ship s = owner[r][c];
            if (s != null) {
                s.registerHit(r, c);
            }
            return true;
        }
        else if (cells[r][c] == CellState.WATER)
        {
            cells[r][c] = CellState.MISS;
            return false;
        }
        return false;
    }

    public void setCell(int r, int c, CellState state)
    {
        if (!isInBounds(r, c)) return;
        cells[r][c] = state;
    }

    public Ship getOwnerAt(int r, int c)
    {
        if (!isInBounds(r, c)) return null;
        return owner[r][c];
    }

    public void setOwnerOnly(int r, int c, Ship s)
    {
        if (!isInBounds(r, c)) return;
        owner[r][c] = s;
    }
}
