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
public class Goalie implements ControllerPlayer {
    private static int    count         = 0;
//    private static Logger log           = Logger.getLogger(Simple.class);
    private Random        random        = null;
    private boolean       canSeeOwnGoal = false;
    private boolean       canSeeNothing = true;
    private boolean       canSeeBall    = false;
    
    private PlayMode playMode = null;
    private int playerState = 0; // the state machine integer
    private double distanceOtherGoal = 0;
    private boolean canSeeOtherGoal = false;
    private double directionOtherGoal = 0;
    private final ArrayList<PlayerData> visibleOwnPlayers; // an arrayList of all the visible players on your team
    private final ArrayList<PlayerData> visibleOtherPlayers; // an arrayList of all the visible players on enemy team
    
    private double        directionBall;
    private double        directionOwnGoal;
    private double        distanceBall;
    private double        distanceOwnGoal;
    private double        distanceCentre;
    private double        directionCentre;
    private boolean       canSeeCentre;
    private ActionsPlayer player;

    /**
     * Constructs a new simple client.
     */
    public Goalie() {
        random = new Random(System.currentTimeMillis() + count);
        visibleOwnPlayers = new ArrayList();
        visibleOtherPlayers = new ArrayList();
//        distanceBall = 50;
        count++;
    }

    /** {@inheritDoc}
     * @return  */
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
        canSeeBall    = false;
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
        }else{ // continue with normal behaviour
            goalieBehaviour();
        }
    }
    
    private void goalieBehaviour(){
        final int REACTION_DISTANCE = 20;
        final int HOME_DISTANCE = 5;

        //the first state has the goalie wait in goal but constantly look for the ball
        if(playerState == 0){
            turnTowardBall();
            //the goalie state changes when the ball moves to a certain distance from the goalie
            if(canSeeBall && distanceBall < REACTION_DISTANCE) playerState = 1;
        }
        //in this state the goalie is actively running towards the ball attempting to intercept it and
        //kick it away if possible
        if(playerState == 1){
            canSeeBallAction();
            //the goalie state changes when the ball leaves a certain range
            if(distanceOwnGoal >= REACTION_DISTANCE) playerState = 2;
        }
        //in this state the goalie moves back to the goal
        if(playerState == 2){
            //the goalie needs to be able to see his/her own goal to move back to it
            if(!canSeeOwnGoal){
                turnTowardOwnGoal();
            }else{
                //the goalie moves back towards the goal
                canSeeOwnGoalAction();
                //the goalie state changes back to the first state of looking for the ball and
                //watching it when it returns to the goal
                if(distanceOwnGoal < HOME_DISTANCE) playerState = 0;
            }
        }
    }
    
    private void canSeeBallAction() {
        //the goalie dashes towards the ball
        getPlayer().dash(100);
        turnTowardBall();
        if (distanceBall < 0.7) {
            // the goalie kicks the ball far away when it is in range of the kick
            getPlayer().kick(100, directionOtherGoal);
        }
//        if (log.isDebugEnabled()) {
//            log.debug("b(" + directionBall + "," + distanceBall + ")");
//        }
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
    }

    /** {@inheritDoc}*/
    @Override
    public void infoSeeFlagGoalOther(Flag flag, double distance, double direction, double distChange, double dirChange,
                                     double bodyFacingDirection, double headFacingDirection) {
        this.distanceOtherGoal = distance;
        this.canSeeOtherGoal = true;
        this.directionOtherGoal = direction;
        this.canSeeNothing = false;
        this.distanceOwnGoal = 104 - this.distanceOtherGoal; //reminds that player that although they cannot see their own
                                                             //goal, it doesn't change position
        //redacted code that tells the player exactly where they are in terms of anything -- scrapped due to possible complexity issues
//        double y = distance*Math.sin(direction);
//        double z = distance*Math.cos(direction);
//        double a = (double)(104-z);
//        double b = Math.sqrt(a*a + y*y);
//        double top = y*y + b*b - a*a;
//        double bot = 2*y*b;
//        this.directionOwnGoal = Math.acos(top/bot) + 90;
//        this.distanceOwnGoal = b;
//        System.out.println(this.directionOwnGoal);
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
        visibleOtherPlayers.add(pd); // adds the distance and direction data of the enemy player
    }

    /** {@inheritDoc} */
    @Override
    public void infoSeePlayerOwn(int number, boolean goalie, double distance, double direction, double distChange,
                                 double dirChange, double bodyFacingDirection, double headFacingDirection) {
        
        PlayerData p = new PlayerData(distance, direction);
        visibleOwnPlayers.add(p); // adds the distance and direction data of the friendly player
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
     * This is the action performed when the player can see the ball.
     * It involves running at it and kicking it...
     */

    /**
     * If the player can see anything that is not a ball or a goal, it turns.
     */
//    private void canSeeAnythingAction() {
//        getPlayer().dash(this.randomDashValueSlow());
//        getPlayer().turn(20);
////        if (log.isDebugEnabled()) {
////            log.debug("a");
////        }
//    }

    /**
     * If the player can see nothing, it turns 180 degrees.
     */
    private void canSeeNothingAction() {
        getPlayer().turn(90);
//        if (log.isDebugEnabled()) {
//            log.debug("n");
//        }
    }

    /**
     * If the player can see its own goal, it goes and stands by it...
     */
    private void canSeeOwnGoalAction() {
        getPlayer().dash(100);
        turnTowardOwnGoal();
//        if (log.isDebugEnabled()) {
//            log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
//        }
    }

    /**
     * Randomly choose a fast dash value.
     * @return
     */
    private int randomDashValueFast() {
        return 30 + random.nextInt(100);
    }

    /**
     * Randomly choose a slow dash value.
     * @return
     */
    private int randomDashValueSlow() {
        return -10 + random.nextInt(50);
    }

    /**
     * Turn towards the ball.
     */
    private void turnTowardBall() {
        getPlayer().turn(directionBall);
    }

    /**
     * Turn towards our goal.
     */
    private void turnTowardOwnGoal() {
        getPlayer().turn(directionOwnGoal);
        
    }

    /**
     * Randomly choose a kick direction.
     * @return
     */
    private int randomKickDirectionValue() {
        return -45 + random.nextInt(90);
    }
    
    private void turnTowardOtherGoal(){
        getPlayer().turn(directionOtherGoal);
    }

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
