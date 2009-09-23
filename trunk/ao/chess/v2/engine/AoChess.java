package ao.chess.v2.engine;

import ao.RandomBot;
import ao.chess.v2.engine.simple.RandomPlayer;
import ao.chess.v2.engine.simple.SimPlayer;
import ao.chess.v2.engine.uct.UctPlayer;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import ao.chess.v2.state.Status;
import util.Io;

import java.io.IOException;
import java.util.Arrays;

/**
 * User: aostrovsky
 * Date: 14-Sep-2009
 * Time: 4:02:37 PM
 *
"C:\Program Files\Java\jdk1.6.0_14\bin\java.exe -server -Xmx512m -jar C:\~\proj\personal\chess\chess.jar uct"
"C:\Program Files\Java\jdk1.6.0_14\bin\java.exe -server -Xmx512m -jar C:\~\proj\personal\chess\chess.jar sim"
"C:\Program Files\Java\jdk1.6.0_14\bin\java.exe -server -Xmx512m -jar C:\~\proj\personal\chess\chess.jar random"
 *
 */
public class AoChess {
    //--------------------------------------------------------------------
    private static final String WINBOARD_TAG = "xboard";


    // feature - A few special commands that can be sent at startup,
    //              more in Tim Mann's guide
    // myname - The name of the engine \" since we need the quotation
    //              marks inside the string
    // usermove - Makes winBoard send "usermove Ne4" instead of just "Ne4"
    // setboard - Uses the setboard feature instead of edit
    // colors - Disables the white/black commands since Mediocre
    //              does not use the anyway
    // analyze - Mediocre does not support analyze mode in winboard yet
    // done - We are now done with starting up and can begin
    private static final String WINBOARD_INIT =
            "feature myname=\"alexo2\" usermove=1 " +
            "setboard=1 colors=0 analyze=0 done=1";

    private static final String WINBOARD_NEW      = "new";
    private static final String WINBOARD_QUIT     = "quit";
    private static final String WINBOARD_SETBOARD = "setboard";
    private static final String WINBOARD_TIME     = "time";
    private static final String WINBOARD_FORCE    = "force";
    private static final String WINBOARD_LEVEL    = "level";


    //--------------------------------------------------------------------
    public static void main(String[] args)
    {
        try
        {
            Io.display( Arrays.toString(args) );
            Player bot = new RandomPlayer();
            String botName = (args.length > 0 ? args[0] : "");
            if (botName.equals("random")) {
                bot = new RandomPlayer();
            } else if (botName.equals("uct")) {
                bot = new UctPlayer(64 * 1024);
            } else if (botName.equals("sim")) {
                bot = new SimPlayer(64 * 1024);
            }
            
//            if (botName.matches("\\d+"))
//            {
//                bot = new UctBot(
//                        Integer.parseInt(botName), true);
//            }
//            if (botName.equals("opt"))
//            {
//                bot = new UctBot(1024*16, true);
//            }
            
            while (winboard(bot)) {}
        }
        catch (Throwable t)
        {
            Io.display( t );
            Io.display( Arrays.toString(t.getStackTrace()) );
            t.printStackTrace();
        }
    }


