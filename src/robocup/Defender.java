package robocup;
//~--- non-JDK imports --------------------------------------------------------
import com.github.robocup_atan.atan.model.ActionsPlayer;
import com.github.robocup_atan.atan.model.ControllerPlayer;
import com.github.robocup_atan.atan.model.enums.Errors;
import com.github.robocup_atan.atan.model.enums.Flag;
import com.github.robocup_atan.atan.model.enums.Line;
import com.github.robocup_atan.atan.model.enums.Ok;
import com.github.robocup_atan.atan.model.enums.PlayMode;
import com.github.robocup_atan.atan.model.enums.RefereeMessage;
import com.github.robocup_atan.atan.model.enums.ServerParams;
import com.github.robocup_atan.atan.model.enums.ViewAngle;
import com.github.robocup_atan.atan.model.enums.ViewQuality;
import com.github.robocup_atan.atan.model.enums.Warning;
import java.util.ArrayList;
//import org.apache.log4j.Logger;
//~--- JDK imports ------------------------------------------------------------
import java.util.HashMap;
import java.util.Random;
/**
* A simple controller. It implements the following simple behaviour. If the
* client sees nothing (it might be out of the field) it turns 180 degree. If
* the client sees the own goal and the distance is less than 40 and greater
* than 10 it turns to his own goal and dashes. If it cannot see the own goal
* but can see the ball it turns to the ball and dashes. If it sees anything but
* not the ball or the own goals it dashes a little bit and turns a fixed amount
* of degree to the right.
*
* @author Atan
*/
public class Defender implements ControllerPlayer {
    private static int count = 0;
    // private static Logger log = Logger.getLogger(Simple.class);
    private Random random = null;
    private boolean canSeeOwnGoal = false;
    private boolean canSeeNothing = true;
    private boolean canSeeBall = false;
    private PlayMode playMode = null;
    private int playerState = 2; // the state machine integer
    private double distanceOtherGoal = 0;
    private boolean canSeeOtherGoal = false;
    private double directionOtherGoal = 0;
    private final ArrayList<PlayerData> visibleOwnPlayers; // an arrayList of all the visible players on your team
    private final ArrayList<PlayerData> visibleOtherPlayers; // an arrayList of all the visible players on enemy team
    private double directionBall;
    private double directionOwnGoal;
    private double distanceBall;
    private double distanceOwnGoal;
    private double distanceCentre;
    private double directionCentre;
    private boolean canSeeCentre;
    private ActionsPlayer player;
    private final int REACTION_DISTANCE = 10; // the distance ]
    private final int HOME_DISTANCE = 20; // similar to the reaction distance behaviour of the goalie, this variable keeps tr-
                                          // ack of where this player needs to be in his 0 state
    /**
    * Constructs a new simple client.
    */
    public Defender() {
        random = new Random(System.currentTimeMillis() + count);
        visibleOwnPlayers = new ArrayList();
        visibleOtherPlayers = new ArrayList();
        // distanceBall = 50;
        count++;
    }
    /** {@inheritDoc}
    * @return */
    @Override
    public ActionsPlayer getPlayer() {
        return player;
    }
    /** {@inheritDoc}
    * @param p */
    @Override
    public void setPlayer(ActionsPlayer p) {
        player = p;
    }
    /** {@inheritDoc} */
    @Override
    public void preInfo() {
        canSeeOwnGoal = false;
        canSeeOtherGoal = false;
        canSeeBall = false;
        canSeeNothing = true;
        visibleOwnPlayers.clear();
        visibleOtherPlayers.clear();
    }
    /** {@inheritDoc} */
    @Override
    public void postInfo() {
        //before the beginning of the game get all players to rotate and 'find' all of the
        //in game components
        if(playMode == PlayMode.BEFORE_KICK_OFF){
            canSeeNothingAction();
        }else if(playMode == PlayMode.CORNER_KICK_OWN || playMode == PlayMode.KICK_OFF_OTHER){
            markOtherPlayer(); // move close to another player to help intercept the ball when play continues
        }else{ // continue with normal behaviour
            behaviour();
        }
    }

    private void behaviour(){
        if(playerState == 0){
            turnTowardBall(); // the first state is about the player standing in place looking for the ball
            if(distanceBall <= REACTION_DISTANCE) playerState = 1; // when the ball comes into range, change state
        }
        if(playerState == 1){                
            getClear(); // kick the ball away from the momentum of  the enemy player coming towards it
            if(distanceOwnGoal >= HOME_DISTANCE + REACTION_DISTANCE) playerState = 2; // when the player leaves a certain range, change state
        }
        if(playerState == 2){
            returnHome(); // return the player to the range it came from
            if(distanceOwnGoal <= HOME_DISTANCE) playerState = 0; // when the player returns home, returns to the default state
        }
    }
    
