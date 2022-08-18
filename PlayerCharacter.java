import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PlayerCharacter
{
    private String charName;
    private ArrayList<String> charPronouns;

    private long charXp;
    //? private Skill skillTreeRoot; will eventually implement

    private HashMap<String, Long> charInv;

    PlayerCharacter(ArrayList<String> ids, long xp, HashMap<String, Long> inv)
    {
        charName = ids.get(0);
        ids.remove(0);

        charPronouns = ids;

        charXp = xp;

        charInv = inv;
    }

    public void displayStats()
    {
        String header = "===== " + charName + " " + charPronouns.toString() + " =====\n";
        System.out.println(header);

        System.out.println("Remaining XP: " + charXp);

        System.out.println("Inventory: ");
        if (charInv.isEmpty())
            System.out.println("  -Empty!");
        else
        {
            for (String key: charInv.keySet())
            {
                String[] itemDescs = key.split("~");
                long amt = charInv.get(key);

                System.out.println("  -" + itemDescs[0] + " (x" + amt + "): " + itemDescs[1]);
            }
        }

        System.out.print("\n");
        for (int i = 0; i < header.length(); i++)
            System.out.print("=");
        System.out.println("\n");
    }
}
