package ao.chess.v2.engine.run;

/**
 * User: aostrovsky
 * Date: 18-Oct-2009
 * Time: 11:07:37 PM
 */
public class Config
{
    //--------------------------------------------------------------------
    private Config() {}


    //--------------------------------------------------------------------
    private static String workingDirectory = "";


    //--------------------------------------------------------------------
    public static String workingDirectory() {
        return workingDirectory;
    }

    public static void setWorkingDirectory(String workingDir) {
        workingDirectory = workingDir;
    }
}
