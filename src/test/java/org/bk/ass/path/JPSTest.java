package org.bk.ass.path;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.SplittableRandom;
import javax.imageio.ImageIO;
import org.bk.ass.path.JPS.Map;
import org.bk.ass.path.JPS.Position;
import org.bk.ass.path.JPS.Result;
import org.junit.jupiter.api.Test;

class JPSTest {

  @Test
  void shouldReturnIdentityIfStartAndEndMatch() {
    // GIVEN
    JPS sut = new JPS(Map.fromBooleanArray(new boolean[][]{{true}}));

    // WHEN
    Result result = sut.findPath(new Position(0, 0), new Position(0, 0));

    // THEN
    assertThat(result.path).containsExactly(new Position(0, 0));
  }

  @Test
  void shouldFindNotFindPathWhenBlocked() {
    // GIVEN
    JPS sut = new JPS(Map.fromBooleanArray(new boolean[][]{{true, false, true}}));

    // WHEN
    Result result = sut.findPath(new Position(0, 0), new Position(2, 0));

    // THEN
    assertThat(result.path).isEmpty();
  }

  @Test
  void shouldFindHorizontalPath() {
    // GIVEN
    JPS sut = new JPS(Map.fromBooleanArray(new boolean[][]{{true, true, true}}));

    // WHEN
    Result result = sut.findPath(new Position(0, 0), new Position(2, 0));

    // THEN
    assertThat(result.path).containsExactly(new Position(0, 0), new Position(2, 0));
  }

  @Test
  void shouldFindVerticalPath() {
    // GIVEN
    JPS sut = new JPS(Map.fromBooleanArray(new boolean[][]{{true}, {true}, {true}}));

    // WHEN
    Result result = sut.findPath(new Position(0, 0), new Position(0, 2));

    // THEN
    assertThat(result.path).containsExactly(new Position(0, 0), new Position(0, 2));
  }

  @Test
  void shouldFindDiagonalPath() {
    // GIVEN
    JPS sut =
        new JPS(
            Map.fromBooleanArray(
                new boolean[][]{{true, true, true}, {true, true, true}, {true, true, true}}));

    // WHEN
    Result result = sut.findPath(new Position(2, 2), new Position(0, 0));

    // THEN
    assertThat(result.path).containsExactly(new Position(2, 2), new Position(0, 0));
  }

  @Test
  void shouldFindPathWithObstacle() {
    // GIVEN
    JPS sut =
        new JPS(
            Map.fromBooleanArray(
                new boolean[][]{{true, true, true}, {true, false, false}, {true, true, true}}));

    // WHEN
    Result result = sut.findPath(new Position(2, 2), new Position(0, 0));

    // THEN
    assertThat(result.path)
        .containsExactly(
            new Position(2, 2), new Position(1, 2), new Position(0, 1), new Position(0, 0));
  }

  @Test
  void shouldNotFindPathWhenBlockedButWithCircle() {
    // GIVEN
    JPS sut =
        new JPS(
            Map.fromBooleanArray(
                new boolean[][]{
                    {true, true, true, true, true},
                    {true, false, false, false, true},
                    {true, false, true, false, true},
                    {true, false, false, false, true},
                    {true, true, true, true, true}
                }));

    // WHEN
    Result result = sut.findPath(new Position(0, 0), new Position(2, 2));

    // THEN
    assertThat(result.path).isEmpty();
  }

  @Test
  void shouldFindPathWhenCircleHasHole() {
    // GIVEN
    JPS sut =
        new JPS(
            Map.fromBooleanArray(
                new boolean[][]{
                    {true, true, true, true, true},
                    {true, false, false, false, true},
                    {true, false, true, false, true},
                    {true, false, false, true, true},
                    {true, true, true, true, true}
                }));

    // WHEN
    Result result = sut.findPath(new Position(0, 0), new Position(2, 2));

    // THEN
    assertThat(result.length).isBetween(8f, 8.3f);
  }

  @Test
  void shouldFindPathInLargerExample() {
    // GIVEN
    JPS sut =
        new JPS(
            (x, y) ->
                y >= 0
                    && y <= 999
                    && (x == 0 && y % 4 == 1
                    || x == 999 && y % 4 == 3
                    || y % 2 == 0 && x >= 0 && x <= 999));

    // WHEN
    Result result = sut.findPath(new Position(0, 0), new Position(999, 999));

    // THEN
    assertThat(result.path).hasSize(1499);
  }

  @Test
  void shouldFindPathInDemoMap() throws IOException {
    // GIVEN
    BufferedImage image = ImageIO.read(JPSTest.class.getResourceAsStream("/dungeon_map.bmp"));
    Map map =
        (x, y) ->
            x >= 0
                && x < image.getWidth()
                && y >= 0
                && y < image.getHeight()
                && image.getRGB(x, y) == -1;
    JPS sut = new JPS(map);
    SplittableRandom rnd = new SplittableRandom(123456);
    Position start;
    do {
      start = new Position(rnd.nextInt(image.getWidth()), rnd.nextInt(image.getHeight()));
    } while (!map.isWalkable(start.x, start.y));
    Position end;
    do {
      end = new Position(rnd.nextInt(image.getWidth()), rnd.nextInt(image.getHeight()));
    } while (!map.isWalkable(end.x, end.y));

    // WHEN
    Result result = sut.findPath(start, end);

    // THEN
    assertThat(result.path).isNotEmpty();
    BufferedImage out =
        new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    Graphics g = out.getGraphics();
    g.drawImage(image, 0, 0, null);
    g.setColor(Color.GREEN);
    Position last = null;
    for (Position p : result.path) {
      if (last != null) {
        g.drawLine(last.x, last.y, p.x, p.y);
      }
      last = p;
    }
    ImageIO.write(out, "PNG", new File("build/map_with_path.png"));
  }
}
