import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

public class RollForShoes
{
    static Scanner userInput;
    static ArrayList<Character> cast;

    /* Runs the main program, displaying the initial flavortext,
     * assigning the scanner and cast array, and prompts a file
     * to be selected, if any exist.
     */
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
    /* Searches the save directory for existing JSONs to be loaded,
     * otherwise directs the user to create a new save (option is
     * also offered even if saves are detected).
     */
    public static void promptFile()
    {
        String[] pathnames;
        String root = "C:/Users/HP/Documents/Github/Roll-for-Shoes-Aid/Save Files/";
        File f = new File(root);

        FilenameFilter filter = new FilenameFilter() 
        {
            @Override
            public boolean accept(File f, String name) 
            {
                return name.endsWith("_save.json");
            }
        };

        pathnames = f.list(filter);

        if (pathnames == null)
        {
            System.out.println("Loading Saves... \n"
                                + "No saves found, creating a new save slot...");
            buildCast();
        }
        else
        {
            int fileChoice;

            System.out.print("Loading Saves...");
            while (true)
            {
                try
                {
                    System.out.println("\nSave(s) found, select a file [#] or '0' to create a new save: \n"
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
                        System.out.println("\n!!! {INPUT INVALID} !!!");
                    else if (confirmInput(String.valueOf(fileChoice)))
                    {
                        if (fileChoice == 0)
                        {
                            System.out.println("\nCreating a new save slot...");
                            buildCast();
                            break;
                        }
                        else
                        {
                            System.out.println("\nLoading save slot...");
                            loadCast(root + pathnames[fileChoice - 1]);
                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                    System.out.println("\n!!! {INPUT INVALID} !!!");
                    userInput.next();
                    e.printStackTrace();
                }
            }
        }
    }

    public static void loadCast(String path)
    {
        System.out.println(path);
        JSONParser jsonParser = new JSONParser();
         
        try (FileReader reader = new FileReader(path))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            JSONArray castList = (JSONArray) obj;

            for (int i = 0; i < castList.size(); i++)
            {
                JSONObject playerChar = (JSONObject)castList.get(i);
                parseSaveFile(playerChar);
            }
        }
        catch (IOException e) 
        {
            System.out.println("\n!!! {ERROR READING FILE} !!!");
            e.printStackTrace();
        } 
        catch (ParseException e) 
        {
            System.out.println("\n!!! {ERROR READING FILE} !!!");
            e.printStackTrace();
        }

        // load the JSON file the user chose
        // extract the # of PCs in the file, for each one...
        // either extract the data into variables and run a constructor...
        // or find a way to turn the structure into an object directly
        // add each 
    }

    public static void parseSaveFile(JSONObject character)
    {
        //Get player character object within list
        JSONObject charObject = (JSONObject) character.get("character");
         
        String name = (String) charObject.get("name");    
        System.out.println(name);

        ArrayList<String> pronouns = new ArrayList<String>();
        JSONArray jPronouns = (JSONArray) charObject.get("pronouns");
        for (Object jPnoun : jPronouns) 
        {
            System.out.println(jPnoun.toString());
            pronouns.add(jPnoun.toString());
        }

        String skill;
        long skillLvl;
        JSONArray skills = (JSONArray) charObject.get("skills");
        for (Object ability : skills)
        {
            JSONObject jAbility = (JSONObject) ability;
            skill = (String) jAbility.get("skill");
            skillLvl = (long) jAbility.get("level");
            System.out.println(skill + ", lvl: " + skillLvl);
        }
        
        long xp = (long) charObject.get("xp");    
        System.out.println(xp);

        String itemName;
        String itemDesc;
        JSONArray inventory = (JSONArray) charObject.get("inventory");
        for (Object item : inventory)
        {
            JSONObject jItem = (JSONObject) item;
            itemName = (String) jItem.get("item");
            itemDesc = (String) jItem.get("description");
            System.out.println(itemName + ": \t" + itemDesc);
        }
    }

    public static void buildCast()
    {
        // ask for number of characters, for each character...
        // ask for name & pronouns
        // initialize xp to 0, empty inventory, single skill: Do Something (1) then...
        // display cast list, allow editing before confirming
        // add characters to players[] array
    }

    
    // ========== INPUT ==========
    /* After selecting a option from multiple presented, the user
     * will be asked to verify their choice; a return of true breaks
     * and submits choice, false will re-prompt with the question.
     */
    public static boolean confirmInput(String input)
    {
        String confirm;
        String border = "--------------------------------";
        for (int i = 0; i < input.length() + 1; i++)
        {
            border += "-";
        }

        while (true)
        {
            try
            {
                System.out.println("\nConfirm your selection [Y/N]: '" 
                                    + input + "'? \n"
                                    + border);

                System.out.print(">_: ");
                confirm = userInput.next();
                String confirmL = confirm.toLowerCase();

                if (!(confirmL.equals("y") || confirmL.equals("n")))
                    System.out.println("\n!!! {INPUT INVALID} !!!");
                else if (confirmL.equals("y"))
                    return true;
                else
                    return false;
            }
            catch (Exception e)
            {
                System.out.println("\n!!! {INPUT INVALID} !!!");
                userInput.next();
            }
        }
    }


    // ========== GAMEPLAY ==========

}