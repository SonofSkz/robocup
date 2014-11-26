//package robocup;
////~--- non-JDK imports --------------------------------------------------------
//
//import com.github.robocup_atan.atan.model.ActionsPlayer;
//import com.github.robocup_atan.atan.model.ControllerPlayer;
//import com.github.robocup_atan.atan.model.enums.Errors;
//import com.github.robocup_atan.atan.model.enums.Flag;
//import com.github.robocup_atan.atan.model.enums.Line;
//import com.github.robocup_atan.atan.model.enums.Ok;
//import com.github.robocup_atan.atan.model.enums.PlayMode;
//import com.github.robocup_atan.atan.model.enums.RefereeMessage;
//import com.github.robocup_atan.atan.model.enums.ServerParams;
//import com.github.robocup_atan.atan.model.enums.ViewAngle;
//import com.github.robocup_atan.atan.model.enums.ViewQuality;
//import com.github.robocup_atan.atan.model.enums.Warning;
//import java.util.ArrayList;
//
////import org.apache.log4j.Logger;
//
////~--- JDK imports ------------------------------------------------------------
//
//import java.util.HashMap;
//import java.util.Random;
//
///**
// * A simple controller. It implements the following simple behaviour. If the
// * client sees nothing (it might be out of the field) it turns 180 degree. If
// * the client sees the own goal and the distance is less than 40 and greater
// * than 10 it turns to his own goal and dashes. If it cannot see the own goal
// * but can see the ball it turns to the ball and dashes. If it sees anything but
// * not the ball or the own goals it dashes a little bit and turns a fixed amount
// * of degree to the right.
// *
// * @author Atan
// */
//public class Simple implements ControllerPlayer {
//    private static int    count         = 0;
////    private static Logger log           = Logger.getLogger(Simple.class);
//    private Random        random        = null;
//    private boolean       canSeeOwnGoal = false;
//    private boolean       canSeeNothing = true;
//    private boolean       canSeeBall    = false;
//    
//    private PlayMode playMode = null;
//    
//    private boolean goalie = false;
//    private int playerState = 0;
//    private double distanceOtherGoal = 0;
//    private boolean canSeeOtherGoal = false;
//    private double directionOtherGoal = 0;
//    private final ArrayList<PlayerData> visibleOwnPlayers;
//    private final ArrayList<PlayerData> visibleOtherPlayers;
//    
//    private double        directionBall;
//    private double        directionOwnGoal;
//    private double        distanceBall;
//    private double        distanceOwnGoal;
//    private double        distanceCentre;
//    private double        directionCentre;
//    private boolean       canSeeCentre;
//    private ActionsPlayer player;
//
//    /**
//     * Constructs a new simple client.
//     */
//    public Simple() {
//        random = new Random(System.currentTimeMillis() + count);
//        visibleOwnPlayers = new ArrayList();
//        visibleOtherPlayers = new ArrayList();
////        distanceBall = 50;
//        count++;
//    }
//
//    /** {@inheritDoc}
//     * @return  */
//    @Override
//    public ActionsPlayer getPlayer() {
//        return player;
//    }
//
//    /** {@inheritDoc}
//     * @param p */
//    @Override
//    public void setPlayer(ActionsPlayer p) {
//        player = p;
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void preInfo() {
//        canSeeOwnGoal = false;
//        canSeeOtherGoal = false;
//        canSeeBall    = false;
//        goalie = getPlayer().getNumber() == 1;
//        canSeeNothing = true;
//        visibleOwnPlayers.clear();
//        visibleOtherPlayers.clear();
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void postInfo() {
//        //before the beginning of the game get all players to rotate and 'find' all of the
//        //in game components
//        if(playMode == PlayMode.BEFORE_KICK_OFF){
//            canSeeNothingAction();
//        }else{ // continue with normal behaviour
//            //goalie behaviour
//            generalBehaviour();
//            
//        }
//    }
//    
//    
//    
//    private void generalBehaviour(){
//        //finds distance between the closest player and the ball
//        double distanceClosestToBall = 104;
//        if(!canSeeBall) playerState = 0;
//        //the first state is the player trying to find the ball if it cannot see it
//        if(playerState == 0){
//            if(!canSeeBall) canSeeNothingAction();
//            //switches to the next state if the player can see the ball
//            if(canSeeBall) playerState = 1;
//        }
//        //this state determines whether or not to go after the ball
//        if(playerState == 1){
//            //searches through the entire list to find which player is closest to the ball using
//            //an arraylist of PlayerDatas (which are used to record relevant information on the players
//            for(PlayerData p : visibleOwnPlayers)
//                if(p.getDistanceTo() - distanceBall < distanceClosestToBall)
//                    distanceClosestToBall = p.getDistanceTo() - distanceBall;
//            if(distanceClosestToBall > distanceBall) playerState = 2;
//            //the player switches state if it is not the closest person on his team to the ball
//            else playerState = 3;
//        }if(playerState == 2){
//            //while the ball is in the player's possession (while the player is the closest one to the ball)
//            // it dribbles it towards the goal
//            dribbleTowardOtherGoal();
//        }if(playerState == 3){
//            getClear();
//        }
//    }
//    
//    private void dribbleTowardOtherGoal(){
//        turnTowardBall();
//        if(distanceBall < 0.7){
//            if(canSeeOtherGoal){
//                if(distanceOtherGoal < 30) getPlayer().kick(100, directionOtherGoal);
//                else getPlayer().kick(40, directionOtherGoal);
//            }else getPlayer().turnNeck(90);
//        }
//        getPlayer().dash(60);
//    }
//    
//    private void markOtherPlayer(){
//        PlayerData closestEnemy = getClosestEnemy();
//        getPlayer().turn(closestEnemy.getDirectionTo());
//        getPlayer().dash(50);
//    }
//    
//    private void getClear(){
//        PlayerData closestEnemy = getClosestEnemy();
//        if(closestEnemy.getDistanceTo() < 10){
//            getPlayer().turn(closestEnemy.getDirectionTo() + 90);
//        }
//    }
//    
//    private PlayerData getClosestEnemy(){
//        PlayerData closestEnemy = new PlayerData();
//        closestEnemy.setDistanceTo(104);
//        for(PlayerData x : visibleOtherPlayers){
//            if(x.getDistanceTo() < closestEnemy.getDistanceTo()){
//                closestEnemy = x;
//            }
//        }
//        return closestEnemy;
//    }
//    
//    private void canSeeBallAction() {
//        getPlayer().dash(this.randomDashValueFast());
//        turnTowardBall();
//        if (distanceBall < 0.7 && distanceOtherGoal < 20) {
//            getPlayer().kick(100, directionOtherGoal);
//        } else if (distanceBall < 0.8){
//            getPlayer().kick(20, directionOtherGoal);
//        }
////        if (log.isDebugEnabled()) {
////            log.debug("b(" + directionBall + "," + distanceBall + ")");
////        }
//    }
//    
////    private void canSeeBallActionGoalie() {
////        getPlayer().dash(this.randomDashValueFast());
////        turnTowardBall();
////        
////        if (distanceBall < 0.7 && distanceOtherGoal < 20) {
////            getPlayer().kick(100, directionOtherGoal);
////        } else if (distanceBall < 0.8){
////            getPlayer().kick(20, directionOtherGoal);
////        }
//////        if (log.isDebugEnabled()) {
//////            log.debug("b(" + directionBall + "," + distanceBall + ")");
//////        }
////    }
//
//    /** {@inheritDoc}
//     * @param flag
//     * @param distance
//     * @param direction
//     * @param distChange
//     * @param dirChange
//     * @param bodyFacingDirection
//     * @param headFacingDirection */
//    @Override
//    public void infoSeeFlagRight(Flag flag, double distance, double direction, double distChange, double dirChange,
//                                 double bodyFacingDirection, double headFacingDirection) {
//        canSeeNothing = false;
//    }
//
//    /** {@inheritDoc}
//     * @param flag
//     * @param distance
//     * @param direction
//     * @param distChange
//     * @param dirChange
//     * @param bodyFacingDirection
//     * @param headFacingDirection*/
//    @Override
//    public void infoSeeFlagLeft(Flag flag, double distance, double direction, double distChange, double dirChange,
//                                double bodyFacingDirection, double headFacingDirection) {
//        canSeeNothing = false;
//    }
//
//    /** {@inheritDoc}
//     * @param flag
//     * @param distance
//     * @param dirChange
//     * @param direction
//     * @param distChange
//     * @param bodyFacingDirection
//     * @param headFacingDirection */
//    @Override
//    public void infoSeeFlagOwn(Flag flag, double distance, double direction, double distChange, double dirChange,
//                               double bodyFacingDirection, double headFacingDirection) {
//        canSeeNothing = false;
//    }
//
//    /** {@inheritDoc}
//     * @param flag
//     * @param distance
//     * @param direction
//     * @param distChange
//     * @param dirChange
//     * @param bodyFacingDirection
//     * @param headFacingDirection */
//    @Override
//    public void infoSeeFlagOther(Flag flag, double distance, double direction, double distChange, double dirChange,
//                                 double bodyFacingDirection, double headFacingDirection) {
//        canSeeNothing = false;
//    }
//
//    /** {@inheritDoc}
//     * @param flag
//     * @param distance
//     * @param distChange
//     * @param direction
//     * @param dirChange
//     * @param bodyFacingDirection
//     * @param headFacingDirection */
//    @Override
//    public void infoSeeFlagCenter(Flag flag, double distance, double direction, double distChange, double dirChange,
//                                  double bodyFacingDirection, double headFacingDirection) {
//        canSeeNothing = false;
//        this.distanceCentre = distance;
//        this.directionCentre= direction;
//        canSeeCentre = true;
//    }
//
//    /** {@inheritDoc}
//     * @param flag
//     * @param distance
//     * @param direction
//     * @param distChange
//     * @param dirChange
//     * @param bodyFacingDirection
//     * @param headFacingDirection */
//    @Override
//    public void infoSeeFlagCornerOwn(Flag flag, double distance, double direction, double distChange, double dirChange,
//                                     double bodyFacingDirection, double headFacingDirection) {
//        canSeeNothing = false;
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoSeeFlagCornerOther(Flag flag, double distance, double direction, double distChange,
//                                       double dirChange, double bodyFacingDirection, double headFacingDirection) {
//        canSeeNothing = false;
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoSeeFlagPenaltyOwn(Flag flag, double distance, double direction, double distChange,
//                                      double dirChange, double bodyFacingDirection, double headFacingDirection) {
//        canSeeNothing = false;
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoSeeFlagPenaltyOther(Flag flag, double distance, double direction, double distChange,
//            double dirChange, double bodyFacingDirection, double headFacingDirection) {
//        canSeeNothing = false;
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoSeeFlagGoalOwn(Flag flag, double distance, double direction, double distChange, double dirChange,
//                                   double bodyFacingDirection, double headFacingDirection) {
//        canSeeNothing = false;
//        if (flag == Flag.CENTER) {
//            this.canSeeOwnGoal    = true;
//            this.distanceOwnGoal  = distance;
//            this.directionOwnGoal = direction;
//        }
//    }
//
//    /** {@inheritDoc}*/
//    @Override
//    public void infoSeeFlagGoalOther(Flag flag, double distance, double direction, double distChange, double dirChange,
//                                     double bodyFacingDirection, double headFacingDirection) {
//        this.distanceOtherGoal = distance;
//        this.canSeeOtherGoal = true;
//        this.directionOtherGoal = direction;
//        this.canSeeNothing = false;
//        this.distanceOwnGoal = 104 - this.distanceOtherGoal;
////        double y = distance*Math.sin(direction);
////        double z = distance*Math.cos(direction);
////        double a = (double)(104-z);
////        double b = Math.sqrt(a*a + y*y);
////        double top = y*y + b*b - a*a;
////        double bot = 2*y*b;
////        this.directionOwnGoal = Math.acos(top/bot) + 90;
////        this.distanceOwnGoal = b;
////        System.out.println(this.directionOwnGoal);
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoSeeLine(Line line, double distance, double direction, double distChange, double dirChange,
//                            double bodyFacingDirection, double headFacingDirection) {
//        canSeeNothing = false;
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoSeePlayerOther(int number, boolean goalie, double distance, double direction, double distChange,
//                                   double dirChange, double bodyFacingDirection, double headFacingDirection) {
//        PlayerData pd = new PlayerData(distance, direction);
//        visibleOtherPlayers.add(pd);
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoSeePlayerOwn(int number, boolean goalie, double distance, double direction, double distChange,
//                                 double dirChange, double bodyFacingDirection, double headFacingDirection) {
//        
//        PlayerData p = new PlayerData(distance, direction);
//        visibleOwnPlayers.add(p);
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoSeeBall(double distance, double direction, double distChange, double dirChange,
//                            double bodyFacingDirection, double headFacingDirection) {
//        canSeeNothing      = false;
//        this.canSeeBall    = true;
//        this.distanceBall  = distance;
//        this.directionBall = direction;
//    }
//
//    /** {@inheritDoc}
//     * @param refereeMessage */
//    @Override
//    public void infoHearReferee(RefereeMessage refereeMessage) {}
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoHearPlayMode(PlayMode playMode) {
//        this.playMode = playMode;
//        if (playMode == PlayMode.BEFORE_KICK_OFF) {
//            this.pause(1000);
//            switch (this.getPlayer().getNumber()) {
//                case 1 :
//                    this.getPlayer().move(-50, -0);
//                    break;
//                case 2 :
//                    this.getPlayer().move(-10, 10);
//                    break;
//                case 3 :
//                    this.getPlayer().move(-10, -10);
//                    break;
//                case 4 :
//                    this.getPlayer().move(-20, 0);
//                    break;
//                case 5 :
//                    this.getPlayer().move(-20, 10);
//                    break;
//                case 6 :
//                    this.getPlayer().move(-20, -10);
//                    break;
//                case 7 :
//                    this.getPlayer().move(-20, 20);
//                    break;
//                case 8 :
//                    this.getPlayer().move(-20, -20);
//                    break;
//                case 9 :
//                    this.getPlayer().move(-30, 0);
//                    break;
//                case 10 :
//                    this.getPlayer().move(-40, 10);
//                    break;
//                case 11 :
//                    this.getPlayer().move(-40, -10);
//                    break;
//                default :
//                    throw new Error("number must be initialized before move");
//            }
//        }
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoHearPlayer(double direction, String message) {}
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoSenseBody(ViewQuality viewQuality, ViewAngle viewAngle, double stamina, double unknown,
//                              double effort, double speedAmount, double speedDirection, double headAngle,
//                              int kickCount, int dashCount, int turnCount, int sayCount, int turnNeckCount,
//                              int catchCount, int moveCount, int changeViewCount) {}
//
//    /** {@inheritDoc} */
//    @Override
//    public String getType() {
//        return "Simple";
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void setType(String newType) {}
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoHearError(Errors error) {}
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoHearOk(Ok ok) {}
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoHearWarning(Warning warning) {}
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoPlayerParam(double allowMultDefaultType, double dashPowerRateDeltaMax,
//                                double dashPowerRateDeltaMin, double effortMaxDeltaFactor, double effortMinDeltaFactor,
//                                double extraStaminaDeltaMax, double extraStaminaDeltaMin,
//                                double inertiaMomentDeltaFactor, double kickRandDeltaFactor,
//                                double kickableMarginDeltaMax, double kickableMarginDeltaMin,
//                                double newDashPowerRateDeltaMax, double newDashPowerRateDeltaMin,
//                                double newStaminaIncMaxDeltaFactor, double playerDecayDeltaMax,
//                                double playerDecayDeltaMin, double playerTypes, double ptMax, double randomSeed,
//                                double staminaIncMaxDeltaFactor, double subsMax) {}
//
//    @Override
//    public void infoPlayerType(int id, double playerSpeedMax, double staminaIncMax, double playerDecay,
//                               double inertiaMoment, double dashPowerRate, double playerSize, double kickableMargin,
//                               double kickRand, double extraStamina, double effortMax, double effortMin) {}
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoCPTOther(int unum) {}
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoCPTOwn(int unum, int type) {}
//
//    /** {@inheritDoc} */
//    @Override
//    public void infoServerParam(HashMap<ServerParams, Object> info) {}
//
//    /**
//     * This is the action performed when the player can see the ball.
//     * It involves running at it and kicking it...
//     */
//
//    /**
//     * If the player can see anything that is not a ball or a goal, it turns.
//     */
////    private void canSeeAnythingAction() {
////        getPlayer().dash(this.randomDashValueSlow());
////        getPlayer().turn(20);
//////        if (log.isDebugEnabled()) {
//////            log.debug("a");
//////        }
////    }
//
//    /**
//     * If the player can see nothing, it turns 180 degrees.
//     */
//    private void canSeeNothingAction() {
//        getPlayer().turn(90);
////        if (log.isDebugEnabled()) {
////            log.debug("n");
////        }
//    }
//
//    /**
//     * If the player can see its own goal, it goes and stands by it...
//     */
//    private void canSeeOwnGoalAction() {
//        getPlayer().dash(100);
//        turnTowardOwnGoal();
////        if (log.isDebugEnabled()) {
////            log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
////        }
//    }
//
//    /**
//     * Randomly choose a fast dash value.
//     * @return
//     */
//    private int randomDashValueFast() {
//        return 30 + random.nextInt(100);
//    }
//
//    /**
//     * Randomly choose a slow dash value.
//     * @return
//     */
//    private int randomDashValueSlow() {
//        return -10 + random.nextInt(50);
//    }
//
//    /**
//     * Turn towards the ball.
//     */
//    private void turnTowardBall() {
//        getPlayer().turn(directionBall);
//    }
//
//    /**
//     * Turn towards our goal.
//     */
//    private void turnTowardOwnGoal() {
//        getPlayer().turn(directionOwnGoal);
//        
//    }
//
//    /**
//     * Randomly choose a kick direction.
//     * @return
//     */
//    private int randomKickDirectionValue() {
//        return -45 + random.nextInt(90);
//    }
//    
//    private void turnTowardOtherGoal(){
//        getPlayer().turn(directionOtherGoal);
//    }
//
//    /**
//     * Pause the thread.
//     * @param ms How long to pause the thread for (in ms).
//     */
//    private synchronized void pause(int ms) {
//        try {
//            this.wait(ms);
//        } catch (InterruptedException ex) {
////            log.warn("Interrupted Exception ", ex);
//        }
//    }
//}
