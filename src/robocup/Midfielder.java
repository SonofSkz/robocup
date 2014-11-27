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
public class Midfielder implements ControllerPlayer {
    private static int count = 0;
    // private static Logger log = Logger.getLogger(Simple.class);
    private Random random = null;
    
    private final double randPos;
    private boolean canSeeOwnGoal = false;
    private boolean canSeeNothing = true;
    private boolean canSeeBall = false;
    private PlayMode playMode = null;
    private int playerState = 0;
    private double distanceOtherGoal = 0;
    private boolean canSeeOtherGoal = false;
    private double directionOtherGoal = 0;
    private final ArrayList<PlayerData> visibleOwnPlayers;
    private final ArrayList<PlayerData> visibleOtherPlayers;
    private double directionBall;
    private double directionOwnGoal;
    private double distanceBall;
    private double distanceOwnGoal;
    private double distanceCentre;
    private double directionCentre;
    private boolean canSeeCentre;
    private ActionsPlayer player;
    private final int REACTION_DISTANCE = 20;
    private final int HOME_DISTANCE;
    private final int HOME_MAX = 55;
    private final int HOME_MIN = 45;
    /**
    * Constructs a new simple client.
    */
    public Midfielder() {
        random = new Random(System.currentTimeMillis() + count);
        visibleOwnPlayers = new ArrayList();
        visibleOtherPlayers = new ArrayList();
        Random gen = new Random();
        randPos = gen.nextInt(90) - 45;
        HOME_DISTANCE = gen.nextInt(HOME_MAX - HOME_MIN) + HOME_MIN;
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
        ///before the beginning of the game get all players to rotate and 'find' all of the
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
            //like the defender and goalie behaviour, turns to face the ball in place
            turnTowardBall();
            //the the player moves to intercept the ball if the ball gets to a certain distance away from the player
            if(distanceBall <= REACTION_DISTANCE) playerState = 1;
        }
        if(playerState == 1){             
            dribbleTowardOtherGoal(); //the player dribbles the ball towards the goal and passes it to the nearest players
            //the state changes when the player moves too far away from his default position
            if(distanceOwnGoal > HOME_DISTANCE + REACTION_DISTANCE) playerState = 2;
        }
        if(playerState == 2){
            //the player moves to the default position
            returnHome();
            //the player returns to the idle state when he is at the default position
            if(distanceOwnGoal <= HOME_DISTANCE) playerState = 0;
        }
    }
    
    private void dribbleTowardOtherGoal(){
        //lightly kicks the ball if it is close enough
        turnTowardBall();
        if(distanceBall < 0.7){
            passOn();
        }
        getPlayer().dash(60);
    }
    
    //moves the player towards the own goal
    private void returnHome(){
        turnTowardOwnGoal();
        getPlayer().dash(randomDashValueFast());
    }

    //makes the player moe towards the closest enemy player and stands next to them hoping to intercept the ball
    //in times of a free kick etc...
    private void markOtherPlayer(){
        PlayerData closestEnemy = getNearFarPlayers(visibleOtherPlayers).getLeft();
        getPlayer().turn(closestEnemy.getDirectionTo());
        getPlayer().dash(50);
    }
    
    private void passOn(){
        //loop that finds all the enemy players that are less than 10 away from this player
        ArrayList<PlayerData> closestEnemies = new ArrayList();
        for(PlayerData e : visibleOtherPlayers){
            if(e.getDistanceTo() < 10) closestEnemies.add(e);
        } 
        if(closestEnemies.size() >= 4) getPlayer().kick(70, getNearFarPlayers(visibleOwnPlayers).getRight().getDirectionTo()); // if there are more than 4 enemies around the player then kick the ball hard to the furthest player from this player
        else getPlayer().kick(30, getNearFarPlayers(visibleOwnPlayers).getLeft().getDirectionTo()); // else pass the ball lightly towards the closest friendsly player
    }
    
    //method that returns a Pair object consising of the closest player of either team on the left side of the pair and the
    //furthest on the right side of the pair
    private Pair<PlayerData, PlayerData> getNearFarPlayers(ArrayList<PlayerData> players){
        PlayerData closestPlayer = new PlayerData(); // make a new place to record the closest player
        PlayerData furthestPlayer = new PlayerData(); // make a new place to record the furthest player
        closestPlayer.setDistanceTo(104); // set the distance of the closest one to the maximum distance
        furthestPlayer.setDistanceTo(0); // set the distance of the furthest one to the minimum distance
        for(PlayerData x : players){ //for each player in the vidible players arrayList,
            if(x.getDistanceTo() < closestPlayer.getDistanceTo()){
                closestPlayer = x; // the closest player in the list is saved to the closestPlayer variable
            }
            if(x.getDistanceTo() > furthestPlayer.getDistanceTo()){
                furthestPlayer = x; // the furthest player in the list is saved to the furthestPlayer variable
            }
        }
        //save the details to a pair
        Pair<PlayerData, PlayerData> p = new Pair();
        p.setLeft(closestPlayer);
        p.setRight(furthestPlayer);
        return p;
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
     * Randomly choose a slow dash value.
     * @return
     */
    private int randomDashValueSlow() {
        return -10 + random.nextInt(50);
    }

    /**
     * Turn towards the ball if the player can see it, else turn around if it cant.
     */
    private void turnTowardBall() {
        if(!canSeeBall) canSeeNothingAction();
        else getPlayer().turn(directionBall);
    }

    /**
     * Turn towards own goal if the player can see it, else turn around if it cant.
     */
    private void turnTowardOwnGoal() {
        if(!canSeeOwnGoal) canSeeNothingAction();
        else getPlayer().turn(directionOwnGoal + randPos);
    }

    /**
     * Randomly choose a kick direction.
     * @return
     */
    private int randomKickDirectionValue() {
        return -45 + random.nextInt(90);
    }
    
    private void turnTowardOtherGoal(){
        //if the player can see the goal, turn towards it, else turn around looking for it
        if(!canSeeOtherGoal) canSeeNothingAction();
        else getPlayer().turn(directionOtherGoal);
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