    private void dribbleTowardOtherGoal(){
        //turns towards the goal
        turnTowardBall();
        if(distanceBall < 0.7){
            if(canSeeOtherGoal){
                //the player kicks the ball towards the goal but relatively lightly so that it doesn't take lokg for it to get
                //to the ball again
                getPlayer().kick(40, directionOtherGoal);
            }else{ getPlayer().turnNeck(90); getPlayer().turnNeck(-90); } // turn neck to keep track of whats going on
        }
        //dashes after the ball
        getPlayer().dash(100);
    }
    
    private void returnHome(){
        //basic code to get the player to return to the required area (go back towards the own goal)
        turnTowardOwnGoal();
        getPlayer().dash(randomDashValueFast());
    }

    private void markOtherPlayer(){
        //get the closest enemy player and go towards it
        PlayerData closestEnemy = getClosestPlayer(visibleOtherPlayers);
        getPlayer().turn(closestEnemy.getDirectionTo());
        getPlayer().dash(100);
    }
    
    private void getClear(){
        PlayerData closestEnemy = getClosestPlayer(visibleOtherPlayers);
        if(closestEnemy.getDistanceTo() < 10 && distanceBall < 0.7){
            //if there is an enemy player that is close to the player, the player clears the ball away in a direction
            //normal to the current facing direction (I assume)
            getPlayer().turn(closestEnemy.getDirectionTo() + 90);
            getPlayer().kick(50, 0);
            //moves the ball further down-pitch if there are no other enemies near
        }else dribbleTowardOtherGoal();
    }
    
    //a method that returns the PlayerData of the closest player on wither team
    private PlayerData getClosestPlayer(ArrayList<PlayerData> players){
        PlayerData closestPlayer = new PlayerData();
        closestPlayer.setDistanceTo(104);
        for(PlayerData x : players){
            if(x.getDistanceTo() < closestPlayer.getDistanceTo()){
                closestPlayer = x;
            }
        }
        return closestPlayer;
    }
    
    /**
     * If the player can see nothing, it turns 90 degrees.
     */
    private void canSeeNothingAction() {
        getPlayer().turn(90);
    }

    /**
     * Randomly choose a fast dash value.
     * @return
     */
    private int randomDashValueFast() {
        return 30 + random.nextInt(100);
    }

    /**
     * Turn towards the ball.
     */
    
    //if the player cannot see the ball turn towards it else look for it
    private void turnTowardBall() {
        if(!canSeeBall) canSeeNothingAction();
        else getPlayer().turn(directionBall);
    }

    /**
     * Turn towards our goal.
     */
    private void turnTowardOwnGoal() {
        if(!canSeeOwnGoal) canSeeNothingAction();
        else getPlayer().turn(directionOwnGoal);
    }
    