    //--------------------------------------------------------------------
    private static boolean winboard(Player bot) throws IOException
    {
        State state = new State();
//		board.setupStart();

//        long time = System.currentTimeMillis();
//        int mv = Mediocre.receiveMove("e2e4", board);
//        board.makeMove( mv );
//        new UctBot(1024).act( board );
//        System.out.println("took " + (System.currentTimeMillis() - time));

        String command = Io.read();
        assert command.equals(WINBOARD_TAG)
                : "must use WinBoard protocol";
        Io.write(WINBOARD_INIT);

        int     moveTime  = 0;
        int     increment = 0;
        int     timeLeft  = 0;
        boolean force     = false;
        while (true)
		{
            command = Io.read();

            if(command.equals( WINBOARD_TAG ))
			{
				Io.write(WINBOARD_INIT);
			}

            else if (command.equals( WINBOARD_NEW ))
            {
                force = false;
                state = new State();
            }

            else if (command.equals( WINBOARD_QUIT ))
            {
                return false;
            }

            else if (command.startsWith( WINBOARD_SETBOARD ))
            {
                state.loadFen( command.substring(9) );
            }

            else if (command.startsWith( WINBOARD_TIME ))
            {
                // Winboard reports time left in centiseconds,
                //  transform to milliseconds
				try
				{
					timeLeft =
                            Integer.parseInt(command.substring(5)) * 10;
				}
				catch (NumberFormatException ex) {timeLeft = 0;}
            }

            else if(command.startsWith( WINBOARD_LEVEL ))
			{
				String[] splitString = command.split(" ");
				try
				{
					// Winboard reports increment in full seconds,
                    //  transform to milliseconds
					increment = Integer.parseInt(splitString[3]) * 1000;
				}
				catch(ArrayIndexOutOfBoundsException ex)
				{
					increment = 0;
                }
				catch(NumberFormatException ignored) {}
			}

            else if(command.equals( WINBOARD_FORCE ))
			{
				force = true;
			}

            else if(command.startsWith("st"))
			{
				try
				{
					moveTime =
                        Integer.parseInt(command.substring(3)) * 1000;
				}
				catch(NumberFormatException ex) {moveTime = 0;}
			}

//            else if(command.startsWith("sd"))
//			{
//				try
//				{
//					searchDepth = Integer.parseInt(command.substring(3));
//				}
//				catch(NumberFormatException ignored) {}
//			}

            // Opponent played a move or told us to play from the position
			else if("go".equals( command ) ||
                    command.startsWith("usermove"))
			{
				if(command.equals("go")) force = false;
				if(command.startsWith("usermove"))
				{
                    String moveCommand = command.substring(9);
                    forceMove(state, moveCommand);
                    if (gameIsDone(state)) return true;
				}

				if (! force)
				{
                    playMove(state, bot, timeLeft, moveTime, increment);
                    gameIsDone(state);
				}
			}
        }
    }


    //--------------------------------------------------------------------
    private static void playMove(
            State state, Player bot,
            int timeLeft, int moveTime, int timeIncrement)
    {
        int move = bot.move(state, timeLeft, moveTime, timeIncrement);
        if (move != -1) // We have found a move to make
        {
            Move.apply(move, state);
            Io.write("move " + Move.toInputNotation(move));
            Io.display("playing " + Move.toInputNotation(move) +
                        " :: " + Move.toString(move));
        }
        else {
            Io.display("could not move in:\n" + state);
        }
    }


    //--------------------------------------------------------------------
    private static boolean gameIsDone(State state)
    {
        Status outcome = state.knownStatus();
        if (outcome != Status.IN_PROGRESS) {
            Io.write(outcome);
            Io.display(outcome);
            return true;
        }
        return false;
    }


    //--------------------------------------------------------------------
    private static void forceMove(State state, String moveCommand)
    {
        // Receive the move and play it on the board
        int move = asMove(state, moveCommand);
        if(move == -1)
        {
            Io.write(
                    "The move " + moveCommand +
                    " could not be found. " +
                        "Waiting for new command.");
            Io.display(
                    "The move " + moveCommand +
                    " could not be found. " +
                        "Waiting for new command.");
        }
        else
        {
            Move.apply(move, state);
            Io.display("forcing " + Move.toInputNotation(move) + 
                        " :: " + Move.toString(move));
        }
    }



    //--------------------------------------------------------------------
    private static int asMove(
            State state, String moveCommand) {
        int[] legalMoves = new int[128];
        int nMoves = state.legalMoves(legalMoves);
        for (int n = 0; n < nMoves; n++) {
            int move = legalMoves[ n ];
            if (Move.toInputNotation(move)
                    .equals(moveCommand)) {
                return move;
            }
        }
        return -1;
    }
}
