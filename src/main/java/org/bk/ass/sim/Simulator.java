package org.bk.ass.sim;

import org.bk.ass.PositionOutOfBoundsException;
import org.bk.ass.collection.UnorderedCollection;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * Used to simulate 2 groups of agents engaging each other. Either use the default constructor which
 * initialized the default behavior or customize the behaviors. Ie. if you want to simulate one
 * group running away while the other uses the default behavior: <br>
 * <code>new Simulator(new {@link RetreatBehavior}(), new {@link RoleBasedBehavior}());</code> <br>
 * General usage guide: <br>
 *
 * <ol>
 *   <li>Create a new Simulator
 *   <li>On each frame, call <code>reset()</code> <em>once</em>
 *   <li>Before each simulation call <code>resetUnits()</code> before adding units
 * </ol>
 *
 * Be cautious when modifying {@link Agent}s after they have been added to the simulation.
 */
public class Simulator {
  /** Use to count health and shields as equal, maybe useful in PvP? */
  public static final ToIntFunction<Agent> HEALTH_AND_SHIELD =
      agent -> agent.getHealth() + agent.getShields();
  /** Use to count shields as half the value of health. */
  public static final ToIntFunction<Agent> HEALTH_AND_HALFED_SHIELD =
      agent -> agent.getHealth() + agent.getShields() / 2;

  private static final int MAX_MAP_DIMENSION = 8192;
  private static final int TILE_SIZE = 16;
  // Hack to fix DTs not being able to hit a target in another TILE due to collision
  public static final int MIN_SIMULATION_RANGE =
      (TILE_SIZE + TILE_SIZE / 2) * (TILE_SIZE + TILE_SIZE / 2);
  private static final int COLLISION_MAP_DIMENSION = MAX_MAP_DIMENSION / TILE_SIZE;
  private final UnorderedCollection<Agent> playerA = new UnorderedCollection<>();
  private final UnorderedCollection<Agent> playerB = new UnorderedCollection<>();

  final byte[] collision = new byte[COLLISION_MAP_DIMENSION * COLLISION_MAP_DIMENSION];
  private final Behavior playerABehavior;
  private final Behavior playerBBehavior;
  private final int frameSkip;

  private Simulator(int frameSkip, Behavior playerABehavior, Behavior playerBBehavior) {
    if (frameSkip < 1) throw new IllegalArgumentException("frameSkip must be >= 1");
    Objects.requireNonNull(playerABehavior, "Behavior of player A must be set");
    Objects.requireNonNull(playerBBehavior, "Behavior of player B must be set");

    this.playerABehavior = playerABehavior;
    this.playerBBehavior = playerBBehavior;
    this.frameSkip = frameSkip;
  }

  public Simulator addAgentA(Agent agent) {
    checkBounds(agent);
    playerA.add(agent);
    if (!agent.isFlyer) {
      collision[colindex(agent.x, agent.y)]++;
    }
    return this;
  }

  public void removeAgentA(Agent agent) {
    playerA.remove(agent);
  }

  public void removeAgentB(Agent agent) {
    playerB.remove(agent);
  }

  public Simulator addAgentB(Agent agent) {
    checkBounds(agent);
    playerB.add(agent);
    if (!agent.isFlyer) {
      collision[colindex(agent.x, agent.y)]++;
    }
    return this;
  }

  private void checkBounds(Agent agent) {
    if (agent.x < 0 || agent.x >= 8192 || agent.y < 0 || agent.y >= 8192) {
      throw new PositionOutOfBoundsException(
          agent + " should be inside the map! This could be caused by an agent being fogged.");
    }
  }

  public Collection<Agent> getAgentsA() {
    return Collections.unmodifiableCollection(playerA);
  }

  public Collection<Agent> getAgentsB() {
    return Collections.unmodifiableCollection(playerB);
  }

  /**
   * Performs a summation of evaluations on agents of player a and b.
   *
   * @param agentEval the evaluation function to use
   * @return the sum of evaluations for all agents, accumulated to an {@link IntEvaluation}.
   */
  public IntEvaluation evalToInt(ToIntFunction<Agent> agentEval) {
    int evalA = 0;
    for (Agent agent : playerA) evalA += agentEval.applyAsInt(agent);
    int evalB = 0;
    for (Agent agent : playerB) evalB += agentEval.applyAsInt(agent);
    return new IntEvaluation(evalA, evalB);
  }

