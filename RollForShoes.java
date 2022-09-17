import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import java.io.*;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;

public class RollForShoes
{
    static Scanner userInput;
    static ArrayList<PlayerCharacter> cast;

    /* Runs the main program, displaying the initial flavortext,
     * assigning the scanner and cast array, and prompts a file
     * to be selected, if any exist.
     */
    public static void main(String[] args)
    {
        userInput = new Scanner(System.in);
        cast = new ArrayList<PlayerCharacter>();

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
        String root = System.getProperty("user.dir") + "/Save Files/";
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
            buildCast(pathnames);
        }
        else
        {
            int fileChoice;

            System.out.print("Loading Saves...");
            while (true)
            {
                try
                {
                    System.out.println("\nSave(s) found, select a file [#] or enter '0' to create a new save: \n"
                                        + "-------------------------------------------------------------------");

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
                            System.out.print("\nInitializing a new save slot...");
                            buildCast(pathnames);
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
        System.out.println(path + "\n");
        JSONParser jsonParser = new JSONParser();
         
        try (FileReader reader = new FileReader(path))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            JSONArray castList = (JSONArray) obj;

            for (int i = 0; i < castList.size(); i++)
            {
                JSONObject playerChar = (JSONObject)castList.get(i);
                PlayerCharacter castMem = parseSaveFile(playerChar);
                
                castMem.displayStats();
                cast.add(castMem);
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
        catch (Exception e)
        {
            System.out.println("\n!!! {ERROR READING FILE} !!!");
            e.printStackTrace();
        }

        runSession(cast, path);
    }

    public static PlayerCharacter parseSaveFile(JSONObject character)
    {
        //Get player character object within list
        JSONObject charObject = (JSONObject) character.get("character");
         
        ArrayList<String> identifiers = new ArrayList<String>();
        String name = (String) charObject.get("name");    
        identifiers.add(name);

        JSONArray pronouns = (JSONArray) charObject.get("pronouns");
        for (Object pronoun : pronouns)
            identifiers.add(pronoun.toString());

        Skill treeRoot = new Skill();
        boolean first = true;
        JSONArray skills = (JSONArray) charObject.get("skills");
        for (Object ability : skills)
        {
            JSONObject jAbility = (JSONObject) ability;

            String skill = (String) jAbility.get("skill");
            String prevSkill = (String) jAbility.get("previous");

            if (first)
                first = false;
            else
            {
                Skill tempHold = treeRoot.searchSkills(prevSkill);
                if (tempHold == null)
                    treeRoot.stemSkill(skill);
                else
                    tempHold.stemSkill(skill);
                    // CHECK FOR REDUNDANCY
            }
        }
        
        long xp = (long) charObject.get("xp");

        HashMap<String, Long> inventory = new HashMap<String, Long>();
        JSONArray jInventory = (JSONArray) charObject.get("inventory");
        for (Object item : jInventory)
        {
            JSONObject jItem = (JSONObject) item;

            String itemName = (String) jItem.get("item");
            String itemDesc = (String) jItem.get("description");
            long itemCount = (long) jItem.get("amount");

            inventory.put(itemName + "~" + itemDesc, itemCount);
        }

        PlayerCharacter retVal = new PlayerCharacter(identifiers, xp, treeRoot, inventory);
        return retVal;
    }

    public static void buildCast(String[] existingSaves)
    {
        int castSize;
        String memField = "";
        ArrayList<String> memID = new ArrayList<String>();

        castSize = Integer.valueOf(validateCycle("\nHow many members will be in the cast [#]:", Integer.class, 1));

        for (int i = 0; i < castSize; i++)
        {
            memID.clear();

            memField = validateCycle("\nWhat is character " + (i + 1) + "'s name [__]:", String.class, Integer.MIN_VALUE);
            memID.add(memField);

            memField = validateCycle("\nWhat are " + memField + "'s pronouns [__,...,__]:", String.class, Integer.MIN_VALUE);
            memID.addAll(Arrays.asList(memField.replaceAll("\\s", "").split(",")));

            PlayerCharacter memTemp = new PlayerCharacter(new ArrayList<String>(memID), 0, new Skill(), new HashMap<String, Long>());
            cast.add(memTemp);
        }

        System.out.println("\nSession Cast:\n-------------\n");
        for (PlayerCharacter mem : cast)
            mem.displayStats();

        boolean repeatSave = true;
        String savePath = "";
        do
        {
            savePath = validateCycle("Name your save file [A-z]: ", String.class, Integer.MIN_VALUE) + "_save.json";
            for (String path : existingSaves)
            {
                if (savePath.equals(path))
                {
                    System.out.println("\n!!! {FILENAME ALREADY EXISTS} !!!\n");
                    repeatSave = true;
                    break;
                }
                else
                    repeatSave = false;
            }

            if (!savePath.substring(0, savePath.length() - 10).matches("^[a-zA-Z]*$"))
            {
                System.out.println("\n!!! {INPUT CONTAINS NON-ALPHABETIC ELEMENTS} !!!\n");
                repeatSave = true;
            }
        }
        while (repeatSave);

        System.out.println("\nFinalizing new save..." + 
                            "\n" + System.getProperty("user.dir") + "/Save Files/" + savePath + "\n");
        runSession(cast, savePath);
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

    public static String validateCycle(String question, Class<?> typeExpect, int limitRange)
    {   
        int intInput;
        String strInput;
        String strInputFinal = "";
        
        do
        {
            try
            {
                System.out.println(question);
                for (int i = 0; i < question.length() - 1; i++)
                    System.out.print("-");
                System.out.print("\n>_: ");

                userInput.nextLine();

                if (typeExpect == Integer.class)
                {
                    intInput = userInput.nextInt();
                    if (intInput < limitRange)
                    {
                        System.out.println("\n!!! {INPUT OUT OF RANGE} !!!");
                        continue;
                    }

                    strInputFinal = String.valueOf(intInput);
                }
                else if (typeExpect == String.class)
                {
                    strInput = userInput.nextLine();
                    if (strInput.length() == 0)
                    {
                        System.out.println("\n!!! {INPUT EMPTY} !!!");
                        continue;
                    }

                    strInputFinal = strInput;
                }

                if(confirmInput(strInputFinal))
                    break;
            }
            catch (Exception e)
            {
                System.out.println("\n!!! {INPUT INVALID} !!!");
                userInput.next();
            }
        }
        while(true);

        return strInputFinal;
    }


    // ========== GAMEPLAY ==========
    public static void runSession(ArrayList<PlayerCharacter> cast, String path)
    {
        // Exit and Help needs to ONLY have "exit"
        // List has sub commands (cast, skills, inventory), REQUIRES player
        // Roll REQUIRES a player AND their skill, reason is optional

        boolean runGame = true;
        String[] commands = new String[]{"Exit", "Help", "List", "Roll", "!c", "!i", "!s"};

        String inputCommand = "";
        while (true)
        {
            inputCommand = userInput.nextLine().toLowerCase();
        }
    }
}