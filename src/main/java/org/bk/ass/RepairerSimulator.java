package org.bk.ass;

import static org.bk.ass.AgentUtil.distanceSquared;
import static org.bk.ass.AgentUtil.moveToward;

public class RepairerSimulator {

  // Retrieved from OpenBW
  public static final int SCV_REPAIR_RANGE_SQUARED = 5 * 5;

  public boolean simUnit(Agent agent, UnorderedCollection<Agent> allies) {
    Agent selectedAlly = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;
    for (int i = 0; i < allies.size(); i++) {
      Agent ally = allies.get(i);
      if (ally.isMechanic && ally.healthShifted < ally.maxHealthShifted && ally != agent) {

        int distance = distanceSquared(agent, ally);
        if (distance < selectedDistanceSquared) {
          selectedDistanceSquared = distance;
          selectedAlly = ally;

          // If we can repair it this frame, we're done searching
          if (selectedDistanceSquared <= SCV_REPAIR_RANGE_SQUARED) {
            break;
          }
        }
      }
    }

    if (selectedAlly == null) {
      return false;
    }

    moveToward(agent, selectedAlly, selectedDistanceSquared);
    if (selectedDistanceSquared > SCV_REPAIR_RANGE_SQUARED) {
      return true;
    }
    selectedAlly.healthShifted += selectedAlly.hpConstructionRate;
    if (selectedAlly.healthShifted > selectedAlly.maxHealthShifted) {
      selectedAlly.healthShifted = selectedAlly.maxHealthShifted;
    }

    return true;
  }
}
