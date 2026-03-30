package fr.upsaclay.bibs.pacman.model.maze;

import fr.upsaclay.bibs.pacman.model.Direction;

import java.util.Objects;

/**
 * Concrete implementation of {@link Maze}.
 *
 * <h2>Why a separate MazeImpl file?</h2>
 * The project provides {@link Maze} as an interface used by tests and by the rest of the application.
 * Keeping the interface separated from its implementation has several benefits:
 * <ul>
 *   <li><b>Encapsulation:</b> the internal representation (here a 2D {@link Tile} array) stays private and can evolve
 *       without breaking the public API.</li>
 *   <li><b>Test compatibility:</b> tests depend on the interface contract, not on a specific implementation.</li>
 *   <li><b>Maintainability:</b> Maze-related logic (bounds checks, wrapping, pixel→tile conversion) is centralized in one
 *       concrete class.</li>
 * </ul>
 *
 * <p>
 * This implementation models the maze as a rectangular grid of tiles. Each tile corresponds to a fixed number of pixels
 * (see constants in {@link Maze}). Positions of actors are expressed in pixels, but interactions with the maze are
 * tile-based. Therefore the class provides helpers to convert pixels to tile positions and to navigate between tiles.
 * </p>
 */
public final class MazeImpl implements Maze {

    /** Number of tiles horizontally. */
    private final int width;

    /** Number of tiles vertically. */
    private final int height;

    /**
     * Internal grid representation: {@code tiles[line][col]}.
     * Kept private so we can change the representation later without changing the API.
     */
    private final Tile[][] tiles;

    /**
     * Creates an "empty" maze filled with an empty tile (typically {@code EE}).
     *
     * @param width  number of tiles horizontally
     * @param height number of tiles vertically
     * @throws IllegalArgumentException if width/height are not strictly positive
     */
    public MazeImpl(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width/height must be positive");
        }
        this.width = width;
        this.height = height;

        // Why: tests and early steps need a valid maze; defaulting to an empty tile avoids null tiles everywhere.
        Tile fill = defaultEmptyTile();