    @Override
    public void infoSeeFlagRight(Flag flag, double distance, double direction, double distChange, double dirChange,
                                 double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    @Override
    public void infoSeeFlagLeft(Flag flag, double distance, double direction, double distChange, double dirChange,
                                double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    @Override
    public void infoSeeFlagOwn(Flag flag, double distance, double direction, double distChange, double dirChange,
                               double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    @Override
    public void infoSeeFlagOther(Flag flag, double distance, double direction, double distChange, double dirChange,
                                 double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    @Override
    public void infoSeeFlagCenter(Flag flag, double distance, double direction, double distChange, double dirChange,
                                  double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        this.distanceCentre = distance;
        this.directionCentre= direction;
        canSeeCentre = true;
    }

    @Override
    public void infoSeeFlagCornerOwn(Flag flag, double distance, double direction, double distChange, double dirChange,
                                     double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagCornerOther(Flag flag, double distance, double direction, double distChange,
                                       double dirChange, double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagPenaltyOwn(Flag flag, double distance, double direction, double distChange,
                                      double dirChange, double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagPenaltyOther(Flag flag, double distance, double direction, double distChange,
            double dirChange, double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeFlagGoalOwn(Flag flag, double distance, double direction, double distChange, double dirChange,
                                   double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
        if (flag == Flag.CENTER) {
            this.canSeeOwnGoal    = true;
            this.distanceOwnGoal  = distance;
            this.directionOwnGoal = direction;
        }
        if(!canSeeOtherGoal) this.distanceOtherGoal = 104 - this.distanceOwnGoal;
    }

    /** {@inheritDoc}*/
    @Override
    public void infoSeeFlagGoalOther(Flag flag, double distance, double direction, double distChange, double dirChange,
                                     double bodyFacingDirection, double headFacingDirection) {
        this.distanceOtherGoal = distance;
        this.canSeeOtherGoal = true;
        this.directionOtherGoal = direction;
        this.canSeeNothing = false;
        if(!canSeeOwnGoal) this.distanceOwnGoal = 104 - this.distanceOtherGoal;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeLine(Line line, double distance, double direction, double distChange, double dirChange,
                            double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing = false;
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeePlayerOther(int number, boolean goalie, double distance, double direction, double distChange,
                                   double dirChange, double bodyFacingDirection, double headFacingDirection) {
        PlayerData pd = new PlayerData(distance, direction);
        visibleOtherPlayers.add(pd);
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeePlayerOwn(int number, boolean goalie, double distance, double direction, double distChange,
                                 double dirChange, double bodyFacingDirection, double headFacingDirection) {
        
        PlayerData p = new PlayerData(distance, direction);
        visibleOwnPlayers.add(p);
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeeBall(double distance, double direction, double distChange, double dirChange,
                            double bodyFacingDirection, double headFacingDirection) {
        canSeeNothing      = false;
        this.canSeeBall    = true;
        this.distanceBall  = distance;
        this.directionBall = direction;
    }

    /** {@inheritDoc}
     * @param refereeMessage */
    @Override
    public void infoHearReferee(RefereeMessage refereeMessage) {}

    /** {@inheritDoc} */
    @Override
    public void infoHearPlayMode(PlayMode playMode) {
        this.playMode = playMode;
        if (playMode == PlayMode.BEFORE_KICK_OFF) {
            this.pause(1000);
            switch (this.getPlayer().getNumber()) {
                case 1 :
                    this.getPlayer().move(-50, -0);
                    break;
                case 2 :
                    this.getPlayer().move(-10, 10);
                    break;
                case 3 :
                    this.getPlayer().move(-10, -10);
                    break;
                case 4 :
                    this.getPlayer().move(-20, 0);
                    break;
                case 5 :
                    this.getPlayer().move(-20, 10);
                    break;
                case 6 :
                    this.getPlayer().move(-20, -10);
                    break;
                case 7 :
                    this.getPlayer().move(-20, 20);
                    break;
                case 8 :
                    this.getPlayer().move(-20, -20);
                    break;
                case 9 :
                    this.getPlayer().move(-30, 0);
                    break;
                case 10 :
                    this.getPlayer().move(-40, 10);
                    break;
                case 11 :
                    this.getPlayer().move(-40, -10);
                    break;
                default :
                    throw new Error("number must be initialized before move");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void infoHearPlayer(double direction, String message) {}

    /** {@inheritDoc} */
    @Override
    public void infoSenseBody(ViewQuality viewQuality, ViewAngle viewAngle, double stamina, double unknown,
                              double effort, double speedAmount, double speedDirection, double headAngle,
                              int kickCount, int dashCount, int turnCount, int sayCount, int turnNeckCount,
                              int catchCount, int moveCount, int changeViewCount) {}

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return "Simple";
    }

    /** {@inheritDoc} */
    @Override
    public void setType(String newType) {}

    /** {@inheritDoc} */
    @Override
    public void infoHearError(Errors error) {}

    /** {@inheritDoc} */
    @Override
    public void infoHearOk(Ok ok) {}

    /** {@inheritDoc} */
    @Override
    public void infoHearWarning(Warning warning) {}

    /** {@inheritDoc} */
    @Override
    public void infoPlayerParam(double allowMultDefaultType, double dashPowerRateDeltaMax,
                                double dashPowerRateDeltaMin, double effortMaxDeltaFactor, double effortMinDeltaFactor,
                                double extraStaminaDeltaMax, double extraStaminaDeltaMin,
                                double inertiaMomentDeltaFactor, double kickRandDeltaFactor,
                                double kickableMarginDeltaMax, double kickableMarginDeltaMin,
                                double newDashPowerRateDeltaMax, double newDashPowerRateDeltaMin,
                                double newStaminaIncMaxDeltaFactor, double playerDecayDeltaMax,
                                double playerDecayDeltaMin, double playerTypes, double ptMax, double randomSeed,
                                double staminaIncMaxDeltaFactor, double subsMax) {}

    @Override
    public void infoPlayerType(int id, double playerSpeedMax, double staminaIncMax, double playerDecay,
                               double inertiaMoment, double dashPowerRate, double playerSize, double kickableMargin,
                               double kickRand, double extraStamina, double effortMax, double effortMin) {}

    /** {@inheritDoc} */
    @Override
    public void infoCPTOther(int unum) {}

    /** {@inheritDoc} */
    @Override
    public void infoCPTOwn(int unum, int type) {}

    /** {@inheritDoc} */
    @Override
    public void infoServerParam(HashMap<ServerParams, Object> info) {}

    /**
     * Pause the thread.
     * @param ms How long to pause the thread for (in ms).
     */
    private synchronized void pause(int ms) {
        try {
            this.wait(ms);
        } catch (InterruptedException ex) {
//            log.warn("Interrupted Exception ", ex);
        }
    }
}
