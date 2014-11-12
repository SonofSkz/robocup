/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robocup;

/**
 *
 * @author Mustafa
 */
public class PlayerData {
    private final double distanceTo;
    private final double directionTo;
    
    public PlayerData(double distanceTo, double directionTo){
        this.distanceTo = distanceTo;
        this.directionTo = directionTo;
    }

    public double getDistanceTo() {
        return distanceTo;
    }

    public double getDirectionTo() {
        return directionTo;
    }
}
