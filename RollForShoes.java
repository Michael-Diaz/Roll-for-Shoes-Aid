import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

public class RollForShoes
{
    static Scanner userInput;
    static ArrayList<Character> cast;

    public static void main(String[] args)
    {
        userInput = new Scanner(System.in);
        cast = new ArrayList<Character>();

        System.out.println("==== Welcome to the 'Roll for Shoes' Aid! ==== \n\n" 
                            + "'Roll for Shoes' is an easy-to-setup TTRPG \n"
                            + "with a few simple rules, making it great to \n"
                            + "play at a moment's notice and inviting to \n"
                            + "anyone new to TTRPGs as a whole! \n\n"
                            + "For more info on rules and how to play, visit: \n"
                            + "https://rollforshoes.com/ \n\n"
                            + "============================================== \n");
        
        promptFile();
    }


    // ========== STARTUP ==========
    static public void promptFile()
    {
        String[] pathnames;
        File f = new File("C:/Users/HP/Documents/Github/Roll-for-Shoes-Aid/Save Files");

        FilenameFilter filter = new FilenameFilter() 
        {
            @Override
            public boolean accept(File f, String name) 
            {
                return name.endsWith("_save.json");
            }
        };

        pathnames = f.list(filter);

        if (pathnames.length == 0)
            buildCast();
        else
        {
            int fileChoice;

            System.out.println("Loading Saves...");
            while (true)
            {
                try
                {
                    System.out.println("Save(s) found, select a file [#] or '0' to create a new save: \n"
                                        + "-------------------------------------------------------------");

                    int fileNum = 1;
                    for (String pathname : pathnames) 
                    {
                        System.out.println("\t" + fileNum + "). " 
                                            + pathname.substring(0, pathname.length() - 10));
                        fileNum++;
                    }

                    System.out.print(">_: ");
                    fileChoice = userInput.nextInt();

                    if (fileChoice < 0 || fileChoice > pathnames.length)
                        System.out.println("\n!!! {INPUT INVALID} !!!\n");
                    else if (fileChoice == 0)
                    {
                        buildCast();
                        break;
                    }
                    else
                    {
                        loadCast();
                        break;
                    }
                }
                catch (Exception e)
                {
                    System.out.println("\n!!! {INPUT INVALID} !!!\n");
                    userInput.next();
                }
            }
        }

        // selecting a save loads the players[] array with the respective characters else...
        // run buildCast method
    }

    static public void loadCast()
    {

    }

    static public void buildCast()
    {
        // ask for number of characters, for each character...
        // ask for name & pronouns
        // initialize xp to 0, empty inventory, single skill: Do Something (1) then...
        // display cast list, allow editing before confirming
        // add characters to players[] array
    }


    // ========== GAMEPLAY ==========

}