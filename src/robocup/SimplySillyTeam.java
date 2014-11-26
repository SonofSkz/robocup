package robocup;
//~--- non-JDK imports --------------------------------------------------------

import com.github.robocup_atan.atan.model.AbstractTeam;
import com.github.robocup_atan.atan.model.ControllerCoach;
import com.github.robocup_atan.atan.model.ControllerPlayer;

/**
 * A class to setup a Simple Silly AbstractTeam.
 *
 * @author Atan
 */
public class SimplySillyTeam extends AbstractTeam {

    /**
     * Constructs a new simple silly team.
     *
     * @param name The team name.
     * @param port The port to connect to SServer.
     * @param hostname The SServer hostname.
     * @param hasCoach True if connecting a coach.
     */
    public SimplySillyTeam(String name, int port, String hostname, boolean hasCoach) {
        super(name, port, hostname, hasCoach);
    }

    /**
     * {@inheritDoc}
     *
     * The first controller of the team is silly the others are simple.
     */
    @Override
    public ControllerPlayer getNewControllerPlayer(int number) {
        ControllerPlayer cntrp = new Defender();
        if (number == 0){
            cntrp = new Goalie();    
        } else if (number == 10 || number == 9 || number == 8 || number == 7){
            cntrp = new Defender();
        } else if (number == 1 || number == 2){
            cntrp = new Attacker();
        } else if (number == 3 || number == 4 || number == 5 || number == 6){
            cntrp = new Midfielder();
        }
        return cntrp;
    }

    /**
     * {@inheritDoc}
     *
     * Generates a new coach.
     */
    @Override
    public ControllerCoach getNewControllerCoach() {
        return new Coach();
    }
}
