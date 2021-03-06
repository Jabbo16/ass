package org.bk.ass.sim;

import io.jenetics.util.IntRange;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openbw.bwapi4j.test.BWDataProvider;
import org.openbw.bwapi4j.type.UnitType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The most interesting tests are those where the outcome is not a landslide. But of course some
 * tests in that department are also required to calibrate the {@link Evaluator}
 */
class EvaluatorTest {
  Evaluator evaluator = new Evaluator();
  private BWAPI4JAgentFactory factory = new BWAPI4JAgentFactory(null);

  @BeforeAll
  static void setup() throws Exception {
    BWDataProvider.injectValues();
  }

  @Test
  void noAgentsShouldNotResultInNaN() {
    // WHEN
    double result = evaluator.evaluate(Collections.emptyList(), Collections.emptyList());

    // THEN
    assertThat(result).isBetween(0.49, 0.51);
  }

  @Test
  void MMvsMM() {
    // GIVEN
    List<Agent> agentsA =
        Arrays.asList(
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Medic),
            factory.of(UnitType.Terran_Medic));
    List<Agent> agentsB =
        Arrays.asList(
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Medic),
            factory.of(UnitType.Terran_Medic));

    // WHEN
    double result = evaluator.evaluate(agentsA, agentsB);

    // THEN
    assertThat(result).isBetween(0.49, 0.51);
  }

  @Test
  void _7MutasVs8Hydras() {
    // GIVEN
    List<Agent> agentsA = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      agentsA.add(factory.of(UnitType.Zerg_Mutalisk));
    }
    List<Agent> agentsB = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      agentsB.add(factory.of(UnitType.Zerg_Hydralisk));
    }

    // WHEN
    double result = evaluator.evaluate(agentsA, agentsB);

    // THEN
    assertThat(result).isBetween(0.4, 0.6);
  }

  @Test
  void _10HydrasVsDT() {
    // GIVEN
    List<Agent> agentsA = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      agentsA.add(factory.of(UnitType.Zerg_Hydralisk));
    }
    List<Agent> agentsB =
        Collections.singletonList(factory.of(UnitType.Protoss_Dark_Templar).setDetected(false));

    // WHEN
    double result = evaluator.evaluate(agentsA, agentsB);

    // THEN
    assertThat(result).isLessThan(0.2);
  }

  @Test
  void _7MutasVs14Marines() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk));

    List<Agent> b =
        Arrays.asList(
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isLessThan(0.5);
  }

  @Test
  void _2LurkersVs6Marines() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(factory.of(UnitType.Zerg_Lurker), factory.of(UnitType.Zerg_Lurker));

    List<Agent> b =
        Arrays.asList(
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isGreaterThan(0.6);
  }

  @Test
  void _2LurkersVs14Marines() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(factory.of(UnitType.Zerg_Lurker), factory.of(UnitType.Zerg_Lurker));

    List<Agent> b =
        Arrays.asList(
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isLessThan(0.3);
  }

  @Test
  void _6MutasVs1Bunker() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Terran_Bunker));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isGreaterThan(0.5);
  }

  @Test
  void _5MutasVs1Bunker() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Terran_Bunker));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isLessThan(0.5);
  }

  @Test
  void _1MutaVs1SCV() {
    // GIVEN
    List<Agent> a = Collections.singletonList(factory.of(UnitType.Zerg_Mutalisk));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Terran_SCV));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isGreaterThan(0.9);
  }

  @Test
  void GoonVsTank() {
    // GIVEN
    List<Agent> a = Collections.singletonList(factory.of(UnitType.Protoss_Dragoon));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Terran_Siege_Tank_Tank_Mode));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isGreaterThan(0.5);
  }

  @Test
  void _3ZerglingVsSiegedTankAndMarine() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling));
    List<Agent> b =
        Arrays.asList(
            factory.of(UnitType.Terran_Siege_Tank_Siege_Mode), factory.of(UnitType.Terran_Marine));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isBetween(0.2, 0.4);
  }

  @Test
  void _6ZerglingVsSiegedTankAndMarine() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling));
    List<Agent> b =
        Arrays.asList(
            factory.of(UnitType.Terran_Siege_Tank_Siege_Mode), factory.of(UnitType.Terran_Marine));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isBetween(0.5, 0.7);
  }

  @Test
  void reaverVs12Lings() {
    // GIVEN

    List<Agent> a = Collections.singletonList(factory.of(UnitType.Protoss_Reaver));

    List<Agent> b = IntRange.of(0, 11).stream().mapToObj(unused -> factory.of(UnitType.Zerg_Zergling)).collect(Collectors.toList());

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isBetween(0.2, 0.45);
  }

  @Test
  void reaverVs9Lings() {
    // GIVEN

    List<Agent> a = Collections.singletonList(factory.of(UnitType.Protoss_Reaver));

    List<Agent> b = IntRange.of(0, 9).stream().mapToObj(unused -> factory.of(UnitType.Zerg_Zergling)).collect(Collectors.toList());

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isBetween(0.5, 0.8);
  }

  @Test
  void MvsMM() {
    // GIVEN
    List<Agent> a = IntRange.of(0, 4).stream().mapToObj(unused -> factory.of(UnitType.Terran_Marine)).collect(Collectors.toList());
    List<Agent> b = Stream.concat(
            IntRange.of(0, 3).stream().mapToObj(unused -> factory.of(UnitType.Terran_Marine)),
            IntRange.of(0, 1).stream().mapToObj(unused -> factory.of(UnitType.Terran_Medic)))
            .collect(Collectors.toList());


    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isLessThan(0.3);
  }

  @Test
  void vsNothingIsFreeWin() {
    List<Agent> a = IntRange.of(0, 4).stream().mapToObj(unused -> factory.of(UnitType.Terran_Marine)).collect(Collectors.toList());
    List<Agent> b = Collections.emptyList();

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isGreaterThan(0.9);
  }

  @Test
  void vsHatcheryIsFreeWin() {
    List<Agent> a = Collections.singletonList(factory.of(UnitType.Zerg_Zergling));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Zerg_Hatchery));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isGreaterThan(0.9);
  }
}
