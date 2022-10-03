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
import java.util.Random;

public class RollForShoes
{
    static Scanner userInput;
    static ArrayList<PlayerCharacter> cast;
    static String[] castNames;

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
                if (i < castList.size() - 1)
                    System.out.println("\n");
                else
                    System.out.println();
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
                tempHold.stemSkill(skill);
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

        castSize = Integer.valueOf(validateCycle("\nHow many members will be in the cast [#]:", Integer.class, null, 1, Integer.MAX_VALUE));

        for (int i = 0; i < castSize; i++)
        {
            memID.clear();

            memField = validateCycle("\nWhat is character " + (i + 1) + "'s name [__]:", String.class, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            memID.add(memField);

            memField = validateCycle("\nWhat are " + memField + "'s pronouns [__,...,__]:", String.class, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            memID.addAll(Arrays.asList(memField.replaceAll("\\s", "").split(",")));

            PlayerCharacter memTemp = new PlayerCharacter(new ArrayList<String>(memID), 0, new Skill(), new HashMap<String, Long>());
            cast.add(memTemp);
        }

        System.out.println("\nSession Cast:\n-------------\n");
        for (PlayerCharacter mem : cast)
        {
            mem.displayStats();
            System.out.println("\n");
        }

        boolean repeatSave = true;
        String savePath = "";
        do
        {
            savePath = validateCycle("Name your save file [A-z]: ", String.class, null, Integer.MIN_VALUE, Integer.MAX_VALUE) + "_save.json";
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

        savePath = System.getProperty("user.dir") + "/Save Files/" + savePath;
        System.out.println("\nFinalizing new save..." + 
                            "\n" + savePath);
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
                System.out.println("\nConfirm your selection [Y/n]: '" 
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

    public static String validateCycle(String question, Class<?> typeExpect, String[] options, int limitRangeMin, int limitRangeMax)
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
                System.out.println();
                if (options != null)
                {
                    for (int i = 0; i < options.length; i++)
                    System.out.println("\t" + (i + 1) + "). " + options[i]);
                }
                System.out.print(">_: ");

                userInput.nextLine();

                if (typeExpect == Integer.class)
                {
                    intInput = userInput.nextInt();
                    if (intInput < limitRangeMin || intInput > limitRangeMax)
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
        boolean runGame = true;
        String[] commands = new String[]{"Help", "List Aspects", "Use Item", "Roll Skill"};

        castNames = new String[cast.size()];
        for (int i = 0; i < cast.size(); i++)
            castNames[i] = cast.get(i).getName();

        int inputSelect;
        while (runGame)
        {
            inputSelect = Integer.valueOf(validateCycle("\nSelect a command [#] or enter '0' to exit gameplay:", Integer.class, commands, 0, 4));

            switch (inputSelect)
            {
                case 0:
                    System.out.println("\nSaving and Exiting...");
                    runGame = false;
                    break;
                case 1:
                    help();
                    break;
                case 2:
                    listAspects(castNames);
                    break;
                case 3:
                    useItem(castNames);
                    break;
                case 4:
                    rollSkill(castNames);
                    break;
            }
        }

        saveGame(path);
    }

    public static void help()
    {
        System.out.println("\n==================== HELP ==================== \n\n"
                            + "EXIT: Ends the session and saves any changes \n\n"
                            + "HELP: Shows this text explaining the functions \n"
                            + "of all the available commands \n\n"
                            + "LIST ASPECTS: Displays the information for a \n"
                            + "given character from the current cast \n\n"
                            + "ROLL SKILL: Rolls a number of d6's equal to \n"
                            + "the respective character's skill against an \n"
                            + "opposing roll to determine success or failure \n\n"
                            + "==============================================");
    }

    public static void listAspects(String[] nameOptions)
    {
        int memOpt = Integer.valueOf(validateCycle("\nSelect a character [#] to display their stats:", Integer.class, nameOptions, 0, nameOptions.length));
        
        System.out.println();
        cast.get(memOpt - 1).displayStats();
        System.out.println();
    }

    public static void useItem(String[] nameOptions)
    {
        int memOpt = Integer.valueOf(validateCycle("\nSelect a character [#] to display their inventory:", Integer.class, nameOptions, 0, nameOptions.length));

        HashMap<String, Long> inventory = cast.get(memOpt - 1).getInventory();
        String[][] invKeys = new String[inventory.size()][2];
        String[] invNames = new String[inventory.size()];

        if (inventory.isEmpty())
            System.out.println("\n!!! {" + cast.get(memOpt - 1).getName().toUpperCase() + "'S INVENTORY IS EMPTY} !!!");
        else
        {
            int iCount = 0;
            for (String key: inventory.keySet())
            {
                String[] itemDescs = key.split("~");
                invKeys[iCount][0] = invNames[iCount] = itemDescs[0];
                invKeys[iCount][1] = itemDescs[1];

                iCount++;
            }
        }

        int invOpt = Integer.valueOf(validateCycle("\nSelect an item [#] to interact with or enter '0' to add a new item:", Integer.class, invNames, 0, inventory.size()));
        
        if (invOpt == 0 || inventory.isEmpty())
        {
            String newItem = validateCycle("\nWhat item [A-z] will be added to " + cast.get(memOpt - 1).getName() + "'s inventory:", String.class, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            newItem += "~" + validateCycle("\n Give " + newItem + " a description [A-z]:", String.class, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            
            inventory.put(newItem, 1L);
        }
        else
        {
            invOpt--;

            System.out.println("\n" + invKeys[invOpt][0] + " (x" + inventory.get(invKeys[invOpt][0] + "~" + invKeys[invOpt][1]) + "): " + invKeys[invOpt][1]);
            
            int amtChange = Integer.valueOf(validateCycle("\nEnter a value [#] to increase/decrease the amount by:", Integer.class, null, (int)(inventory.get(invKeys[invOpt][0] + "~" + invKeys[invOpt][1]) * -1), Integer.MAX_VALUE));
            if (amtChange == (int)(inventory.get(invKeys[invOpt][0] + "~" + invKeys[invOpt][1]) * -1))
                inventory.remove(invKeys[invOpt][0] + "~" + invKeys[invOpt][1]);
            else
                inventory.put(invKeys[invOpt][0] + "~" + invKeys[invOpt][1], (inventory.get(invKeys[invOpt][0] + "~" + invKeys[invOpt][1]) + amtChange));
        }
    }

    public static void rollSkill(String[] nameOptions)
    {
        Random randNum = new Random();
        Skill memRoot;

        int memOpt = Integer.valueOf(validateCycle("\nSelect a character's [#] skills to roll:", Integer.class, nameOptions, 0, nameOptions.length));
        memRoot = cast.get(memOpt - 1).getSkillRoot();

        System.out.println();
        memRoot.printTree();

        int[] rollsPlayer;
        int[] rollsComp;
        int sumPlayer = 0;
        int sumComp = 0;

        int diceNum = 0;
        int sixes = 0;
        boolean success = false;

        boolean skillFound = false;
        Skill skillRolled = null;
        do
        {
            String rollOpt = validateCycle("\nSelect a which of " + cast.get(memOpt - 1).getName() + "'s skills [A-z] to roll:", String.class, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            skillRolled = memRoot.searchSkills(rollOpt);

            if (skillRolled != null)
            {
                skillFound = true;
                diceNum = (int) skillRolled.getLevel();
                
                rollsPlayer = new int[diceNum];
                rollsComp = new int[diceNum];
                for (int i = 0; i < diceNum; i++)
                {
                    int roll = randNum.nextInt(1, 6 + 1);
                    if (roll == 6)
                        sixes++;
                    rollsPlayer[i] = roll;
                    sumPlayer += roll;

                    roll = randNum.nextInt(1, 6 + 1);

                    rollsComp[i] = roll;
                    sumComp += roll;
                }

                System.out.println("\nYour Rolls: " + Arrays.toString(rollsPlayer) + ", Your Sum: " + sumPlayer
                                + "\nAdv. Rolls: " + Arrays.toString(rollsComp) + ", Adv. Sum: " + sumComp);
                
                if (sumPlayer > sumComp)
                    success = true;
            }
            else
                System.out.println("\n!!! {SKILL NOT FOUND} !!!");
        }  
        while (!skillFound);   
        
        if (success)
        {
            System.out.println("\nYou PASS the skill check!");
            if (sixes == diceNum)
            {
                String newSkill = validateCycle("\nWhat skill will branch off from \"" + skillRolled.getName() + "\" [A-z]:", String.class, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
                skillRolled.stemSkill(newSkill);
                
                System.out.println(cast.get(memOpt - 1).getName() + " learned a new skill, '" + newSkill + "' (Lvl. " + memRoot.searchSkills(newSkill).getLevel() + ")!");
            }
            else if ((sixes + (int) cast.get(memOpt - 1).getXP() >= diceNum) && (sixes != diceNum))
            {
                boolean confirmLoopExit = false;
                String confirmSkill = "";

                do
                {
                    String prompt = "\nWould you like to use " + (diceNum - sixes) + " XP to gain a new skill [Y/n]:";
                    String border = "\n";
                    for (int i = 0; i < prompt.length() - 1; i++)
                        border += "-";

                    System.out.println(prompt + border);

                    System.out.print(">_: ");
                    confirmSkill = userInput.next().toLowerCase();

                    if (!(confirmSkill.equals("y") || confirmSkill.equals("n")))
                        System.out.println("\n!!! {INPUT INVALID} !!!");
                    else
                        confirmLoopExit = true;
                }
                while(!confirmLoopExit);

                if (confirmSkill.equals("y"))
                {
                    cast.get(memOpt - 1).alterXP(-1 * (diceNum - sixes));

                    String newSkill = validateCycle("\nWhat skill will branch off from \"" + skillRolled.getName() + "\" [A-z]:", String.class, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    skillRolled.stemSkill(newSkill);

                    System.out.println("\n" + cast.get(memOpt - 1).getName() + "'s XP reduced! " + (cast.get(memOpt - 1).getXP() + (diceNum - sixes)) + " -> " + cast.get(memOpt - 1).getXP());
                    System.out.println(cast.get(memOpt - 1).getName() + " learned a new skill, '" + newSkill + "' (Lvl. " + memRoot.searchSkills(newSkill).getLevel() + ")!");
                }
            }

        }
        else
        {
            System.out.println("\nYou FAIL the skill check...");
            cast.get(memOpt - 1).alterXP(1);
            System.out.println("" + cast.get(memOpt - 1).getName() + "'s XP increased! " + (cast.get(memOpt - 1).getXP() - 1) + " -> " + cast.get(memOpt - 1).getXP());
        }
    }

    // ========== SAVING ==========
    @SuppressWarnings("unchecked")
    public static void saveGame(String path)
    {
        System.out.println(path);
        // turn all players into JSONObjects
        // add JSONObjects to array
        // write array to file
        // either delete old file or rewrite over said file
        try (FileWriter writer = new FileWriter(path))
        {
            JSONArray jCast = new JSONArray();

            for (PlayerCharacter mem : cast)
            {
                JSONObject memContainer = new JSONObject();

                JSONObject jMem = mem.jsonify();
                memContainer.put("character", jMem);

                jCast.add(memContainer);
            }

            writer.write(jCast.toJSONString());
        }
        catch (IOException e) 
        {
            System.out.println("\n!!! {ERROR READING FILE} !!!");
            e.printStackTrace();
        } 
        /*catch (ParseException e) 
        {
            System.out.println("\n!!! {ERROR READING FILE} !!!");
            e.printStackTrace();
        }*/
        catch (Exception e)
        {
            System.out.println("\n!!! {ERROR READING FILE} !!!");
            e.printStackTrace();
        }
        
    }
}