  /**
   * Simulates 4 seconds into the future.
   *
   * @return the actual number of frames simulated, 96 if the battle was not decided earlier
   */
  public int simulate() {
    return simulate(96);
  }

  /**
   * Simulate the given number of frames. If negative, simulation will only stop if one party has no
   * agents left. If units decide to run away, this could be an endless loop - use with care!
   *
   * @return the number of frames left after simulating, usually 0
   */
  public int simulate(int frames) {
    if (frames > 0) frames += Math.floorMod(frameSkip - frames, frameSkip);
    while (frames != 0 && !playerA.isEmpty() && !playerB.isEmpty()) {
      frames -= frameSkip;
      if (!step()) {
        break;
      }
    }
    playerA.clearReferences();
    playerB.clearReferences();
    return frames;
  }

  public void reset() {
    for (int i = playerA.size() - 1; i >= 0; i--) {
      Agent agent = playerA.get(i);
      collision[colindex(agent.x, agent.y)] = 0;
    }
    for (int i = playerB.size() - 1; i >= 0; i--) {
      Agent agent = playerB.get(i);
      collision[colindex(agent.x, agent.y)] = 0;
    }
    resetUnits();
  }

  private void resetUnits() {
    playerA.clear();
    playerB.clear();
  }

  /**
   * Simulate one frame.
   *
   * @return false, if nothing happened in this step and the sim can be aborted.
   */
  private boolean step() {
    boolean simRunning = false;
    for (int i = playerA.size() - 1; i >= 0; i--) {
      Agent agent = playerA.get(i);
      simRunning |=
          agent.isLockeddown
              || agent.isStasised
              || playerABehavior.simUnit(frameSkip, agent, playerA, playerB);
    }
    for (int i = playerB.size() - 1; i >= 0; i--) {
      Agent agent = playerB.get(i);
      simRunning |=
          agent.isLockeddown
              || agent.isStasised
              || playerBBehavior.simUnit(frameSkip, agent, playerB, playerA);
    }
    removeDead(playerA);
    removeDead(playerB);
    updateStats(playerA);
    updateStats(playerB);
    return simRunning;
  }

  private void removeDead(UnorderedCollection<Agent> agents) {
    int i = 0;
    while (i < agents.size()) {
      if (agents.get(i).healthShifted < 1) {
        Agent agent = agents.removeAt(i);
        if (!agent.isFlyer) collision[colindex(agent.x, agent.y)]--;
        agent.onDeathHandler.accept(agent, agents);
      } else {
        i++;
      }
    }
  }

  private void updateStats(UnorderedCollection<Agent> agents) {
    for (int i = agents.size() - 1; i >= 0; i--) {
      Agent agent = agents.get(i);

      assert agent.healthShifted >= 0;

      updatePosition(agent);
      agent.vx = 0;
      agent.vy = 0;
      agent.healedThisFrame = false;

      // Since these calls are potentially made every frame, no boundary checks are done for performance reasons!
      // Bounds are established when the fields are modified.
      agent.cooldown -= frameSkip;
      agent.shieldsShifted += 7 * frameSkip;
      if (agent.plagueDamagePerFrameShifted * frameSkip < agent.healthShifted)
        agent.healthShifted -= agent.plagueDamagePerFrameShifted * frameSkip;
      agent.remainingStimFrames -= frameSkip;
      if (agent.regeneratesHealth) agent.healthShifted += 4 * frameSkip;
      agent.energyShifted += 8 * frameSkip;
    }
  }

  private void updatePosition(Agent agent) {
    int tx = agent.x + agent.vx;
    int ty = agent.y + agent.vy;
    if (tx < 0 || ty < 0 || tx >= MAX_MAP_DIMENSION || ty >= MAX_MAP_DIMENSION) {
      return;
    }

    if (!agent.isFlyer) {
      int oldCI = colindex(agent.x, agent.y);
      int newCI = colindex(tx, ty);
      if (oldCI != newCI) {
        if (collision[newCI] > TILE_SIZE / 8 - 1) {
          return;
        }
        collision[oldCI]--;
        collision[newCI]++;
      }
    }

    agent.x = tx;
    agent.y = ty;
  }

