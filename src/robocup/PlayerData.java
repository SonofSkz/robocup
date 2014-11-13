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
    private double distanceTo;
    private double directionTo;
    
    public PlayerData(){
        this.distanceTo = 0;
        this.directionTo = 0;
    }
    
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
    
    public void setDistanceTo(double distance){
        this.distanceTo = distance;
    }
    
    public void setDirectionTo(double direction){
        this.directionTo = direction;
    }
}
