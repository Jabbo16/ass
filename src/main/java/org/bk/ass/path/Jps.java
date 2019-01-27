package org.bk.ass.path;

import java.util.Collections;

/**
 * Jump point search. Initialize with a {@link Map} instance and call {@link #findPath(Position,
 * Position)}.
 */
public class Jps {

  private final Map map;

  public Jps(Map map) {
    this.map = map;
  }

  /**
   * Searches for a path between start and end. Allows concurrent pathing requests if the map
   * implementation allows concurrent queries.
   *
   * @return a valid path or a path with infinite length if none could be found. Never returns null.
   */
  public Result findPath(Position start, Position end) {
    if (start.equals(end)) {
      return new Result(0, Collections.singletonList(start));
    }
    return new PathFinder(end, map).searchFrom(start);
  }

  private static class PathFinder extends AbstractPathFinder {

    private final Map map;

    protected PathFinder(Position target, Map map) {
      super(target, map);
      this.map = map;
    }

    @Override
    protected Position jumpHorizontal(int px, int py, int dx) {
      assert dx != 0;
      int x = px + dx;

      int a = (map.isWalkable(x, py - 1) ? 0 : 1) | (map.isWalkable(x, py + 1) ? 0 : 2);
      while (map.isWalkable(x, py)) {
        int b = (map.isWalkable(x + dx, py - 1) ? 1 : 0) | (map.isWalkable(x + dx, py + 1) ? 2 : 0);
        if (x == target.x && py == target.y || (a & b) != 0) {
          return new Position(x, py);
        }
        x += dx;
        a = ~b;
      }
      return null;
    }

    @Override
    protected Position jumpVertical(int px, int py, int dy) {
      assert dy != 0;
      int y = py + dy;

      int a = (map.isWalkable(px - 1, y) ? 0 : 1) | (map.isWalkable(px + 1, y) ? 0 : 2);
      while (map.isWalkable(px, y)) {
        int b = (map.isWalkable(px - 1, y + dy) ? 1 : 0) | (map.isWalkable(px + 1, y + dy) ? 2 : 0);
        if (px == target.x && y == target.y || (a & b) != 0) {
          return new Position(px, y);
        }
        y += dy;
        a = ~b;
      }
      return null;
    }
  }
}