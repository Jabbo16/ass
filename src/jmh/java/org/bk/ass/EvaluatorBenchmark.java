package org.bk.ass;

import org.bk.ass.sim.Agent;
import org.bk.ass.sim.BWAPI4JAgentFactory;
import org.bk.ass.sim.Evaluator;
import org.openbw.bwapi4j.test.BWDataProvider;
import org.openbw.bwapi4j.type.UnitType;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Measurement(iterations = 5, time = 5)
@Fork(3)
public class EvaluatorBenchmark {

  @State(Scope.Thread)
  public static class MyState {

    Evaluator evaluator = new Evaluator();
    BWAPI4JAgentFactory factory = new BWAPI4JAgentFactory(null);
    private List<Agent> agentsA = new ArrayList<>();
    private List<Agent> agentsB = new ArrayList<>();

    @Setup
    public void setup() {
      for (int i = 0; i < 7; i++) {
        agentsA.add(factory.of(UnitType.Zerg_Mutalisk));
      }
      for (int i = 0; i < 8; i++) {
        agentsB.add(factory.of(UnitType.Zerg_Hydralisk));
      }
    }
  }

  static {
    try {
      BWDataProvider.injectValues();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Benchmark
  public double _7MutasVs8Hydras(MyState state) {
    return state.evaluator.evaluate(state.agentsA, state.agentsB);
  }
}