  private int colindex(int tx, int ty) {
    return ty / TILE_SIZE * COLLISION_MAP_DIMENSION + tx / TILE_SIZE;
  }

  /** Dispatches behaviors based on the role in combat. */
  public static class RoleBasedBehavior implements Behavior {

    private final Behavior attackerSimulator;
    private final Behavior healerSimulator;
    private final Behavior repairerSimulator;
    private final Behavior suiciderSimulator;

    public RoleBasedBehavior(
        Behavior attackerSimulator,
        Behavior healerSimulator,
        Behavior repairerSimulator,
        Behavior suiciderSimulator) {
      this.attackerSimulator = attackerSimulator;
      this.healerSimulator = healerSimulator;
      this.repairerSimulator = repairerSimulator;
      this.suiciderSimulator = suiciderSimulator;
    }

    public RoleBasedBehavior() {
      this(
          new AttackerBehavior(),
          new HealerBehavior(),
          new RepairerBehavior(),
          new SuiciderBehavior());
    }

    @Override
    public boolean simUnit(
        int frameSkip,
        Agent agent,
        UnorderedCollection<Agent> allies,
        UnorderedCollection<Agent> enemies) {
      if (agent.isSuicider) {
        return suiciderSimulator.simUnit(frameSkip, agent, allies, enemies);
      }
      if (agent.isHealer) {
        return healerSimulator.simUnit(frameSkip, agent, allies, enemies);
      }
      if (agent.isRepairer && repairerSimulator.simUnit(frameSkip, agent, allies, enemies)) {
        return true;
        // Otherwise FIGHT, you puny SCV!
      }
      return attackerSimulator.simUnit(frameSkip, agent, allies, enemies);
    }
  }

  /**
   * Implementations define what action to take for an agent in regards to its allies and/or
   * enemies.
   */
  public interface Behavior {

    /**
     * Simulate the given agent. Returns true if the agent was active (including waiting),
     * false if the agent won't be able to do anything anymore.
     */
    boolean simUnit(
            int frameSkip,
            Agent agent,
            UnorderedCollection<Agent> allies,
            UnorderedCollection<Agent> enemies);
  }

  public static class IntEvaluation {
    public final int evalA;
    public final int evalB;

    IntEvaluation(int evalA, int evalB) {
      this.evalA = evalA;
      this.evalB = evalB;
    }

    /**
     * Returns the delta of the evaluations for player a and b (evalA - evalB). Evaluating before
     * and after a simulation, the 2 resulting deltas can be used to determine a positive or
     * negative outcome.
     */
    public int delta() {
      return evalA - evalB;
    }

    public int dot(IntEvaluation other) {
      return evalA * other.evalA - evalB * other.evalB;
    }

    public int cross(IntEvaluation other) {
      return evalA * other.evalB - evalB * other.evalA;
    }

    /**
     * Subtracts another evaluation and returns the result. Evaluating before and after a
     * simulation, this can be used to calculate before - after. This in turn represents the loss
     * each player had in the meantime.
     */
    public IntEvaluation subtract(IntEvaluation other) {
      return new IntEvaluation(evalA - other.evalA, evalB - other.evalB);
    }
  }

  public static final class Builder {
    private Behavior playerABehavior = new RoleBasedBehavior();
    private Behavior playerBBehavior = new RoleBasedBehavior();
    private int frameSkip = 1;

    public Builder() {}

    public Builder withPlayerABehavior(Behavior playerABehavior) {
      this.playerABehavior = playerABehavior;
      return this;
    }

    public Builder withPlayerBBehavior(Behavior playerBBehavior) {
      this.playerBBehavior = playerBBehavior;
      return this;
    }

    public Builder withFrameSkip(int frameSkip) {
      this.frameSkip = frameSkip;
      return this;
    }

    public Simulator build() {
      return new Simulator(frameSkip, playerABehavior, playerBBehavior);
    }
  }
}