        this.tiles = new Tile[height][width];
        for (int line = 0; line < height; line++) {
            for (int col = 0; col < width; col++) {
                this.tiles[line][col] = fill;
            }
        }
    }

    /**
     * Creates a maze from an existing tile grid.
     * Defensive copying is used to prevent external modifications to the maze.
     *
     * @param tiles rectangular non-null grid of tiles
     * @throws NullPointerException     if tiles (or any cell) is null
     * @throws IllegalArgumentException if grid is empty or not rectangular
     */
    public MazeImpl(Tile[][] tiles) {
        Objects.requireNonNull(tiles, "tiles");
        if (tiles.length == 0 || tiles[0].length == 0) {
            throw new IllegalArgumentException("tiles must be non-empty");
        }
        this.height = tiles.length;
        this.width = tiles[0].length;

        // Why: defensive copy so callers cannot mutate our internal maze after construction.
        Tile[][] copy = new Tile[height][width];
        for (int line = 0; line < height; line++) {
            if (tiles[line] == null || tiles[line].length != width) {
                throw new IllegalArgumentException("tiles must be rectangular");
            }
            for (int col = 0; col < width; col++) {
                copy[line][col] = Objects.requireNonNull(
                        tiles[line][col],
                        "tile at (" + line + "," + col + ")"
                );
            }
        }
        this.tiles = copy;
    }

    /**
     * Returns the tile used for "empty" cells.
     * The project commonly uses the enum constant {@code EE}, but we provide a fallback to keep the code robust even if
     * the tile set changes.
     *
     * @return the empty tile constant
     */
    private static Tile defaultEmptyTile() {
        try {
            return Tile.valueOf("EE");
        } catch (IllegalArgumentException ex) {
            Tile[] all = Tile.values();
            if (all.length == 0) {
                throw new IllegalStateException("Tile enum has no values");
            }
            return all[0];
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getWidth() {
        return width;
    }

    /** {@inheritDoc} */
    @Override
    public int getPixelWidth() {
        return width * TILE_WIDTH;
    }

    /** {@inheritDoc} */
    @Override
    public int getHeight() {
        return height;
    }

    /** {@inheritDoc} */
    @Override
    public int getPixelHeight() {
        return height * TILE_HEIGHT;
    }

    /**
     * Returns the tile at a given tile coordinate.
     *
     * @param line row index
     * @param col  column index
     * @return the tile at (line, col)
     * @throws IndexOutOfBoundsException if coordinates are outside the maze
     */
    @Override
    public Tile getTile(int line, int col) {
        checkBounds(line, col);
        return tiles[line][col];
    }

    /** {@inheritDoc} */
    @Override
    public Tile getTile(TilePosition pos) {
        Objects.requireNonNull(pos, "pos");
        return getTile(pos.getLine(), pos.getCol());
    }

    /**
     * Computes the neighbouring tile position when moving one tile in the given direction.
     * Uses wrap-around (modulo) so going out of the grid re-enters on the other side.
     *
     * <p>
     * Why: the original Pac-Man maze has tunnel behaviour; using wrap-around is a simple step-1 behavior that can be
     * refined later if needed (e.g. horizontal-only tunnels).
     * </p>
     */
    @Override
    public TilePosition getNeighbourTilePosition(int line, int col, Direction dir) {
        Objects.requireNonNull(dir, "dir");

        int nl = line;
        int nc = col;

        switch (dir) {
            case UP -> nl--;
            case DOWN -> nl++;
            case LEFT -> nc--;
            case RIGHT -> nc++;
        }

        nl = wrap(nl, height);
        nc = wrap(nc, width);
        return new TilePosition(nl, nc);
    }

    /** {@inheritDoc} */
    @Override
    public TilePosition getNeighbourTilePosition(TilePosition pos, Direction dir) {
        Objects.requireNonNull(pos, "pos");
        return getNeighbourTilePosition(pos.getLine(), pos.getCol(), dir);
    }

    /** {@inheritDoc} */
    @Override
    public Tile getNeighbourTile(int line, int col, Direction dir) {
        return getTile(getNeighbourTilePosition(line, col, dir));
    }

    /** {@inheritDoc} */
    @Override
    public Tile getNeighbourTile(TilePosition pos, Direction dir) {
        return getTile(getNeighbourTilePosition(pos, dir));
    }

    /**
     * Updates a tile in the maze.
     * This is used by later steps to consume dots/bonuses and update the maze state.
     */
    @Override
    public void setTile(int line, int col, Tile tile) {
        Objects.requireNonNull(tile, "tile");
        checkBounds(line, col);
        tiles[line][col] = tile;
    }

    /** {@inheritDoc} */
    @Override
    public void setTile(TilePosition pos, Tile tile) {
        Objects.requireNonNull(pos, "pos");
        setTile(pos.getLine(), pos.getCol(), tile);
    }

    /**
     * Converts a pixel coordinate into the corresponding tile coordinate.
     *
     * <p>
     * Why clamp: early steps and tests may give boundary pixel values; clamping keeps the method safe and prevents
     * index errors.
     * </p>
     *
     * @param x x-coordinate in pixels
     * @param y y-coordinate in pixels
     * @return the tile position containing (x, y)
     */
    @Override
    public TilePosition getTilePosition(int x, int y) {
        int col = x / TILE_WIDTH;
        int line = y / TILE_HEIGHT;

        col = clamp(col, 0, width - 1);
        line = clamp(line, 0, height - 1);

        return new TilePosition(line, col);
    }

    /**
     * Counts remaining dots on the maze.
     * In this project, small dots and big dots are represented by {@code SD} and {@code BD} tiles.
     *
     * @return number of dot tiles (small + big)
     */
    @Override
    public int getNumberOfDots() {
        int count = 0;
        for (int line = 0; line < height; line++) {
            for (int col = 0; col < width; col++) {
                String n = tiles[line][col].name();
                if ("SD".equals(n) || "BD".equals(n)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Validates tile coordinates.
     * Centralized to keep error messages consistent and avoid duplicated checks.
     */
    private void checkBounds(int line, int col) {
        if (line < 0 || line >= height || col < 0 || col >= width) {
            throw new IndexOutOfBoundsException("Out of bounds: " + line + "," + col);
        }
    }

    /**
     * Wraps an index in {@code [0, mod-1]} using mathematical modulo (works with negatives).
     */
    private static int wrap(int v, int mod) {
        int r = v % mod;
        return r < 0 ? r + mod : r;
    }

    /**
     * Clamps a value into {@code [lo, hi]}.
     */
    private static int clamp(int v, int lo, int hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }
}